package com.coldmint.rust.core.tool

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.coldmint.rust.core.R
import kotlin.Throws
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.coldmint.rust.core.interfaces.RemoveAndCopyListener
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.Exception

/**
 * 文件操作类
 */
object FileOperator {
    private val MATCH_ARRAY = arrayOf(
        arrayOf(".3gp", "video/3gpp"),
        arrayOf(".apk", "application/vnd.android.package-archive"),
        arrayOf(".asf", "video/x-ms-asf"),
        arrayOf(".avi", "video/x-msvideo"),
        arrayOf(".bin", "application/octet-stream"),
        arrayOf(".bmp", "image/bmp"),
        arrayOf(".class", "application/octet-stream"),
        arrayOf(".conf", "text/plain"),
        arrayOf(".doc", "application/msword"),
        arrayOf(".exe", "application/octet-stream"),
        arrayOf(".gif", "image/gif"),
        arrayOf(".gtar", "application/x-gtar"),
        arrayOf(".gz", "application/x-gzip"),
        arrayOf(".htm", "text/html"),
        arrayOf(".html", "text/html"),
        arrayOf(".jpeg", "image/jpeg"),
        arrayOf(".jpg", "image/jpeg"),
        arrayOf(".log", "text/plain"),
        arrayOf(".m3u", "audio/x-mpegurl"),
        arrayOf(".m4a", "audio/mp4a-latm"),
        arrayOf(".m4b", "audio/mp4a-latm"),
        arrayOf(".m4p", "audio/mp4a-latm"),
        arrayOf(".m4u", "video/vnd.mpegurl"),
        arrayOf(".m4v", "video/x-m4v"),
        arrayOf(".mov", "video/quicktime"),
        arrayOf(".mp2", "audio/x-mpeg"),
        arrayOf(".mp3", "audio/x-mpeg"),
        arrayOf(".mp4", "video/mp4"),
        arrayOf(".mpc", "application/vnd.mpohun.certificate"),
        arrayOf(".mpe", "video/mpeg"),
        arrayOf(".mpeg", "video/mpeg"),
        arrayOf(".mpg", "video/mpeg"),
        arrayOf(".mpg4", "video/mp4"),
        arrayOf(".mpga", "audio/mpeg"),
        arrayOf(".msg", "application/vnd.ms-outlook"),
        arrayOf(".ogg", "audio/ogg"),
        arrayOf(".pdf", "application/pdf"),
        arrayOf(".png", "image/png"),
        arrayOf(".pps", "application/vnd.ms-powerpoint"),
        arrayOf(".ppt", "application/vnd.ms-powerpoint"),
        arrayOf(".prop", "text/plain"),
        arrayOf(".rar", "application/x-rar-compressed"),
        arrayOf(".rc", "text/plain"),
        arrayOf(".rmvb", "audio/x-pn-realaudio"),
        arrayOf(".rtf", "application/rtf"),
        arrayOf(".sh", "text/plain"),
        arrayOf(".tar", "application/x-tar"),
        arrayOf(".tgz", "application/x-compressed"),
        arrayOf(".wav", "audio/x-wav"),
        arrayOf(".wma", "audio/x-ms-wma"),
        arrayOf(".wmv", "audio/x-ms-wmv"),
        arrayOf(".wps", "application/vnd.ms-works"),
        arrayOf(".z", "application/x-compress"),
        arrayOf(".zip", "application/zip"),
        arrayOf("", "*/*")
    )

    /**
     * 获取MimeType
     *
     * @param path 路径
     * @return mineType
     */
    private fun getMimeType(path: String): String {
        for (strings in MATCH_ARRAY) {
            if (path.contains(strings[0])) {
                return strings[1]
            }
        }
        return "*/*"
    }


    /**
     * 解析图片地址
     *
     * @param context 上下文环境
     * @param intent  意图
     * @return 成功返回图片路径，失败返回null
     */
    fun parsePicturePath(context: Context, intent: Intent?): String? {
        if (intent != null) {
            val selectedImage = intent.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor =
                context.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            return picturePath
        }
        return null
    }




    /**
     * 调用app打开文件
     *
     * @param context 上下文环境
     * @param file    文件
     */
    fun openFile(context: Context?, file: File?) {
        if (context == null || file == null) return
        val intent = Intent()
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.action = Intent.ACTION_VIEW
        val type = getMimeType(file.absolutePath)
        try {
            val uri: Uri
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, context.packageName + ".fileProvider", file)
            } else {
                Uri.fromFile(file)
            }
            intent.setDataAndType(uri, type)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 分享文件
     *
     * @param context   上下文环境
     * @param shareFile 文件
     */
    fun shareFile(context: Context?, shareFile: File?) {
        if (shareFile == null || context == null) {
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileProvider",
                shareFile
            )
            context.grantUriPermission(
                context.packageName,
                contentUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile))
        }
        intent.type = getMimeType(shareFile.absolutePath)
        val chooser = Intent.createChooser(intent, context.getString(R.string.share))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        }
    }

    /**
     * 获取文件SHA1值
     *
     * @param file 文件对象
     * @return SHA1值，失败返回null
     */
    fun getSHA1(file: File): String? {
        return try {
            getDigest(file, "SHA1")
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取文件MD5值
     *
     * @param file 文件对象
     * @return MD5值，失败返回null
     */
    fun getMD5(file: File): String? {
        return try {
            getDigest(file, "MD5")
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    //获取文件编码
    @Throws(IOException::class)
    private fun getDigest(file: File, algo: String): String? {
        try {
            val md = MessageDigest.getInstance(algo)
            val buffer = ByteArray(8192)
            val fis = FileInputStream(file)
            while (true) {
                val r = fis.read(buffer)
                if (r == -1) {
                    break
                }
                md.update(buffer, 0, r)
            }
            fis.close()
            return BigInteger(1, md.digest()).toString(16)
        } catch (e: NoSuchAlgorithmException) {
        }
        return null
    }

    /**
     * 写出资源文件(assets目录)
     *
     * @param context  上下文环境
     * @param filename 文件名
     * @param newFile  新的文件位置
     * @return 写出结果
     */
    fun outputResourceFile(context: Context, filename: String?, newFile: File): Boolean {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.assets.open(filename!!)
            if (!newFile.parentFile.exists()) {
                newFile.parentFile.mkdirs()
            }
            if (inputStream != null) {
                val outputStream = BufferedOutputStream(FileOutputStream(newFile))
                val bytes = ByteArray(1024)
                var c: Int
                while (inputStream.read(bytes).also { c = it } > 0) {
                    outputStream.write(bytes, 0, c)
                }
                inputStream.close()
                outputStream.close()
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 写文件
     *
     * @param file 文件
     * @param text 内容
     * @return 是否写入成功
     */
    @JvmStatic
    fun writeFile(file: File, text: String): Boolean {
        return try {
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            outputStream.write(text.toByteArray())
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读取文件
     *
     * @param targetFile 目标文件
     * @return 成功返回文件内容，失败返回null
     */
    @JvmStatic
    fun readFile(targetFile: File?): String? {
        return if (targetFile != null && targetFile.exists()) {
            try {
                val inputStream = BufferedInputStream(FileInputStream(targetFile))
                val byteArrayOutputStream = ByteArrayOutputStream()
                var len = 0
                val buffer = ByteArray(1024)
                while (inputStream.read(buffer).also { len = it } > -1) {
                    byteArrayOutputStream.write(buffer, 0, len)
                }
                val result = byteArrayOutputStream.toString()
                inputStream.close()
                byteArrayOutputStream.close()
                result
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * 复制文件方法
     * 若父文件夹不存在，则自动创建
     *
     * @param form 源文件
     * @param to   目标文件
     * @return 是否复制成功
     */
    fun copyFile(form: File?, to: File?): Boolean {
        if (form == null || !form.exists()) {
            return false
        }
        if (to == null || to.exists()) {
            return false
        }
        if (to.parentFile != null && !to.parentFile.exists()) {
            to.parentFile.mkdirs()
        }
        return try {
            val inputStream = BufferedInputStream(FileInputStream(form))
            val outputStream = BufferedOutputStream(FileOutputStream(to))
            val bytes = ByteArray(1024)
            var c: Int
            while (inputStream.read(bytes).also { c = it } > 0) {
                outputStream.write(bytes, 0, c)
            }
            inputStream.close()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 复制文件方法
     * 若父文件夹不存在，则自动创建
     *
     * @param inputStream 输入流
     * @param to          目标文件
     * @return 是否复制成功
     */
    fun copyFile(inputStream: InputStream, to: File): Boolean {
        if (to.exists()) {
            return false
        }
        if (to.parentFile != null && !to.parentFile.exists()) {
            to.parentFile.mkdirs()
        }
        return try {
            val outputStream = BufferedOutputStream(FileOutputStream(to))
            val bytes = ByteArray(1024)
            var c: Int
            while (inputStream.read(bytes).also { c = it } > 0) {
                outputStream.write(bytes, 0, c)
            }
            inputStream.close()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 移动文件
     * 自动创建子文件夹
     *
     * @param form 源
     * @param to   新文件
     * @return 是否移动成功
     */
    fun removeFile(form: File, to: File?): Boolean {
        return if (copyFile(form, to)) {
            form.delete()
        } else {
            false
        }
    }

    /**
     * 移动文件
     *
     * @param from 来源
     * @param to   到
     * @param listener 监听器
     * @return 是否复制成功
     */
    fun removeFiles(from: File, to: File, listener: RemoveAndCopyListener? = null): Boolean {
        var result = false
        if (to.absolutePath.startsWith(from.absolutePath)) {
            return false
        }
        if (!from.isDirectory) {
            return removeFile(from, to)
        }
        if (!to.exists()) {
            to.mkdirs()
        }
        val files = from.listFiles()
        return if (files.isNotEmpty()) {
            for (mfile in files) {
                listener?.whenOperatorFile(mfile)
                if (mfile.isDirectory) {
                    result = removeFiles(
                        mfile,
                        File(to.absolutePath + "/" + getRelativePath(mfile, from)), listener
                    )
                    mfile.delete()
                } else {
                    result = removeFile(
                        mfile,
                        File(to.absolutePath + "/" + getRelativePath(mfile, from))
                    )
                }

            }
            delete_files(from)
            result
        } else {
            true
        }
    }

    /**
     * 复制文件夹
     *
     * @param from 从哪里
     * @param to   到哪里
     * @return
     */
    fun copyFiles(from: File, to: File, listener: RemoveAndCopyListener? = null): Boolean {
        var result = false
        if (to.absolutePath.startsWith(from.absolutePath)) {
            return false
        }
        if (!from.isDirectory) {
            return copyFile(from, to)
        }
        if (!to.exists()) {
            to.mkdirs()
        }
        val files = from.listFiles()
        return if (files.isNotEmpty()) {
            for (mfile in files) {
                listener?.whenOperatorFile(mfile)
                if (mfile.isDirectory) {
                    result =
                        copyFiles(
                            mfile,
                            File(to.absolutePath + "/" + getRelativePath(mfile, from)),
                            listener
                        )
                    mfile.delete()
                } else {
                    result =
                        copyFile(mfile, File(to.absolutePath + "/" + getRelativePath(mfile, from)))
                }

            }
            result
        } else {
            true
        }
    }

    //根据路径获得document文件
//    fun getDocumentFile(context: Context?, path: String): DocumentFile? {
//        var path = path
//        if (path.endsWith("/")) {
//            path = path.substring(0, path.length - 1)
//        }
//        val path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
//        return DocumentFile.fromTreeUri(
//            context!!,
//            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A${path2}")
//        )
//    }

    /*获取文件类型*/
    @JvmStatic
    fun getFileType(target: File): String {
        val file_name = target.name
        return if (file_name.contains(".")) {
            val Start_num = file_name.lastIndexOf(".")
            file_name.substring(Start_num + 1)
        } else {
            file_name
        }
    }

    /**
     * 获取文件前缀名
     *
     * @param target 目标文件
     * @return 文件前缀名
     */
    fun getPrefixName(target: File): String {
        val fileName = target.name
        return getPrefixName(fileName)
    }

    /**
     * 获取文件前缀名
     *
     * @param fileName 文件名
     * @return 文件前缀名
     */
    fun getPrefixName(fileName: String): String {
        return if (fileName.contains(".")) {
            val endNum = fileName.lastIndexOf(".")
            fileName.substring(0, endNum)
        } else {
            fileName
        }
    }

    /**
     * 获取文件上级目录
     *
     * @param file 文件
     * @return 上级目录
     */
    fun getSuperDirectory(file: File): String {
        return getSuperDirectory(file.absolutePath)
    }

    /**
     * 获取文件上级目录
     *
     * @param path 路径
     * @return 上级目录
     */
    fun getSuperDirectory(path: String): String {
        return if (path == Environment.getExternalStorageDirectory().absolutePath) {
            path
        } else {
            val endNum = path.lastIndexOf("/")
            if (endNum > 0) {
                path.substring(0, endNum)
            } else {
                path
            }
        }
    }

    /*获取文件上级目录*/
    fun getSuperDirectory(filePath: String, rootPath: String): String {
        return if (filePath == rootPath) {
            filePath
        } else {
            val endNum = filePath.lastIndexOf("/")
            if (endNum > 0) {
                filePath.substring(0, endNum)
            } else {
                filePath
            }
        }
    }

    /**
     * 获取文件相对目录
     *
     * @param file     文件
     * @param relative 相对文件
     * @return 相对路径
     */
    fun getRelativePath(file: File, relative: File): String? {
        val filePath = file.absolutePath
        val relativePath = relative.absolutePath
        return if (filePath.startsWith(relativePath)) {
            filePath.substring(relativePath.length)
        } else {
            null
        }
    }

    /**
     * 获取文件相对目录
     *
     * @param filePath     文件路径
     * @param relativePath 相对文件路径
     * @return 相对目录，获取失败返回null
     */
    fun getRelativePath(filePath: String, relativePath: String): String? {
        return if (filePath.startsWith(relativePath)) {
            filePath.substring(relativePath.length)
        } else {
            null
        }
    }

    /**
     * 获取文件多级相对目录
     *
     * @param filePath     文件位置
     * @param relativePath 相对目录
     * @param rootPath     根目录
     * @return
     */
    fun getRelativePath(filePath: String, relativePath: String, rootPath: String): String? {
        var relativePath1 = getRelativePath(filePath, relativePath)
        return if (relativePath1 == null) {
            //不在同一目录
            getRelativePath(filePath, rootPath)
        } else {
            if (relativePath1.startsWith("/")) {
                relativePath1 = relativePath1.substring(1)
            }
            //如果在子目录
            if (relativePath1.contains("/")) {
                getRelativePath(filePath, rootPath)
            } else {
                relativePath1
            }
        }
    }

    /*删除文件夹*/
    fun delete_files(target: File): Boolean {
        var result = false
        if (target.exists()) {
            result = if (target.isDirectory) {
                val files = target.listFiles()
                for (mfile in files) {
                    if (mfile.isDirectory) {
                        delete_files(mfile)
                    } else {
                        mfile.delete()
                    }
                }
                target.delete()
            } else {
                target.delete()
            }
        }
        return result
    }

}