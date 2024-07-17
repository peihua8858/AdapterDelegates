package com.hannesdorfmann.adapterdelegates4.dsl

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class AbsDelegateViewHolder<VB : ViewBinding>(view: View,val binding: VB) : RecyclerView.ViewHolder(view) {
}