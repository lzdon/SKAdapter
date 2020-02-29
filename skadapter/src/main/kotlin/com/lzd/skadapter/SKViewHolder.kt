@file:JvmName("BaseViewHolder")

package com.lzd.skadapter

import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.reflect.ParameterizedType

/**
 * @author liuzundong
 * @version 1.0
 * @since: 2018/9/6 下午4:02
 */

typealias Bind<T> = RecyclerView.ViewHolder.(T) -> Unit

typealias ViewFactory = (ViewGroup) -> View

fun ViewGroup.inflateItemView(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(this.context).inflate(layoutRes, this, false)
}

/**
 * 返回一个[SKViewHolderFactory]对象，用于创建[SKViewHolder]
 * @param viewFactory 创建视图的工厂，没有layoutRes时可以传入viewFactory创建视图
 * @param bindFun 数据绑定方法，绑定视图时回调
 */
inline fun <reified T : Any> ofSKHolderFactory(noinline viewFactory: ViewFactory, noinline bindFun: Bind<T>): SKViewHolderFactory<T> {
    return object : SKViewHolderFactory<T>() {

        override fun createViewHolder(parent: ViewGroup): SKViewHolder<T> {
            return object : SKViewHolder<T>(viewFactory(parent)) {
                override fun onBind(item: T) {
                    bindFun.invoke(this, item)
                }
            }
        }
    }
}

/**
 * 返回一个[SKViewHolderFactory]对象，用于创建[SKViewHolder]
 * @param layoutRes 视图Res
 * @param bindFun 数据绑定方法，绑定视图时回调
 */
inline fun <reified T : Any> ofSKHolderFactory(@LayoutRes layoutRes: Int, noinline bindFun: Bind<T>): SKViewHolderFactory<T> {
    return object : SKViewHolderFactory<T>() {

        override fun createViewHolder(parent: ViewGroup): SKViewHolder<T> {
            return object : SKViewHolder<T>(parent.inflateItemView(layoutRes)) {
                override fun onBind(item: T) {
                    bindFun.invoke(this, item)
                }
            }
        }
    }
}

abstract class SKViewHolderFactory<T : Any> {
    @Suppress("UNCHECKED_CAST")
    internal val itemType: Class<T>
        get() = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    abstract fun createViewHolder(parent: ViewGroup): SKViewHolder<T>
}

abstract class SKViewHolder<T : Any>(view: View) : RecyclerView.ViewHolder(view) {

    lateinit var item: T
        private set
    var isAttached = false
        private set

    internal fun bind(item: T) {
        this.item = item
        this.onBind(item)
    }

    abstract fun onBind(item: T)

    open fun onViewRecycled() {}

    @CallSuper
    open fun onViewDetachedFromWindow() {
        isAttached = false
    }

    @CallSuper
    open fun onViewAttachedToWindow() {
        isAttached = true
    }
}
