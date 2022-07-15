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

    final override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
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

    fun onBindViewHolder(item: T, position: Int, holder: AbsDelegateViewHolder<VB>) {
        onBindViewHolder(item, position, holder.binding)
    }

    abstract fun onBindViewHolder(item: T, position: Int, binding: VB)
}