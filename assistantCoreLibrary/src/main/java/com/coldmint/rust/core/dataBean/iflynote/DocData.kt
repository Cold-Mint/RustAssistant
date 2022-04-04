package com.coldmint.rust.core.dataBean.iflynote

class DocData {
    /**
     * code : 0
     * message : success
     * data : {"fid":"1k5Ug","owner":1612406406675,"passwordStatus":false,"docType":"note","digest":"测试\n{\n\"键\":\"值\",\n\"啦啦啦\":\"测试\"\n}\n","title":"无标题 ","type":2}
     */
    var code = 0
    var message: String? = null

    /**
     * fid : 1k5Ug
     * owner : 1612406406675
     * passwordStatus : false
     * docType : note
     * digest : 测试
     * {
     * "键":"值",
     * "啦啦啦":"测试"
     * }
     * title : 无标题
     * type : 2
     */
    var data: DataBean? = null

    class DataBean {
        var fid: String? = null
        var owner: Long = 0
        var isPasswordStatus = false
        var docType: String? = null
        var digest: String? = null
        var title: String? = null
        var type = 0
    }
}