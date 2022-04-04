package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import android.view.ViewGroup

class TemplateMakerPagerAdapter(
    private val context: Context,
    private val titleId: IntArray,
    private val views: Array<ViewGroup>
) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(views[position])
        return views[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(views[position])
    }

    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.getText(titleId[position])
    }
}