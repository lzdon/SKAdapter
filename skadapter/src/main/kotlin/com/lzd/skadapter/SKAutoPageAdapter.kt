package com.lzd.skadapter

import android.view.View
import android.view.ViewGroup
import com.lzd.skadapter.SKAutoPageAdapter.Companion.sDefaultFooterFactory
import kotlinx.android.synthetic.main.item_layout_auto_pager_footer.view.*
import kotlin.math.max

/**
 * 支持自动分页加载的Adapter
 * 如需定制布局，修改 [sDefaultFooterFactory]，或通过构造方法注入
 * @author liuzundong
 * @version 1.0
 * @since: 2018/10/11 下午3:02
 */
open class SKAutoPageAdapter(
    footerViewFactory: SKViewHolderFactory<PageFooterItem> = sDefaultFooterFactory,
    emptyViewFactory: SKViewHolderFactory<EmptyViewData> = sDefaultEmptyFactory,
    errorViewFactory: SKViewHolderFactory<ErrorViewData> = sDefaultErrorFactory,
    loadingViewFactory: SKViewHolderFactory<LoadingViewData> = sDefaultLoadingFactory
) : SKPlaceHolderAdapter(emptyViewFactory, errorViewFactory, loadingViewFactory) {

    companion object {
        @JvmStatic
        var sDefaultFooterFactory: SKViewHolderFactory<PageFooterItem> = FooterViewHolder.Factory()
    }

    @Suppress("LeakingThis")
    private val footerItem = PageFooterItem(this::retry)

    private var page = 1
    private var isLoadingData = false

    /**
     * 触发加载下一页的阈值，调大可以提前加载下一页
     */
    var loadThreshold = 0
        set(value) {
            field = max(0, value)
        }

    /**
     * 是否显示底部提示
     */
    var showPageFooter = true

    var onLoadNextPage: LoadNextFun? = null

    private fun retry() {
        onLoadNextPage?.invoke(page)
    }

    private fun tryNextPage() {
        if (!isLoadingData) {
            isLoadingData = true
            ++page
            onLoadNextPage?.invoke(page)
        }
    }

    init {
        register(footerViewFactory)
    }

    override fun onBindViewHolder(holder: SKViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (footerItem.state == PageFooterItem.STATE_LOADING && holder.adapterPosition >= itemCount - 1 - loadThreshold) {
            tryNextPage()
        }
    }

    /**
     * 当加载数据失败的时候手动调用这个方法
     */
    fun onLoadPageDataFailed() {
        isLoadingData = false
        val index = indexOf(footerItem)
        if (indexOf(footerItem) >= 0) {
            footerItem.state = PageFooterItem.STATE_FAILED
            notifyItemChanged(index)
        }
    }

    /**
     * 当需要从头开始加载的时候，调用这个方法
     */
    fun setPageItems(items: List<Any>?, hasMore: Boolean) {
        if (items == null || items.isEmpty()) {
            return
        }
        super.setItems(items)
        page = 1
        isLoadingData = false
        if (hasMore) {
            footerItem.state = PageFooterItem.STATE_LOADING
        } else {
            footerItem.state = PageFooterItem.STATE_END
        }
        if (showPageFooter && indexOf(footerItem) < 0) {
            super.appendItem(footerItem, true)
        }
    }

    /**
     * 追加分页数据
     */
    fun appendPageItems(items: List<Any>?, hasMore: Boolean) {
        isLoadingData = false
        if (hasMore) {
            footerItem.state = PageFooterItem.STATE_LOADING
        } else {
            footerItem.state = PageFooterItem.STATE_END
        }
        val index = indexOf(footerItem)
        if (showPageFooter) {
            if (index < 0) {
                super.appendItems(items, true)
                super.appendItem(footerItem, true)
            } else {
                super.insertItems(items, index, true)
                super.notifyItemChanged(indexOf(footerItem))
            }
        } else {
            if (index >= 0) {
                removeItem(footerItem, true)
            }
            super.appendItems(items, true)
        }
    }

    override fun showEmptyView(retryFun: RetryFun?) {
        super.showEmptyView(retryFun)
        isLoadingData = false
        footerItem.state = PageFooterItem.STATE_END
    }

    override fun showErrorView(retryFun: RetryFun?) {
        super.showErrorView(retryFun)
        isLoadingData = false
        footerItem.state = PageFooterItem.STATE_END
    }
}

typealias LoadNextFun = (Int) -> Unit

class PageFooterItem(val retryFun: RetryFun? = null) {
    companion object {
        const val STATE_LOADING = 1
        const val STATE_FAILED = 2
        const val STATE_END = 3
    }

    var state = STATE_LOADING
}

class FooterViewHolder(itemView: View) : SKViewHolder<PageFooterItem>(itemView) {

    init {
        itemView.text_failed.visibility = View.GONE
        itemView.text_end.visibility = View.GONE
        itemView.text_failed.setOnClickListener {
            item.retryFun?.invoke()
        }
    }

    override fun onBind(item: PageFooterItem) {
        when (item.state) {
            PageFooterItem.STATE_LOADING -> {
                itemView.group_loading.visibility = View.VISIBLE
                itemView.text_failed.visibility = View.GONE
                itemView.text_end.visibility = View.GONE
            }
            PageFooterItem.STATE_FAILED -> {
                itemView.group_loading.visibility = View.GONE
                itemView.text_end.visibility = View.GONE
                itemView.text_failed.visibility = View.VISIBLE
            }
            PageFooterItem.STATE_END -> {
                itemView.group_loading.visibility = View.GONE
                itemView.text_failed.visibility = View.GONE
                itemView.text_end.visibility = View.VISIBLE
            }
        }
    }

    class Factory : SKViewHolderFactory<PageFooterItem>() {
        override fun createViewHolder(parent: ViewGroup): SKViewHolder<PageFooterItem> {
            return FooterViewHolder(parent.inflateItemView(R.layout.item_layout_auto_pager_footer))
        }
    }
}
