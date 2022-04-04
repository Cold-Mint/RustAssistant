package com.coldmint.rust.pro.databean

/**
 * @author Cold Mint
 * @date 2022/1/3 19:42
 */
data class ThanksDataBean(val title: String, val description: String, val qq: Long) {

    //https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=2923268971
    /**
     * 获取qq头像链接
     * @return String
     */
    fun getIconLink(): String {
        return "https://q1.qlogo.cn/g?b=qq&nk=${qq}&s=640"
    }
}