package com.hannesdorfmann.adapterdelegates4.dsl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hannesdorfmann.adapterdelegates4.AbsAdapterDelegate

abstract class ViewBindingAbsAdapterDelegate<T, VB : ViewBinding> : AbsAdapterDelegate<T>() {

    abstract val layoutResId: Int
        @LayoutRes get
    abstract val vbFactory: (View) -> VB

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return AbsDelegateViewHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false), vbFactory)
    }

    final override fun onBindViewHolder(
        items: MutableList<T>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        onBindViewHolder(items[position], position, holder as AbsDelegateViewHolder<VB>)
    }

    override fun onBindViewHolder(item: T, position: Int, holder: RecyclerView.ViewHolder) {
        onBindViewHolder(item, position, holder as AbsDelegateViewHolder<VB>)
    }

    private fun onBindViewHolder(item: T, position: Int, holder: AbsDelegateViewHolder<VB>) {
        onBindViewHolder(item, position, holder.binding)
    }

    abstract fun onBindViewHolder(item: T, position: Int, binding: VB)
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        onViewDetachedFromWindow(holder as AbsDelegateViewHolder<VB>)
    }

    private fun onViewDetachedFromWindow(holder: AbsDelegateViewHolder<VB>) {
        onViewDetachedFromWindow(holder.binding)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        onViewRecycled(holder as AbsDelegateViewHolder<VB>)
    }

    open fun onViewRecycled(holder: AbsDelegateViewHolder<VB>) {
        onViewRecycled(holder.binding)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return onFailedToRecycleView(holder as AbsDelegateViewHolder<VB>)
    }

    private fun onFailedToRecycleView(holder: AbsDelegateViewHolder<VB>): Boolean {
        return onFailedToRecycleView(holder.binding)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        onViewAttachedToWindow(holder as AbsDelegateViewHolder<VB>)
    }

    private fun onViewAttachedToWindow(holder: AbsDelegateViewHolder<VB>) {
        onViewAttachedToWindow(holder.binding)
    }

    open fun onViewDetachedFromWindow(binding: VB) {
    }

    open fun onViewRecycled(binding: VB) {
    }

    open fun onFailedToRecycleView(binding: VB): Boolean {
        return false
    }

    open fun onViewAttachedToWindow(binding: VB) {

    }
}