package com.coldmint.rust.pro.base

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.CaseMap
import android.os.Handler
import android.os.Looper
import android.renderscript.Element
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.ActivityWebModInfoBinding
import com.coldmint.rust.pro.interfaces.ItemChangeEvent
import com.coldmint.rust.pro.interfaces.ItemEvent
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AlertDialog
import androidx.core.text.toSpannable
import com.google.android.material.dialog.MaterialAlertDialogBuilder


abstract class BaseAdapter<ViewBindingType : ViewBinding, DataType>(
    private val context: Context,
    private var dataList: MutableList<DataType>
) :
    RecyclerView.Adapter<BaseAdapter.ViewHolder<ViewBindingType>>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var itemEvent: ItemEvent<ViewBindingType, ViewHolder<ViewBindingType>, DataType>? = null
    private var itemChangeEvent: ItemChangeEvent<DataType>? = null
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder()
    private val colorSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#2196F3"))
    private val bold = StyleSpan(Typeface.BOLD)



    /**
     * 建立搜索标题，注意当[BaseAdapter.keyWord]为空时永远返回null
     * @param title String 标题
     * @return 彩色标题
     */
    fun createSpannableString(title: String, keyWord: String): SpannableStringBuilder? {
        val startIndex = title.indexOf(keyWord)
        if (startIndex > -1) {
            spannableStringBuilder.clear()
            spannableStringBuilder.append(title)
            spannableStringBuilder.setSpan(
                colorSpan,
                startIndex,
                startIndex + keyWord.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.setSpan(
                bold,
                startIndex,
                startIndex + keyWord.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            return spannableStringBuilder
        } else {
            return spannableStringBuilder.append(title)
        }
    }

    /**
     * 项目改变类型
     */
    enum class ChangeType {
        Add, Remove, Replace, Disable, Enable
    }

    /**
     * 设置数据列表
     * @param dataList MutableList<DataType>
     */
    fun setNewDataList(dataList: MutableList<DataType>) {
        this.dataList = dataList
    }

    /**
     * 设置项目事件加载器
     * @param itemEvent ItemEvent<ViewBindingType, DataType>
     */
    fun setItemEvent(itemEvent: ((Int, ViewBindingType, ViewHolder<ViewBindingType>, DataType) -> Unit)?) {
        if (itemEvent == null) {
            this.itemEvent = null
        } else {
            this.itemEvent =
                object : ItemEvent<ViewBindingType, ViewHolder<ViewBindingType>, DataType> {
                    override fun loadEvent(
                        index: Int,
                        viewBinding: ViewBindingType,
                        viewHolder: ViewHolder<ViewBindingType>,
                        data: DataType
                    ) {
                        itemEvent.invoke(index, viewBinding, viewHolder, data)
                    }

                }
        }
    }

    /**
     * 设置项目改变事件(当调用[BaseAdapter.addItem]，[BaseAdapter.removeItem]，[BaseAdapter.replaceItem]这3个方法任意一个，触发监听事件)注意调用[BaseAdapter.replaceItem]方法时，data为旧的项目数据
     * @param changeEvent Function3<Int, DataType, Int, Unit>
     */
    fun setItemChangeEvent(changeEvent: ((ChangeType, Int, DataType, Int) -> Unit)?) {
        if (changeEvent == null) {
            this.itemChangeEvent = null
        } else {
            this.itemChangeEvent = object : ItemChangeEvent<DataType> {
                override fun onChanged(type: ChangeType, index: Int, data: DataType, size: Int) {
                    changeEvent.invoke(type, index, data, size)
                }

            }
        }
    }

    /**
     * 移除菜单项目
     * @param index Int
     */
    fun removeItem(index: Int) {
        val data = dataList[index]
        dataList.removeAt(index)
        handler.post {
            notifyItemRemoved(index)
            itemChangeEvent?.onChanged(ChangeType.Remove, index, data, dataList.size)
        }
    }


    /**
     * 显示删除项目对话框
     * @param name String 名称
     * @param index Int 位置
     * @param onClickPositiveButton Function0<Boolean>? 当点击删除按钮时，返回true则删除
     * @param cancelable Boolean 是否可取消
     * @param checkBoxPrompt 选择框显示的文本
     */
    @Deprecated("已废弃")
    fun showDeleteItemDialog(
        name: String,
        index: Int,
        onClickPositiveButton: ((Int, Boolean) -> Boolean)? = null,
        cancelable: Boolean = false,
        checkBoxPrompt: String? = null
    ) {
//        var checked = false
//        val dialog = MaterialAlertDialogBuilder(context)
//        if (checkBoxPrompt != null) {
//            dialog.checkBoxPrompt(text = checkBoxPrompt, onToggle = {
//                checked = it
//            })
//        }
//        dialog.title(R.string.delete_title).message(
//            text = String.format(
//                context.getString(R.string.delete_prompt),
//                name
//            )
//        ).positiveButton(R.string.dialog_ok).positiveButton {
//            if (onClickPositiveButton == null) {
//                removeItem(index)
//            } else {
//                if (onClickPositiveButton.invoke(dialog, checked)) {
//                    removeItem(index)
//                }
//            }
//        }.negativeButton(R.string.dialog_cancel).cancelable(cancelable)
//        handler.post {
//            dialog.show()
//        }
//        return dialog
    }

    /**
     * 添加项目
     * @param data DataType
     * @param index Int
     */
    fun addItem(data: DataType, index: Int = dataList.size) {
        dataList.add(index, data)
        handler.post {
            notifyItemChanged(index)
            itemChangeEvent?.onChanged(ChangeType.Add, index, data, dataList.size)
        }
    }


    /**
     * 替换项目
     * @param data DataType
     * @param index Int
     */
    fun replaceItem(data: DataType, index: Int) {
        val oldData = dataList[index]
        dataList.removeAt(index)
        dataList.add(index, data)
        handler.post {
            notifyItemChanged(index)
            itemChangeEvent?.onChanged(ChangeType.Replace, index, oldData, dataList.size)
        }
    }

    abstract fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBindingType

    abstract fun onBingView(
        data: DataType,
        viewBinding: ViewBindingType,
        viewHolder: ViewHolder<ViewBindingType>,
        position: Int
    )

    class ViewHolder<T : ViewBinding>(val viewBinding: T) :
        RecyclerView.ViewHolder(viewBinding.root) {
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<ViewBindingType> {
        return ViewHolder(getViewBindingObject(layoutInflater, parent, viewType))
    }

    override fun onBindViewHolder(holder: ViewHolder<ViewBindingType>, position: Int) {
        val data: DataType = dataList[position]
        val viewBinding: ViewBindingType = holder.viewBinding
        itemEvent?.loadEvent(position, viewBinding, holder, data)
        onBingView(data, viewBinding, holder, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}