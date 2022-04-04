package com.coldmint.rust.pro.interfaces

/**
 * 列表项目事件装载器
 */
interface ItemEvent<ViewBindingType, ViewHolderType, DataType> {

    fun loadEvent(
        index: Int,
        viewBinding: ViewBindingType,
        viewHolder: ViewHolderType,
        data: DataType
    )

}
