package com.coldmint.rust.core.dataBean.iflynote

data class NoteData(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val headPhoto: String,
        val level: String,
        val nickName: String,
        val note: Note,
        val passwordStatus: Boolean,
        val pv: Int,
        val time: String,
        val uid: Long,
        val words: Int
    ) {
        data class Note(
            val collection: Boolean,
            val contentType: Int,
            val createTime: Long,
            val isShared: Boolean,
            val label: String,
            val location: String,
            val nid: String,
            val noteVersion: String,
            val plain: String,
            val syncState: String,
            val syntime: Long,
            val tagId: Long,
            val text: String,
            val time: Long,
            val title: String,
            val top: Boolean,
            val type: Int,
            val uid: Long
        )
    }
}