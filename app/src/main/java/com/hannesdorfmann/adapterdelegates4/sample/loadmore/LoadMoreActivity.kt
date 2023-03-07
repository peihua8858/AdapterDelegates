package com.hannesdorfmann.adapterdelegates4.sample.loadmore

import android.os.Bundle
import com.fz.common.utils.apiWithAsyncCreated
import com.hannesdorfmann.adapterdelegates4.sample.base.BaseRecyclerViewActivity
import kotlinx.coroutines.delay

class LoadMoreActivity : BaseRecyclerViewActivity<LoadMoreDemoAdapter>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val headData = arrayListOf<AdapterBean<*>>()
        for (i in 0 until 5) {
            headData.add(AdapterBean(LoadMoreDemoAdapter.TYPE_HEAD, "这是头部$i"))
        }
        val footData = arrayListOf<AdapterBean<*>>()
        for (i in 0 until 5) {
            footData.add(AdapterBean(LoadMoreDemoAdapter.TYPE_HEAD, "这是底部$i"))
        }
        mAdapter.setHeadData(headData)
        mAdapter.setFootData(footData)
    }

    override fun getAdapter(): LoadMoreDemoAdapter {
        return LoadMoreDemoAdapter()
    }

    override fun sendRequest(): Boolean {
        apiWithAsyncCreated<MutableList<AdapterBean<*>>> {
            onRequest {
                delay(2000)
                val itemData = arrayListOf<AdapterBean<*>>()
                val start = currentPage + (pageSize * (currentPage - 1))
                val end = start + pageSize + 1
                for (i in start until end) {
                    itemData.add(AdapterBean(LoadMoreDemoAdapter.TYPE_ITEM_DATA, "这是中间内容部分$i"))
                }
                itemData

            }
            onResponse {
                refreshData(it, true)
            }
            onError { requestComplete(true) }
        }
        return true
    }
}