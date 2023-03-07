package com.hannesdorfmann.adapterdelegates4.sample.loadmore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AbsDelegateViewHolder
import com.hannesdorfmann.adapterdelegates4.sample.R
import com.hannesdorfmann.adapterdelegates4.sample.databinding.ItemTextViewBinding

class SimpleFootAdapterDelegate :AbsAdapterDelegate<AdapterBean<*>>() {
    override fun isForViewType(item: AdapterBean<*>): Boolean {
        return item.itemType == LoadMoreDemoAdapter.TYPE_FOOT
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return AbsDelegateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_text_view, parent, false),
            ItemTextViewBinding::bind
        )
    }

    override fun onBindViewHolder(item: AdapterBean<*>, position: Int, holder: RecyclerView.ViewHolder) {
        val tvContent = holder.itemView.findViewById<TextView>(R.id.tv_content)
        tvContent.text = item.value.toString()
    }


}