package com.hannesdorfmann.adapterdelegates4.sample.loadmore

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fz.common.view.utils.getItemView
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.sample.R

/**
 * 加载更多
 * @author dingpeihua
 * @date 2023/3/7 10:25
 * @version 1.0
 */
class SimpleLoadMoreDelegate : LoadMoreAdapterDelegate<Any>() {
    override fun getRootView(parent: ViewGroup): View {
        return parent.getItemView(R.layout.zf_quick_view_load_more)
    }

    override fun getLoadingView(holder: RecyclerView.ViewHolder): View {
        return holder.itemView.findViewById(R.id.load_more_loading_view)
    }

    override fun getLoadComplete(holder: RecyclerView.ViewHolder): View {
        return holder.itemView.findViewById(R.id.load_more_load_complete_view)
    }

    override fun getLoadEndView(holder: RecyclerView.ViewHolder): View {
        return holder.itemView.findViewById(R.id.load_more_load_end_view)
    }

    override fun getLoadFailView(holder: RecyclerView.ViewHolder): View {
        return holder.itemView.findViewById(R.id.load_more_load_fail_view)
    }
}