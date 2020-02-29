package com.lzd.skadapter

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.lang.reflect.Modifier
import java.util.*

/**
 * @author liuzundong
 * @version 1.0
 * @since: 2018/9/6 下午2:45
 */
open class SKRecyclerViewAdapter<T : Any> : RecyclerView.Adapter<SKViewHolder<*>>() {

    private val holderFactoryList = mutableListOf<SKViewHolderFactory<*>>()
    private val typeMap = hashMapOf<Class<out T>, Int>()
    protected val dataWrapper = AdapterDataWrapper<T>()

    /**
     * @param factories 注入进来的SKViewHolderFactory数组
     * 创建SKViewHolderFactory可以使用[ofSKHolderFactory]工厂方法，或自行实现[SKViewHolderFactory]
     */
    fun register(vararg factories: SKViewHolderFactory<out T>) {
        holderFactoryList.addAll(factories.toList().apply {
            forEachIndexed { index, holderFactory ->
                val itemType = holderFactory.itemType.apply { checkItemType(this) }
                val previous = typeMap.put(itemType, index + holderFactoryList.size)
                if (previous != null) {
                    throw IllegalStateException("${holderFactory.itemType} related to " +
                        "${holderFactory.javaClass}" + " was registered already")
                }
            }
        })
    }

    private fun checkItemType(itemType: Class<out T>) {
        if (itemType.isInterface) {
            throw IllegalStateException("interface is not supported here: $itemType")
        }
        if (Modifier.isAbstract(itemType.modifiers)) {
            throw IllegalStateException("abstract class is not supported here: $itemType")
        }
        if (itemType.isArray) {
            throw IllegalStateException("array is not supported here: $itemType")
        }
        if (Map::class.java.isAssignableFrom(itemType) || Iterable::class.java.isAssignableFrom(itemType)) {
            throw IllegalStateException("map or iterable is not supported here: $itemType")
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SKViewHolder<*> {
        val holderFactory = holderFactoryList[viewType]
        return holderFactory.createViewHolder(parent)
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: SKViewHolder<*>, position: Int) {
        val item = getItem(position) ?: return
        (holder as? SKViewHolder<T>)?.bind(item)
    }

    final override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
            ?: throw IllegalStateException("invalid position: $position")

        return typeMap[item::class.java]
            ?: throw NoSuchElementException("${item.javaClass} was not registered")
    }

    final override fun onViewAttachedToWindow(holder: SKViewHolder<*>) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
    }

    final override fun onViewDetachedFromWindow(holder: SKViewHolder<*>) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
    }

    final override fun onViewRecycled(holder: SKViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    final override fun getItemCount(): Int {
        return dataWrapper.getItemCount()
    }

    /**
     * 设置数据源，并立即刷新视图
     * @param items 数据源
     */
    open fun setItems(items: List<T>?) {
        dataWrapper.setItems(items)
        notifyDataSetChanged()
    }

    /**
     * 追加数据，多用于分页加载
     * @param items 追加的数据
     * @param needNotify 是否刷新视图
     */
    @JvmOverloads
    fun appendItems(items: List<T>?, needNotify: Boolean = true) {
        if (items?.isNotEmpty() == true) {
            val startPosition = dataWrapper.getItemCount()
            dataWrapper.appendItems(items)
            if (needNotify) notifyItemRangeChanged(startPosition, items.size)
        }
    }

    /**
     * 获取clz或clz子类的list
     * @param clz java class
     */
    fun <D : T> getItems(clz: Class<D>): List<D> {
        return dataWrapper.getItems(clz)
    }

    fun getItem(position: Int): T? {
        return dataWrapper.getItem(position)
    }

    fun indexOf(item: T): Int {
        return dataWrapper.indexOf(item)
    }

    fun indexOfFirst(itemClz: Class<out T>): Int {
        return dataWrapper.indexOfFirst(itemClz)
    }

    fun exist(itemClz: Class<out T>): Boolean {
        return indexOfFirst(itemClz) >= 0
    }

    fun swap(i: Int, j: Int) {
        dataWrapper.swap(i, j)
    }

    fun clear() {
        dataWrapper.clear()
    }

    fun updateRange(position: Int, list: List<T>?) {
        list?.run {
            forEachIndexed { index, t -> dataWrapper.update(index + position, t) }
            notifyItemRangeChanged(position, size)
        }
    }

    fun update(position: Int, item: T?) {
        item ?: return
        dataWrapper.update(position, item)
        notifyItemChanged(position)
    }

    @JvmOverloads
    fun appendItem(item: T?, needNotify: Boolean = true) {
        item ?: return
        val startPosition = dataWrapper.getItemCount()
        dataWrapper.appendItem(item)
        if (needNotify) notifyItemChanged(startPosition)
    }

    @JvmOverloads
    fun insertItem(item: T?, position: Int, needNotify: Boolean = true) {
        if (item != null && dataWrapper.insertItem(item, position) && needNotify) {
            notifyItemInserted(position)
        }
    }

    @JvmOverloads
    fun insertItems(items: List<T>?, position: Int, needNotify: Boolean = true) {
        items?.run {
            if (dataWrapper.insertItems(this, position) && needNotify) {
                notifyItemRangeInserted(position, items.size)
            }
        }
    }

    @JvmOverloads
    fun removeItemAt(position: Int, needNotify: Boolean = true) {
        if (dataWrapper.removeItem(position) && needNotify) {
            notifyItemRemoved(position)
        }
    }

    @JvmOverloads
    fun removeItem(item: T, needNotify: Boolean = true) {
        removeItemAt(indexOf(item), needNotify)
    }

    @JvmOverloads
    fun removeItems(position: Int, count: Int, needNotify: Boolean = true) {
        val deleteCount = dataWrapper.removeItems(position, count)
        if (deleteCount > 0 && needNotify) {
            notifyItemRangeRemoved(position, deleteCount)
        }
    }
}

/**
 * @author wangfangbing
 * @version 1.0
 * @since: 2018/8/30 上午10:38
 */
class AdapterDataWrapper<T : Any> {
    private val dataList = ArrayList<T>()

    fun getItem(position: Int): T? {
        if (position < 0 || position >= dataList.size) {
            return null
        }
        return dataList[position]
    }

    @Suppress("UNCHECKED_CAST")
    fun <D> getItems(clz: Class<D>): List<D> {
        return dataList.filter { clz.isAssignableFrom(it.javaClass) } as List<D>
    }

    fun getItemCount(): Int {
        return dataList.size
    }

    fun indexOf(item: T): Int {
        return dataList.indexOf(item)
    }

    fun indexOfFirst(clazz: Class<out T>): Int {
        return dataList.indexOfFirst { it.javaClass == clazz }
    }

    fun swap(i: Int, j: Int) {
        Collections.swap(dataList, i, j)
    }

    fun update(position: Int, item: T) {
        dataList[position] = item
    }

    fun setItems(items: List<T>?) {
        dataList.clear()
        if (items != null) {
            dataList.addAll(items)
        }
    }

    fun appendItem(item: T) {
        dataList.add(item)
    }

    fun appendItems(items: List<T>) {
        dataList.addAll(items)
    }

    fun clear() = dataList.clear()

    fun insertItem(item: T, position: Int): Boolean {
        if (position >= 0 && position <= dataList.size) {
            dataList.add(position, item)
            return true
        }
        return false
    }

    fun insertItems(items: List<T>, position: Int): Boolean {
        if (position >= 0 && position <= dataList.size) {
            return dataList.addAll(position, items)
        }
        return false
    }

    fun removeItem(position: Int): Boolean {
        if (position >= 0 && position < dataList.size) {
            dataList.removeAt(position)
            return true
        }
        return false
    }

    /**
     * @return the actual count deleted
     */
    fun removeItems(startPosition: Int, count: Int): Int {
        if (startPosition >= 0 && startPosition < dataList.size) {
            var deleteCount = Math.min(count, dataList.size - startPosition)
            for (index in 0 until deleteCount) {
                dataList.removeAt(startPosition)
            }
            return deleteCount
        }
        return 0
    }
}
