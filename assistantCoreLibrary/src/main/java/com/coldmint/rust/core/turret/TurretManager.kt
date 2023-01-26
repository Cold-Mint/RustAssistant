package com.coldmint.rust.core.turret

import android.util.Log
import android.view.ViewGroup
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.debug.LogCat
import java.io.File

/**
 * 炮塔管理器
 * 增加，移除，修改炮塔
 * 此类用于管理源文件内的炮塔数据
 * @constructor
 */
class TurretManager(val sourceFile: SourceFile) {


    /**
     * 炮塔列表
     */
    val turretList by lazy {
        ArrayList<TurretData>()
    }

    //坐标改变监听器
    private var coordinateChangeListener: ((CoordinateData, TurretData) -> Unit)? = null


    /**
     * 设置坐标改变监听器
     * @param coordinateChangeListener Function2<CoordinateData, TurretData, Unit>?
     */
    fun setCoordinateChangeListener(coordinateChangeListener: ((CoordinateData, TurretData) -> Unit)?) {
        this.coordinateChangeListener = coordinateChangeListener
        if (turretList.size > 0) {
            turretList.forEach {
                viewMap[it.name]?.setCoordinateChangeListener(coordinateChangeListener)
            }
        }
    }

    /**
     * 炮塔和视图的映射
     */
    private val viewMap by lazy {
        HashMap<String, TurretView>()
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
                LogCat.d("炮塔管理器-默认图像", defaultImageFile.absolutePath)
            }
            allSection.forEach {
                //遍历每一个炮塔
                val type = SourceFile.getSectionType(it)
                if (type == "turret") {
                    val name = SourceFile.getAbsoluteSectionName(it)
                    var x = 0
                    var y = 0
                    val xData = sourceFile.readValueFromSection("x", it)
                    if (xData != null && xData.isNotBlank()) {
                        x = xData.toFloat().toInt()
                    }
                    val yData = sourceFile.readValueFromSection("y", it)
                    if (yData != null&& yData.isNotBlank()) {
                        y = yData.toFloat().toInt()
                    }
                    val turretData = TurretData(name, CoordinateData(x, y))
                    val fileList = sourceFile.findResourceFilesFromSection("image", it, false)
                    if (!fileList.isNullOrEmpty()) {
                        val file = fileList.get(0)
                        turretData.imageFile = file
                        LogCat.d("炮塔管理器-$name", "设置炮塔图像" + file.absolutePath)
                    } else {
                        turretData.imageFile = defaultImageFile
                        LogCat.d("炮塔管理器-$name", "加载默认图像" + defaultImageFile?.absolutePath)
                    }
                    turretList.add(turretData)
                }
            }
        }
    }


    /**
     * 保存改变的代码
     * @return Boolean
     */
    fun saveChange(): Boolean {
        turretList.forEach {
            val section = "turret_" + it.name
            sourceFile.writeValueFromSection("x", it.gameCoordinateData.x.toString(),section)
            sourceFile.writeValueFromSection("y", it.gameCoordinateData.y.toString(),section)
        }
        return sourceFile.save()
    }


    /**
     * 安装全部炮塔到视图
     * @param frameLayout FrameLayout
     * @param sourceFile SourceFile
     */
    fun installerAllTurret(
        viewGroup: ViewGroup,
        turretSketchpadView: TurretSketchpadView
    ) {
        viewMap.clear()
        turretList.forEach {
            installerTurret(viewGroup, it, turretSketchpadView)
        }
    }

    /**
     * 获取炮塔数据
     * @param name String 炮塔名称
     * @return TurretData?
     */
    fun getTurretView(name: String): TurretView? {
        return viewMap[name]
    }


    /**
     * 使用炮塔（激活其视图拖动，禁用其他）
     * @param turretData TurretData
     * @return TurretView?
     */
    fun useTurret(
        turretName: String
    ) {
        turretList.forEach {
            val use = it.name == turretName
            val view = viewMap[it.name]
            if (view == null) {
                LogCat.e("炮塔管理器", "无法找到 ${it.name} 炮塔。")
            } else {
                view.setCanDrag(use)
            }
            LogCat.d("炮塔管理器", "${it.name} 可用${use}")
        }
    }

    /**
     * 安装单个炮塔到视图
     * @param frameLayout FrameLayout
     * @param sourceFile SourceFile
     */
    fun installerTurret(
        viewGroup: ViewGroup,
        turretData: TurretData,
        turretSketchpadView: TurretSketchpadView
    ) {
        val turretView = TurretView(viewGroup.context)
        turretView.setTurretData(turretData)
        turretView.setCoordinateChangeListener(coordinateChangeListener)
        turretView.setTurretSketchpadView(turretSketchpadView)
        viewMap[turretData.name] = turretView
        viewGroup.addView(turretView)
    }


}