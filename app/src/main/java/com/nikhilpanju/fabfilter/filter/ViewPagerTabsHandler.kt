package com.nikhilpanju.fabfilter.filter

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nikhilpanju.fabfilter.R
import com.nikhilpanju.fabfilter.utils.*

@SuppressLint("WrongConstant")
class ViewPagerTabsHandler(
    private val viewPager: ViewPager2,
    private val tabsRecyclerView: RecyclerView,
    private val bottomBarCardView: CardView
) {

    private val context = viewPager.context
    private val bottomBarColor: Int by bindColor(context, R.color.bottom_bar_color)
    private val bottomBarPinkColor: Int by bindColor(context, R.color.colorAccent)

    private val transColor: Int by bindColor(context, R.color.tab_trans_color)
    private val tabColor: Int by bindColor(context, R.color.tab_unselected_color)
    private val tabSelectedColor: Int by bindColor(context, R.color.tab_selected_color)

    private val tabItemWidth: Float by bindDimen(context, R.dimen.tab_item_width)
    private val filterLayoutPadding: Float by bindDimen(context, R.dimen.filter_layout_padding)

    private val toggleAnimDuration =
        context.resources.getInteger(R.integer.toggleAnimDuration).toLong()

    private lateinit var tabsAdapter: FiltersTabsAdapter
    var hasActiveFilters = false
        private set

    fun init() {
        // ViewPager & Tabs
//        viewPager.offscreenPageLimit = FiltersLayout.numTabs
        tabsRecyclerView.updatePadding(right = (context.screenWidth - tabItemWidth - filterLayoutPadding).toInt())
        tabsRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val prevTabView = tabsRecyclerView.layoutManager?.findViewByPosition(position - 1)
                val currentTabView = tabsRecyclerView.layoutManager?.findViewByPosition(position)
                val nextTabView = tabsRecyclerView.layoutManager?.findViewByPosition(position + 1)

                val defaultScale: Float = FiltersTabsAdapter.defaultScale
                val maxScale: Float = FiltersTabsAdapter.maxScale

                prevTabView?.setScale(defaultScale)
                currentTabView?.setScale(defaultScale + (1 - positionOffset) * (maxScale - defaultScale))
                nextTabView?.setScale(defaultScale + positionOffset * (maxScale - defaultScale))

                prevTabView?.findViewById<View>(R.id.tab_pill)?.backgroundTintList =
                    ColorStateList.valueOf(
                        blendColors(
                            tabColor,
                            tabColor,
                            positionOffset
                        )
                    )
                currentTabView?.findViewById<View>(R.id.tab_pill)?.backgroundTintList =
                    ColorStateList.valueOf(
                        blendColors(
                            tabColor,
                            tabSelectedColor,
                            1 - positionOffset
                        )
                    )
                nextTabView?.findViewById<View>(R.id.tab_pill)?.backgroundTintList =
                    ColorStateList.valueOf(blendColors(tabColor, tabSelectedColor, positionOffset))
            }
        })
    }

    fun setAdapters(set: Boolean) {
        if (set) {
            viewPager.adapter = FiltersPagerAdapter(viewPager.context, ::onFilterSelected)
            tabsAdapter = FiltersTabsAdapter(viewPager.context) { clickedPosition ->
                viewPager.setCurrentItem(clickedPosition, true)
            }
            tabsRecyclerView.adapter = tabsAdapter
        } else {
            viewPager.adapter = null
            tabsRecyclerView.adapter = null
            hasActiveFilters = false
        }
    }

    private fun onFilterSelected(updatedPosition: Int, selectedMap: Map<Int, List<Int>>) {
        val hasActiveFilters = selectedMap.filterValues { it.isNotEmpty() }.isNotEmpty()
        val bottomBarAnimator =
            if (hasActiveFilters && !this.hasActiveFilters) ValueAnimator.ofFloat(0f, 1f)
            else if (!hasActiveFilters && this.hasActiveFilters) ValueAnimator.ofFloat(1f, 0f)
            else null

        tabsAdapter.updateBadge(updatedPosition, !selectedMap[updatedPosition].isNullOrEmpty())

        bottomBarAnimator?.let {
            this.hasActiveFilters = !this.hasActiveFilters
            it.addUpdateListener { animation ->
                val color = blendColors(
                    bottomBarColor,
                    bottomBarPinkColor,
                    animation.animatedValue as Float
                )
                bottomBarCardView.setCardBackgroundColor(color)
            }
            it.duration = toggleAnimDuration
            it.start()
        }
    }
}