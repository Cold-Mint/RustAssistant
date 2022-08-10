package com.coldmint.rust.pro.adapters

import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Context
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.Bookmark
import com.coldmint.rust.pro.databinding.AttachFileItemBinding

class BookmarkAdapter( context: Context, dataList: MutableList<Bookmark>) :
    BaseAdapter<AttachFileItemBinding, Bookmark>(context, dataList) {

    /**
     * 设置数据集合
     *
     * @param dataList 数据集合
     */
    fun setArrayList(dataList: MutableList<Bookmark>) {
        this.dataList = dataList
    }


    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): AttachFileItemBinding {
        return AttachFileItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: Bookmark,
        viewBinding: AttachFileItemBinding,
        viewHolder: ViewHolder<AttachFileItemBinding>,
        position: Int
    ) {
        viewBinding.fileName.text = data.name
        viewBinding.filePath.text = data.path
//        holder.del.setOnClickListener {
//            if (bookmarkListener != null) {
//                if (bookmarkListener!!.onClickRemoveButton(holder.del, bookmark)) {
//                    arrayList.remove(bookmark)
//                    notifyItemRemoved(position)
//                    bookmarkListener!!.afterRemoveItem(arrayList.size)
//                }
//            }
//        }
    }
}