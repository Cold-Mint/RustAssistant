package com.coldmint.rust.pro.interfaces

import com.coldmint.rust.pro.base.BaseAdapter

/**
 * @author Cold Mint
 * @date 2022/1/15 18:25
 */
interface ItemChangeEvent<DataType> {

    fun onChanged(type: BaseAdapter.ChangeType, index: Int, data: DataType, size: Int)

}