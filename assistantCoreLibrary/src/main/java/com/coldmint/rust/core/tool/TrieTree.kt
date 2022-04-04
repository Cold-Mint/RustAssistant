package com.coldmint.rust.core.tool

import com.coldmint.rust.core.tool.TrieTree
import com.coldmint.rust.core.interfaces.TrieTreeSearchListener
import java.lang.StringBuilder
import java.util.HashMap

//字典树
class TrieTree {
    //树的分支
    var map = HashMap<Char, TrieTree>()

    //是某个单词的结尾
    var isEnd = false

    //行构造器
    private val stringBuilder = StringBuilder()
    /**
     * 转换为String
     *
     * @param root 根节点
     * @return String对象
     *
     * public String convertToString(TrieTree root) {
     * if (root == null) {
     * return null;
     * }
     * stringBuilder.delete(0, stringBuilder.length());
     * HashMap<Character></Character>, TrieTree> newMap = root.map;
     * Iterator<Entry></Entry><Character></Character>, TrieTree>> iterator = newMap.entrySet().iterator();
     * while (iterator.hasNext()) {
     * // 一定是  Map.Entry
     * Map.Entry<Character></Character>, TrieTree> entry = iterator.next();
     * TrieTree next = entry.getValue();
     * if (next == null) {
     * break;
     * }
     * convertToString(next, new StringBuilder(entry.getKey()));
     * }
     * return stringBuilder.toString();
     * }
     *
     *
     * private void convertToString(TrieTree root, StringBuilder lineBuilder) {
     * if (root == null) {
     * return;
     * }
     * if (root.isEnd) {
     * stringBuilder.append('\n');
     * stringBuilder.append(lineBuilder);
     * }
     * HashMap<Character></Character>, TrieTree> newMap = root.map;
     * Iterator<Entry></Entry><Character></Character>, TrieTree>> iterator = newMap.entrySet().iterator();
     * while (iterator.hasNext()) {
     * // 一定是  Map.Entry
     * Map.Entry<Character></Character>, TrieTree> entry = iterator.next();
     * TrieTree next = entry.getValue();
     * if (next == null) {
     * break;
     * }
     * lineBuilder.append(entry.getKey());
     * convertToString(next, lineBuilder);
     * }
     * }
     */
    /**
     * 插入数据
     *
     * @param root 根节点
     * @param str  字符串
     */
    fun insert(root: TrieTree?, str: String) {
        var root = root
        if (root == null || str.length == 0) {
            return
        }
        val chars = str.toCharArray()
        var next: TrieTree? = null
        for (c in chars) {
            //如果存在字符
            next = root!!.map[c]
            if (next == null) {
                next = TrieTree()
                root.map[c] = next
            }
            root = next
        }
        next!!.isEnd = true
    }

    /**
     * 是否包含每个字符串
     *
     * @param root 根节点
     * @param str  字符串
     * @return 是否包含
     */
    fun contains(root: TrieTree?, str: String): Boolean {
        var root = root
        if (root == null || str.length == 0) {
            return false
        }
        val chars = str.toCharArray()
        var next: TrieTree? = null
        for (c in chars) {
            next = root!!.map[c]
            if (next == null) {
                return false
            }
            root = next
        }
        return next!!.isEnd
    }

    /**
     * 搜索字符串
     *
     * @param root           根节点
     * @param prefix         前缀
     * @param searchListener 搜索监听器
     */
    fun search(root: TrieTree?, prefix: String, searchListener: TrieTreeSearchListener) {
        var root = root
        if (root == null || prefix.length == 0) {
            return
        }
        val chars = prefix.toCharArray()
        var next: TrieTree? = null
        for (c in chars) {
            next = root!!.map[c]
            if (next == null) {
                return
            }
            root = next
        }
        val num = next!!.map.size
        if (num > 0) {
            searchLeave(root, prefix, searchListener)
        } else {
            if (next.isEnd) {
                searchListener.findData(prefix)
            }
        }
    }

    /**
     * 搜索到叶子
     *
     * @param root           根节点
     * @param prefix         前缀
     * @param searchListener 搜索监听器
     */
    private fun searchLeave(
        root: TrieTree?,
        prefix: String,
        searchListener: TrieTreeSearchListener
    ) {
        if (root!!.isEnd) {
            searchListener.findData(prefix)
        }
        val newMap = root.map
        val iterator: Iterator<Map.Entry<Char, TrieTree>> = newMap.entries.iterator()
        while (iterator.hasNext()) {
            // 一定是  Map.Entry
            val entry = iterator.next()
            val next = entry.value ?: break
            searchLeave(next, prefix + entry.key, searchListener)
        }
    }
}