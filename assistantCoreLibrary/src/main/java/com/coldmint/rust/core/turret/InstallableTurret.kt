package com.coldmint.rust.core.turret

import java.io.File

/**
 * 可安装的炮塔数据类
 * @property name String
 * @constructor
 */
data class InstallableTurret(val name: String, var x: Int, var y: Int, var imageFile: File? = null)