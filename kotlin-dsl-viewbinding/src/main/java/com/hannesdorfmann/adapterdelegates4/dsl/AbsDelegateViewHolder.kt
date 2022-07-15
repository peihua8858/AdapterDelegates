package com.hannesdorfmann.adapterdelegates4.dsl

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding

class AbsDelegateViewHolder<VB : ViewBinding>(view: View, vbFactory: (View) -> VB) : RecyclerView.ViewHolder(view) {
    val binding by viewBinding(vbFactory)
}