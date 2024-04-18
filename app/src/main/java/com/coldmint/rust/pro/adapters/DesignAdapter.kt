package com.coldmint.rust.pro.adapters

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.turret.TurretData
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.ItemTabBinding
import com.muqing.VH
import com.muqing.gj

/**
 * @author Cold Mint
 * @date 2022/1/10 8:49
 */
class DesignAdapter(private val onclick: Click) : RecyclerView.Adapter<VH<ItemTabBinding>>() {

    var list: ArrayList<TurretData> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ItemTabBinding> {
        return VH(ItemTabBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    lateinit var string: TurretData

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VH<ItemTabBinding>, position: Int) {
        holder.binging.button.text = list[position].name
        gj.sc(list[position].imageFile)

        if (string == list[position]) {
            holder.binging.root.isEnabled = false
            holder.binging.root.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.material_grey_200))
        } else {
            holder.binging.root.isEnabled = true
            val typedValue = TypedValue()
            holder.itemView.context.theme.resolveAttribute(R.attr.colorPrimaryContainer, typedValue, true)
            val colorPrimary = typedValue.data
            holder.binging.root.setCardBackgroundColor(colorPrimary)

        }
        holder.binging.root.setOnClickListener {
            string = list[holder.absoluteAdapterPosition]
            notifyDataSetChanged()
            onclick.onclick(string.name)
            //todo
        }
        if (list[holder.absoluteAdapterPosition].isImage) {
            holder.binging.imageview.setImageResource(R.drawable.visibility)
        }else{
            holder.binging.imageview.setImageResource(R.drawable.visibility_off)
        }
        holder.binging.imageview.setOnClickListener{
            list[holder.absoluteAdapterPosition].isImage = !list[holder.absoluteAdapterPosition].isImage
            notifyItemChanged(holder.absoluteAdapterPosition)
            onclick.onclickImage()
        }
    }
    interface Click {
        fun onclick(str: String)
        fun onclickImage()
    }

}