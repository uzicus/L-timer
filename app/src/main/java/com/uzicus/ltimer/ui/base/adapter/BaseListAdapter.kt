package com.uzicus.ltimer.ui.base.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.uzicus.ltimer.extension.inflate
import kotlinx.android.extensions.LayoutContainer

abstract class BaseListAdapter<T, VH>(
        private val headerLayoutRes: Int? = null,
        private val footerLayoutRes: Int? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    DiffItemsCallback<T> by SimpleDiffCallback<T>()
        where VH : RecyclerView.ViewHolder,
              VH : Bindable<T> {

    companion object {
        private const val HEADER_VIEW_TYPE = 1
        private const val FOOTER_VIEW_TYPE = 2
        private const val ITEM_VIEW_TYPE = 3
    }

    private var items: MutableList<T> = mutableListOf()

    private val headerOffset: Int get() = if (headerView != null) 1 else 0
    private val footerOffset: Int get() = if (footerView != null) 1 else 0

    var headerView: View? = null
        private set(value) {
            field = value
            notifyItemInserted(0)
        }

    var footerView: View? = null
        private set(value) {
            field = value
            if (field == null) {
                notifyItemRemoved(itemCount - 1)
            } else {
                notifyItemInserted(itemCount - 1)
            }
        }

    override fun getItemCount() = items.size + headerOffset + footerOffset

    fun getItem(position: Int) = items[position - headerOffset]

    fun getItemOrNull(position: Int) = items.getOrNull(position - headerOffset)

    fun getItems() = items

    fun setItems(list: List<T>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<T>) {
        if (items.isEmpty()) {
            setItems(newItems)
        } else {

            val diffResult = DiffUtil.calculateDiff(DiffCallback(newItems, this))

            items.clear()
            items.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (headerLayoutRes != null) {
            headerView = recyclerView.inflate(headerLayoutRes)
        }

        if (footerLayoutRes != null) {
            footerView = recyclerView.inflate(footerLayoutRes)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        headerView = null
        footerView = null
    }

    override fun getItemViewType(position: Int): Int {
        return if (headerView != null && position == 0) {
            HEADER_VIEW_TYPE
        } else if (footerView != null && position == itemCount - 1) {
            FOOTER_VIEW_TYPE
        } else {
            ITEM_VIEW_TYPE
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_VIEW_TYPE -> HeaderViewHolder(headerView!!)
            FOOTER_VIEW_TYPE -> FooterViewHolder(footerView!!)
            else -> newViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (headerView != null && position == 0) return

        if (footerView != null && position == itemCount - 1) return

        @Suppress("UNCHECKED_CAST")
        (holder as VH).bind(getItem(position))

    }

    abstract fun newViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract inner class BaseViewHolder<T>(override val containerView: View) :
            RecyclerView.ViewHolder(containerView),
        Bindable<T>,
            LayoutContainer {

        val item get() = getItem(adapterPosition)
        val itemOrNull get() = getItemOrNull(adapterPosition)
        val resources get() = itemView.resources!!
        val itemPosition get() = adapterPosition - headerOffset
    }

    private class HeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView)
    private class FooterViewHolder(footerView: View) : RecyclerView.ViewHolder(footerView)

    private inner class DiffCallback(
            private val newItems: List<T>,
            private val diffCallback: DiffItemsCallback<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = items.size + headerOffset

        override fun getNewListSize(): Int = newItems.size + headerOffset

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

            val oldType = getItemViewType(oldItemPosition)
            val newType = getItemViewType(newItemPosition)

            @Suppress("RedundantIf")
            return if (oldType == ITEM_VIEW_TYPE && newType == ITEM_VIEW_TYPE) {
                diffCallback.areItemsTheSame(
                        items[oldItemPosition - headerOffset],
                        newItems[newItemPosition - headerOffset]
                )
            } else if (oldType == HEADER_VIEW_TYPE && newType == HEADER_VIEW_TYPE) {
                true
            } else if (oldType == FOOTER_VIEW_TYPE && newType == FOOTER_VIEW_TYPE) {
                true
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

            val oldType = getItemViewType(oldItemPosition)
            val newType = getItemViewType(newItemPosition)

            @Suppress("RedundantIf")
            return if (oldType == ITEM_VIEW_TYPE && newType == ITEM_VIEW_TYPE) {
                diffCallback.areContentsTheSame(
                        items[oldItemPosition - headerOffset],
                        newItems[newItemPosition - headerOffset]
                )
            } else if (oldType == HEADER_VIEW_TYPE && newType == HEADER_VIEW_TYPE) {
                true
            } else if (oldType == FOOTER_VIEW_TYPE && newType == FOOTER_VIEW_TYPE) {
                true
            } else {
                false
            }

        }
    }
}
