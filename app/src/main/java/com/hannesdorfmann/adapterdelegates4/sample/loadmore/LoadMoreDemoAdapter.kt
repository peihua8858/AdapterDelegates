package com.hannesdorfmann.adapterdelegates4.sample.loadmore

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.sample.databinding.ItemTextViewBinding

class LoadMoreDemoAdapter : LoadMoreDelegationAdapter<List<AdapterBean<*>>>() {
    companion object {
        const val TYPE_HEAD = 11111111;
        const val TYPE_FOOT = 2222222;
        const val TYPE_ITEM_DATA = 333333;
    }

    init {
        addHeadDelegate(SimpleHeadAdapterDelegate())
        addDelegate(adapterDelegateViewBinding({ layoutInflater, parent ->
            ItemTextViewBinding.inflate(layoutInflater, parent, false)
        }, on = { item, items, position -> item.itemType == TYPE_ITEM_DATA }) {
            bind {
                binding.apply {
                    tvContent.text = item.value.toString()
                }
            }
        })
        addFootDelegate(SimpleFootAdapterDelegate())
    }
}