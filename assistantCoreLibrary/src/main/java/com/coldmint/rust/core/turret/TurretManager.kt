package com.coldmint.rust.core.turret

import android.util.Log
import com.coldmint.rust.core.SourceFile
import java.io.File

/**
 * 炮塔管理器
 * 增加，移除，修改炮塔
 * 此类用于管理源文件内的炮塔数据
 * @constructor
 */
class TurretManager(val sourceFile: SourceFile) {

    /**
     * 可安装的炮塔列表
     */
    val turretList by lazy {
        ArrayList<InstallableTurret>()
    }

    init {
        val allSection = sourceFile.allSection
        val size = allSection.size
        if (size > 0) {
            val defaultImageList =
                sourceFile.findResourceFilesFromSection("image_turret", "graphics", false)
            var defaultImageFile: File? = null
            if (!defaultImageList.isNullOrEmpty()) {
                defaultImageFile = defaultImageList.get(0)
                Log.d("炮塔管理器-默认图像", defaultImageFile.absolutePath)
            }
            allSection.forEach {
                //遍历每一个炮塔
                val type = SourceFile.getSectionType(it)
                if (type == "turret") {
                    val name = SourceFile.getAbsoluteSectionName(it)
                    var x = 0
                    var y = 0
                    val xData = sourceFile.readValueFromSection("x", it)
                    if (xData != null) {
                        x = xData.toInt()
                    }
                    val yData = sourceFile.readValueFromSection("y", it)
                    if (yData != null) {
                        y = yData.toInt()
                    }
                    val installableTurret = InstallableTurret(name, x, y)
                    val fileList = sourceFile.findResourceFilesFromSection("image", it, false)
                    if (!fileList.isNullOrEmpty()) {
                        val file = fileList.get(0)
                        installableTurret.imageFile = file
                        Log.d("炮塔管理器-$name", file.absolutePath)
                    } else {
                        installableTurret.imageFile = defaultImageFile
                        Log.d("炮塔管理器-$name", "加载默认图像" + defaultImageFile?.absolutePath)
                    }
                    turretList.add(installableTurret)
                }
            }
        }
    }


}