package com.coldmint.rust.core

import android.content.Context
import android.os.Build
import com.coldmint.rust.core.interfaces.*
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.Exception

/**
 * 压缩管理器
 */
class CompressionManager private constructor() {

    companion object {
        val instance: CompressionManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { CompressionManager() }
    }


    var fileFinder: FileFinderInterface? = null

    /**
     * 读取Zip条目的内容
     * @param targetFile File 目标文件
     * @param name String 条目名
     * @return String?
     */
    fun readEntry(targetFile: File, name: String): String? {
        var result: String? = null
        var zipFile: ZipFile? = null
        try {
            zipFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ZipFile(targetFile, Charset.forName("GBK"))
            } else {
                ZipFile(targetFile)
            }
            val entry = zipFile.getEntry(name)
            val inputStream = zipFile.getInputStream(entry)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var len = 0
            val buffer = ByteArray(1024)
            while (inputStream.read(buffer).also { len = it } > -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            result = byteArrayOutputStream.toString()
            inputStream.close()
            byteArrayOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 解压方法
     *
     * @return 是否解压成功
     */
    fun unzip(
        targetFile: File,
        outPutFile: File,
        unzipListener: UnzipListener?,
        startsWithData: String? = null
    ): Boolean {
        var flag = false
        var zipFile: ZipFile? = null
        try {
            zipFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ZipFile(targetFile, Charset.forName("GBK"))
            } else {
                ZipFile(targetFile)
            }
            val e: Enumeration<*> = zipFile.entries()
            var zipEntry: ZipEntry? = null
            val dest = outPutFile
            if (!dest.exists()) {
                dest.mkdirs()
            }
            while (e.hasMoreElements()) {
                zipEntry = e.nextElement() as ZipEntry
                val entryName = zipEntry.name
                //如果有开头限制条件，并且name不符合条件跳出
                if (startsWithData != null && !entryName.startsWith(startsWithData)) {
                    break
                }
                var `in`: BufferedInputStream? = null
                var out: BufferedOutputStream? = null
                try {
                    if (zipEntry.isDirectory) {
                        var name = zipEntry.name
                        name = name.substring(0, name.length - 1)
                        val f = File(outPutFile.toString() + File.separator + name)
                        f.mkdirs()
                        if (unzipListener != null && !unzipListener.whenUnzipFolder(zipEntry, f)) {
                            flag = true
                            break
                        }
                    } else {
                        var index = entryName.lastIndexOf("\\")
                        if (index != -1) {
                            val df = File(
                                outPutFile.toString() + File.separator + entryName.substring(
                                    0,
                                    index
                                )
                            )
                            df.mkdirs()
                        }
                        index = entryName.lastIndexOf("/")
                        if (index != -1) {
                            val df = File(
                                outPutFile.toString() + File.separator + entryName.substring(
                                    0,
                                    index
                                )
                            )
                            df.mkdirs()
                        }
                        val f = File(outPutFile.toString() + File.separator + zipEntry.name)
                        `in` = BufferedInputStream(zipFile.getInputStream(zipEntry))
                        out = BufferedOutputStream(FileOutputStream(f))
                        val by = ByteArray(1024)
                        var c: Int
                        while (`in`.read(by).also { c = it } != -1) {
                            out.write(by, 0, c)
                        }
                        out.flush()
                        if (unzipListener != null && !unzipListener.whenUnzipFile(zipEntry, f)) {
                            flag = true
                            break
                        }
                    }
                    flag = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    flag = false
                } finally {
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            flag = false
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (ex: Exception) {
                    flag = false
                }
            }
        }
        unzipListener?.whenUnzipComplete(flag)
        return flag
    }

    /**
     * 压缩方法
     */
    fun compression(
        targetFile: File,
        outPutFile: File,
        compressionListener: CompressionListener,
        compressionInterceptor: CompressionInterceptor?
    ): Boolean {
        var zipOutputStream: ZipOutputStream? = null

        val temFileFinder: FileFinderInterface? = fileFinder
        val useFileFinder: FileFinderInterface = temFileFinder ?: FileFinder2(targetFile)
        useFileFinder.setFinderListener(object : FileFinderListener {
            override fun whenFindFile(file: File): Boolean {
                try {
                    val bufferedInputStream = BufferedInputStream(FileInputStream(file))
                    val relativePath =
                        FileOperator.getRelativePath(file, targetFile)?.substring(1)
                    if (zipOutputStream == null) {
                        val bufferedOutputStream =
                            BufferedOutputStream(FileOutputStream(outPutFile))
                        zipOutputStream = ZipOutputStream(bufferedOutputStream)
                    }
                    if (compressionInterceptor != null) {
                        val uselessFileRule = compressionInterceptor.uselessFileRule
                        if (uselessFileRule.isNotEmpty() && file.name.matches(
                                Regex(
                                    uselessFileRule
                                )
                            )
                        ) {
                            return compressionListener.whenCompressionFolder(file)
                        }
                        if (file.name.matches(Regex(compressionInterceptor.sourceFileRule))) {
                            val code = compressionInterceptor.getSourceCode(file)
                            if (code != null) {
                                zipOutputStream!!.putNextEntry(ZipEntry(relativePath))
                                zipOutputStream!!.write(code.toByteArray(StandardCharsets.UTF_8))
                                zipOutputStream!!.closeEntry()
                            }
                        } else {
                            var len = 0
                            val buffer = ByteArray(4096)
                            zipOutputStream!!.putNextEntry(ZipEntry(relativePath))
                            while (bufferedInputStream.read(buffer).also { len = it } > -1) {
                                zipOutputStream!!.write(buffer, 0, len)
                            }
                            zipOutputStream!!.closeEntry()
                        }
                    } else {
                        var len = 0
                        val buffer = ByteArray(4096)
                        zipOutputStream!!.putNextEntry(ZipEntry(relativePath))
                        while (bufferedInputStream.read(buffer).also { len = it } > -1) {
                            zipOutputStream!!.write(buffer, 0, len)
                        }
                        zipOutputStream!!.closeEntry()
                    }
                    bufferedInputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return compressionListener.whenCompressionFile(file)
            }

            override fun whenFindFolder(folder: File): Boolean {
                return compressionListener.whenCompressionFolder(folder)
            }
        })
        val result = useFileFinder.onStart()
        try {
            if (zipOutputStream != null) {
                zipOutputStream!!.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        zipOutputStream = null
        compressionListener.whenCompressionComplete(result)
        return result
    }
}