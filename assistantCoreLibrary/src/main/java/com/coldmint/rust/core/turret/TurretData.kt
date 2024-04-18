package com.coldmint.rust.core.turret

import java.io.File

/**
 * 炮塔数据类
 * @property name String
 * @constructor
 */
data class TurretData(
        val name: String,
        var gameCoordinateData: CoordinateData,
        var scaleValue: Float = 1f,
        var imageFile: File? = null,
        var isImage: Boolean = true
)