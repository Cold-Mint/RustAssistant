package com.coldmint.rust.pro.interfaces

import org.json.JSONObject

/**
 * @author Cold Mint
 * @date 2021/11/11 15:01
 */
interface TemplateMakerAdapterListener {

    fun inputComplete(tag : String,data : JSONObject)

}