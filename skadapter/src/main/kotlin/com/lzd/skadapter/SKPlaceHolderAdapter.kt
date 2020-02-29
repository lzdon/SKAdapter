package com.lzd.skadapter

import android.support.annotation.CallSuper
import android.support.annotation.StringRes
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.ViewGroup
import com.lzd.skadapter.SKPlaceHolderAdapter.Companion.sDefaultEmptyFactory
import com.lzd.skadapter.SKPlaceHolderAdapter.Companion.sDefaultErrorFactory
import kotlinx.android.synthetic.main.item_default_place_holder.view.*

/**
 * 支持空提示、错误占位图的Adapter
 * 如需定制布局，修改 [sDefaultEmptyFactory] [sDefaultErrorFactory]，或通过构造方法注入
 * @author liuzundong
 * @version 1.0
 * @since: 2018/10/11 下午3:02
 */
open class SKPlaceHolderAdapter(
    emptyViewFactory: SKViewHolderFactory<EmptyViewData> = sDefaultEmptyFactory,
    errorViewFactory: SKViewHolderFactory<ErrorViewData> = sDefaultErrorFactory,
    loadingViewFactory: SKViewHolderFactory<LoadingViewData> = sDefaultLoadingFactory
) : SKRecyclerViewAdapter<Any>() {

    companion object {
        @JvmStatic
        var sDefaultEmptyFactory: SKViewHolderFactory<EmptyViewData> = EmptyViewHolder.Factory()

        @JvmStatic
        var sDefaultErrorFactory: SKViewHolderFactory<ErrorViewData> = ErrorViewHolder.Factory()

        @JvmStatic
        var sDefaultLoadingFactory: SKViewHolderFactory<LoadingViewData> =
            LoadingViewHolder.Factory()
    }

    init {
        register(emptyViewFactory, errorViewFactory, loadingViewFactory)
    }

    /**
     * 显示空页面，可自定义文案
     * @param textId 自定义文案，string类型资源id
     * @param retryFun 点击事件回调，例如重新发起请求。可为空
     */
    @CallSuper
    @JvmOverloads
    open fun showEmptyViewByText(@StringRes textId: Int? = null, retryFun: RetryFun? = null) {
        clear()
        appendItem(EmptyViewData(textId, retryFun), false)
        notifyDataSetChanged()
    }

    /**
     * 显示空页面
     * @param retryFun 点击事件回调，例如重新发起请求。可为空
     */
    @CallSuper
    @JvmOverloads
    open fun showEmptyView(retryFun: RetryFun? = null) {
        clear()
        appendItem(EmptyViewData(null, retryFun), false)
        notifyDataSetChanged()
    }

    /**
     * 显示错误页面，可自定义文案
     * @param textId 自定义文案，string类型资源id
     * @param retryFun 点击事件回调，例如重新发起请求。可为空
     */
    @CallSuper
    @JvmOverloads
    open fun showErrorViewByText(@StringRes textId: Int? = null, retryFun: RetryFun? = null) {
        clear()
        appendItem(ErrorViewData(textId, retryFun), false)
        notifyDataSetChanged()
    }

    /**
     * 显示错误页面
     * @param retryFun 点击事件回调，例如重新发起请求。可为空
     */
    @CallSuper
    @JvmOverloads
    open fun showErrorView(retryFun: RetryFun? = null) {
        clear()
        appendItem(ErrorViewData(null, retryFun), false)
        notifyDataSetChanged()
    }

    /**
     * 显示loading页面
     */
    fun showLoadingView() {
        clear()
        appendItem(LoadingViewData(), false)
        notifyDataSetChanged()
    }

    /**
     * 一般不用主动调用此方法，调用[setItems]时默认重置所有holderView
     */
    fun hideLoadingView() {
        removeItemAt(indexOfFirst(LoadingViewData::class.java))
    }

    /**
     * 一般不用主动调用此方法，调用[setItems]时默认重置所有holderView
     */
    fun hideErrorView() {
        removeItemAt(indexOfFirst(ErrorViewData::class.java))
    }

    /**
     * 一般不用主动调用此方法，调用[setItems]时默认重置所有holderView
     */
    fun hideEmptyView() {
        removeItemAt(indexOfFirst(ErrorViewData::class.java))
    }

    /**
     * 采用网格布局时，占位图无法占满屏幕宽度，需要对[gridLayoutManager]设置spanSizeLookup
     */
    fun fixSpanSizeLookup(gridLayoutManager: GridLayoutManager) {
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = getItem(position)
                if (item is EmptyViewData || item is ErrorViewData || item is LoadingViewData || item is PageFooterItem) {
                    return gridLayoutManager.spanCount
                }
                return 1
            }
        }
    }
}

typealias RetryFun = () -> Unit

class LoadingViewData
class EmptyViewData(@StringRes val textId: Int? = null, val retryFun: RetryFun? = null)
class ErrorViewData(@StringRes val textId: Int? = null, val retryFun: RetryFun? = null)

class LoadingViewHolder(itemView: View) : SKViewHolder<LoadingViewData>(itemView) {

    override fun onBind(item: LoadingViewData) = Unit

    class Factory : SKViewHolderFactory<LoadingViewData>() {
        override fun createViewHolder(parent: ViewGroup): SKViewHolder<LoadingViewData> {
            return LoadingViewHolder(parent.inflateItemView(R.layout.item_default_loading))
        }
    }
}

class EmptyViewHolder(itemView: View) : SKViewHolder<EmptyViewData>(itemView) {

    override fun onBind(item: EmptyViewData) {
        itemView.image.setImageResource(R.drawable.img_holder_empty)
        itemView.text.setText(item.textId ?: R.string.tips_no_data)
        itemView.setOnClickListener { item.retryFun?.invoke() }
    }

    class Factory : SKViewHolderFactory<EmptyViewData>() {
        override fun createViewHolder(parent: ViewGroup): SKViewHolder<EmptyViewData> {
            return EmptyViewHolder(parent.inflateItemView(R.layout.item_default_place_holder))
        }
    }
}

class ErrorViewHolder(itemView: View) : SKViewHolder<ErrorViewData>(itemView) {

    override fun onBind(item: ErrorViewData) {
        itemView.image.setImageResource(R.drawable.img_cry)
        itemView.text.setText(item.textId ?: R.string.tips_load_error)
        itemView.setOnClickListener { item.retryFun?.invoke() }
    }

    class Factory : SKViewHolderFactory<ErrorViewData>() {
        override fun createViewHolder(parent: ViewGroup): SKViewHolder<ErrorViewData> {
            return ErrorViewHolder(parent.inflateItemView(R.layout.item_default_place_holder))
        }
    }
}
