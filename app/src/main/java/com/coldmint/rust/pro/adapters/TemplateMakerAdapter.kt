package com.coldmint.rust.pro.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import com.coldmint.rust.pro.TemplateMakerActivity.CodeData
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.coldmint.rust.pro.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.AddActionDialogBinding
import com.coldmint.rust.pro.databinding.MarkItemBinding
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.ArrayList


//模板制作适配器
class TemplateMakerAdapter( context: Context, dataList: MutableList<CodeData>) :
    BaseAdapter<MarkItemBinding, CodeData>(context, dataList) {
    private var tags = ArrayList<String>()

    //活动数组
    private var actionArray: JSONArray = JSONArray()

    /**
     * 设置活动数组
     * @param jsonArray JSONArray
     */
    fun setActionArray(jsonArray: JSONArray) {
        val len = jsonArray.length() - 1
        tags.clear()
        if (len > -1) {
            for (i in 0..len) {
                val json = jsonArray.getJSONObject(i)
                tags.add(json.getString("tag"))
            }
        }
        actionArray = jsonArray
    }

    fun getActionArray(): JSONArray {
        return actionArray
    }




    fun noteDialog(code: String) {
        val addActionDialogBinding =
            AddActionDialogBinding.inflate(LayoutInflater.from(context))
        val data = arrayOf(
            this.context.getString(R.string.comment_type),
        )
        val value = arrayOf("Comment")
        val adapter =
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line,
                data
            )
        addActionDialogBinding.typeSpinner.adapter = adapter
        addActionDialogBinding.modNameEdit.setText(code)
        addActionDialogBinding.modNameInputLayout.setHint(R.string.html_text)
        addActionDialogBinding.modNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addActionDialogBinding.modNameInputLayout.helperText = Html.fromHtml(
                        text,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    addActionDialogBinding.modNameInputLayout.helperText =
                        context.getString(R.string.html_text_error)
                }
            }

        })
        addActionDialogBinding.modActionLayout.setHint(R.string.plain_text)
        addActionDialogBinding.modActionLayout.helperText =
            context.getString(R.string.plain_text_tip)

        addActionDialogBinding.modActionLayout.isVisible = true
        addActionDialogBinding.modActionLayout.editText?.setText(code)
        val materialDialog =
            MaterialDialog(context).noAutoDismiss().title(R.string.set_note)
                .customView(view = addActionDialogBinding.root)
                .positiveButton(R.string.dialog_ok)
                .negativeButton(R.string.dialog_cancel)
        materialDialog.positiveButton {
            val jsonObject = JSONObject()
            jsonObject.put("type", "comment")
            jsonObject.put("htmlData", addActionDialogBinding.modNameEdit.text.toString())
            jsonObject.put("plainData", addActionDialogBinding.modAction.text.toString())
            actionArray.put(jsonObject)
            materialDialog.dismiss()
        }
        materialDialog.negativeButton {
            materialDialog.dismiss()
        }
        materialDialog.show()
    }

    /**
     * 构建代码对话框
     * @param key String
     * @param tag String
     * @param codeData CodeData
     * @param holder ViewHolder
     * @param oldJson JSONObject?
     * @param oldJsonIndex Int
     */
    fun codeDialog(
        key: String,
        tag: String,
        codeData: CodeData,
        holder: MarkItemBinding,
        oldJson: JSONObject? = null,
        oldJsonIndex: Int = -1
    ) {
        val addActionDialogBinding =
            AddActionDialogBinding.inflate(LayoutInflater.from(context))
        val title: Int
        if (oldJson == null) {
            addActionDialogBinding.modNameEdit.setText(key)
            title = R.string.set_action
        } else {
            addActionDialogBinding.modNameEdit.setText(oldJson.getString("name"))
            title = R.string.edit_action
        }

        val data = arrayOf(
            this.context.getString(R.string.input_type),
            this.context.getString(R.string.value_selector_type)
        )
        val value = arrayOf("input", "valueSelector")

        val adapter =
            ArrayAdapter(
                this.context,
                android.R.layout.simple_dropdown_item_1line,
                data
            )
        addActionDialogBinding.typeSpinner.adapter = adapter
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = data[position]
                if (item == context.getString(R.string.value_selector_type)) {
                    addActionDialogBinding.modActionLayout.isVisible = true
                    addActionDialogBinding.modActionLayout.helperText =
                        context.getString(R.string.value_selector_array_describe)
                } else {
                    addActionDialogBinding.modActionLayout.isVisible = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        addActionDialogBinding.typeSpinner.onItemSelectedListener = listener
        val materialDialog =
            MaterialDialog(context).noAutoDismiss().title(title)
                .customView(view = addActionDialogBinding.root)
                .positiveButton(R.string.dialog_ok)
                .negativeButton(R.string.dialog_cancel)
        if (oldJsonIndex > -1) {
            materialDialog.neutralButton(R.string.remove)
            materialDialog.neutralButton {
                actionArray.remove(oldJsonIndex)
                holder.actionView.setText(R.string.set_action)
                materialDialog.dismiss()
            }
        }
        materialDialog.positiveButton {
            val jsonObject = JSONObject()
            val type = value[addActionDialogBinding.typeSpinner.selectedItemPosition]
            val name = addActionDialogBinding.modNameEdit.text
            if (name == null || name.isBlank()) {
                setErrorAndInput(
                    context,
                    addActionDialogBinding.modNameEdit,
                    context.getString(R.string.action_name_error)
                )
                return@positiveButton
            }
            val key = codeData.code.substring(0, codeData.code.indexOf(":"))
            jsonObject.put("name", name)
            jsonObject.put("key", key)

            if (codeData.section != null) {
                jsonObject.put("section", codeData.section)
            }

            if (type == "valueSelector") {
                val data = addActionDialogBinding.modAction.text
                if (data == null || data.isEmpty()) {
                    setErrorAndInput(
                        context,
                        addActionDialogBinding.modAction,
                        context.getString(R.string.value_selector_type_error)
                    )
                    return@positiveButton
                } else {
                    val itemList: StringBuilder = StringBuilder()
                    val dataList: StringBuilder = StringBuilder()
                    val lineParser = LineParser(data)
                    lineParser.symbol = ","
                    lineParser.analyse { lineNum, lineData, isEnd ->
                        splitProject(
                            itemData = lineData,
                            itemList = itemList,
                            dataList = dataList
                        )
                        if (!isEnd) {
                            itemList.append(',')
                            if (!dataList.isBlank()) {
                                dataList.append(',')
                            }
                        }
                        true
                    }
                    jsonObject.put("itemList", itemList)
                    if (!dataList.isBlank()) {
                        jsonObject.put("dataList", dataList)
                    }
                }
            }
            jsonObject.put("type", type)
            jsonObject.put("tag", tag)
            tags.add(tag)
            if (oldJsonIndex > -1) {
                actionArray.remove(oldJsonIndex)
            }
            actionArray.put(jsonObject)
            holder.actionView.setText(R.string.edit_action)
            materialDialog.dismiss()
        }

        materialDialog.negativeButton {
            materialDialog.dismiss()
        }
        materialDialog.show()
    }


    /**
     * 根据标签返回json对象
     * @param tag String 标签
     * @return Int 获取失败返回-1
     */
    private fun findJsonByTag(tag: String): Int {
        val len = actionArray.length() - 1
        if (len > -1) {
            for (i in 0..len) {
                val json = actionArray.getJSONObject(i)
                val thisTag = json.getString("tag")
                if (thisTag == tag) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * 处理输入项目
     * @param itemList 项目数组
     * @param dataList 数据数组
     * @param itemData 项目
     */
    private fun splitProject(itemList: StringBuilder, dataList: StringBuilder, itemData: String) {
        if (itemData.startsWith("[") && itemData.endsWith("]")) {
            val index = itemData.indexOf(":")
            if (index > -1) {
                val key = itemData.substring(1, index)
                val value = itemData.substring(index + 1, itemData.length - 1)
                itemList.append(key)
                dataList.append(value)
            } else {
                itemList.append(itemData)
            }
        } else {
            itemList.append(itemData)
        }
    }


    /**
     * 设置错误信息，并转到其编辑框
     *
     * @param editText 编辑框
     * @param str      错误信息
     */
    private fun setErrorAndInput(context: Context, editText: EditText, str: CharSequence?) {

        if (!editText.hasFocus()) {
            editText.requestFocus()
        }
        editText.selectAll()
        editText.error = str
        val inputMethodManager =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }




    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): MarkItemBinding {
        return MarkItemBinding.inflate(layoutInflater,parent,false)
    }

    override fun onBingView(
        codeData: CodeData,
        viewBinding: MarkItemBinding,
        viewHolder: BaseAdapter.ViewHolder<MarkItemBinding>,
        position: Int
    ) {
        var s = codeData.code.trim { it <= ' ' }
        val code = s
        if (s.length > 12) {
            s = s.substring(0, 12) + "..."
        }
        viewBinding.codeView.text = s
        if (code.startsWith("#")) {
            viewBinding.actionView.isVisible = true
            viewBinding.actionView.setText(R.string.set_note)
//            val tag = code
            viewBinding.actionView.setOnClickListener {
                noteDialog(code.substring(1))
            }
        } else if (code.startsWith("[") && code.endsWith("]")) {
            viewBinding.actionView.isVisible = false
        } else if (code.isEmpty()) {
            viewBinding.actionView.isVisible = false
        } else if (code.contains(":")) {
            val symbolPosition = codeData.code.indexOf(":")
            val key = codeData.code.substring(0, symbolPosition)
            val tag = key + "-" + codeData.section
            if (!tags.contains(tag)) {
                viewBinding.actionView.setText(R.string.set_action)
            } else {
                viewBinding.actionView.setText(R.string.edit_action)
            }
            viewBinding.actionView.isVisible = true
            viewBinding.actionView.setOnClickListener { v: View ->
                val type = viewBinding.actionView.text.toString()
                var oldJsonIndex: Int = -1
                var oldJson: JSONObject? = null
                if (type == context.getString(R.string.edit_action)) {
                    oldJsonIndex = findJsonByTag(tag)
                    if (oldJsonIndex > -1) {
                        oldJson = actionArray.getJSONObject(oldJsonIndex)
                    }
                }
                codeDialog(key, tag, codeData, viewBinding, oldJson, oldJsonIndex)
            }

        } else {
            viewBinding.actionView.isVisible = false
        }
    }

}