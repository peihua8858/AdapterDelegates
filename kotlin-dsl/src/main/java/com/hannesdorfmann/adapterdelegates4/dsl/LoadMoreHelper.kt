package com.hannesdorfmann.adapterdelegates4.dsl

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.IntDef
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fz.common.collections.isNonEmpty
import com.fz.multistateview.MultiStateView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.loadmore.FootHeadDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreDelegationAdapter

/**
 * 加载更多数据
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/3/4 11:53
 */
open class LoadMoreHelper<T, ADAPTER : ListDelegationAdapter<List<T>>>(
    private val rootView: View,
    private val mAdapter: ADAPTER,
    private val helperListener: OnLoadMoreHelperListener,
) {
    @IntDef(
        REQUEST_TYPE_LOADING,
        REQUEST_TYPE_NORMAL,
        REQUEST_TYPE_LOAD_MORE
    )
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class RequestType

    /**
     * 列表数据请求类型
     */
    @RequestType
    private var mRequestType = REQUEST_TYPE_NORMAL

    /**
     * 当前页码
     */
    var currentPage = 1

    /**
     * 每一页的数据量
     */
    var pageSize = 20
    protected var mSwipeRefresh: SwipeRefreshLayout? = null
    var recyclerView: RecyclerView? = null
        protected set
    protected var multiStateView: MultiStateView? = null

    /**
     * 用于判断当前是否正在拉取数据
     */
    private var isLoadingData = false
    private val context: Context
    protected open val multiStateViewId: Int
        @IdRes get() = R.id.multi_state_view

    protected open val recyclerViewId: Int
        @IdRes get() = R.id.recycler_view
    protected open val swipeRefreshId: Int
        @IdRes get() = R.id.swipe_refresh_layout

    init {
        setLoadMoreView(mAdapter, helperListener)
        multiStateView = rootView.findViewById(multiStateViewId)
        mSwipeRefresh = rootView.findViewById(swipeRefreshId)
        recyclerView = rootView.findViewById(recyclerViewId)
        context = rootView.context
        recyclerView?.apply {
            layoutManager = helperListener.layoutManager
            val itemAnimator = helperListener.itemAnimator
            if (itemAnimator != null) {
                this.itemAnimator = itemAnimator
            }
            val itemDecoration = helperListener.itemDecoration
            if (itemDecoration != null) {
                addItemDecoration(itemDecoration)
            }
            adapter = mAdapter
        }
    }


    private fun setLoadMoreView(adapter: ADAPTER, helperListener: OnLoadMoreHelperListener) {
        if (adapter is LoadMoreDelegationAdapter<*>) {
            val loadMoreDelegate = helperListener.loadMoreAdapterDelegate
            if (helperListener.isEnableLoadMore && loadMoreDelegate != null) {
                adapter.setLoadMoreDelegate(loadMoreDelegate)
                adapter.setEnableLoadMore(true)
                adapter.setOnLoadMoreListener { onLoadMore() }
            }
        }
    }

    /**
     * 加载数据
     *
     * @author dingpeihua
     * @date 2019/3/4 11:48
     * @version 1.0
     */
    fun showLoadingView() {
        if (multiStateView != null) {
            multiStateView?.showLoadingView()
        } else {
            helperListener.showLoadingView()
        }
    }

    /**
     * 显示内容
     *
     * @author dingpeihua
     * @date 2019/3/4 11:48
     * @version 1.0
     */
    fun showContentView() {
        if (multiStateView != null) {
            multiStateView?.showContentView()
        } else {
            helperListener.showContentView()
        }
    }

    /**
     * 显示没有网络视图
     *
     * @author dingpeihua
     * @date 2019/9/22 12:23
     * @version 1.0
     */
    fun showNoNetworkView() {
        if (multiStateView != null) {
            multiStateView?.showNoNetworkView()
        } else {
            helperListener.showNoNetworkView()
        }
    }

    /**
     * 显示空数据视图
     *
     * @author dingpeihua
     * @date 2019/3/4 11:48
     * @version 1.0
     */
    fun showEmptyView() {
        if (multiStateView != null) {
            multiStateView?.showEmptyView()
        } else {
            helperListener.showEmptyView()
        }
    }

    /**
     * 显示错误视图
     *
     * @author dingpeihua
     * @date 2019/3/4 11:48
     * @version 1.0
     */
    fun showErrorView() {
        if (multiStateView != null) {
            multiStateView?.showErrorView()
        } else {
            helperListener.showErrorView()
        }
    }

    interface OnLoadMoreHelperListener {
        val loadMoreAdapterDelegate: LoadMoreAdapterDelegate<*>?
            get() {
                return null
            }

        /**
         * 请求接口数据
         *
         * @param curPage
         * @param pageSize
         * @return
         * @author dingpeihua
         * @date 2019/3/4 11:40
         * @version 1.0
         */
        fun onRequest(curPage: Int, pageSize: Int): Boolean

        /**
         * 加载数据
         *
         * @author dingpeihua
         * @date 2019/3/4 11:48
         * @version 1.0
         */
        fun showLoadingView() {}

        /**
         * 显示内容
         *
         * @author dingpeihua
         * @date 2019/3/4 11:48
         * @version 1.0
         */
        fun showContentView() {}

        /**
         * 显示没有网络视图
         *
         * @author dingpeihua
         * @date 2019/9/22 12:23
         * @version 1.0
         */
        fun showNoNetworkView() {}

        /**
         * 显示空数据视图
         *
         * @author dingpeihua
         * @date 2019/3/4 11:48
         * @version 1.0
         */
        fun showEmptyView() {}

        /**
         * 显示错误视图
         *
         * @author dingpeihua
         * @date 2019/3/4 11:48
         * @version 1.0
         */
        fun showErrorView() {}

        /**
         * 是否启动加载更多
         *
         * @return
         */
        val isEnableLoadMore: Boolean
            get() = true

        /**
         * 是否初始化加载时检查网络状态
         *
         * @return
         * @author dingpeihua
         * @date 2019/3/12 18:34
         * @version 1.0
         */
        val isInitCheckNetwork: Boolean
            get() = true

        /**
         * 获取布局管理器,如果为null 或者不实现，则需要自行设置
         *
         * @return {@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)}
         * @author dingpeihua
         * @date 2018/11/20 18:39
         * @version 1.0
         */
        val layoutManager: RecyclerView.LayoutManager

        /**
         * 获取item 装饰 如果为null 或者不实现，则需要自行设置
         *
         * @return {@link RecyclerView#addItemDecoration(RecyclerView.ItemDecoration)}
         * @author dingpeihua
         * @date 2018/11/20 18:40
         * @version 1.0
         */
        val itemDecoration: RecyclerView.ItemDecoration?
            get() = null

        /**
         * 获取item 动画
         *
         * @author dingpeihua
         * @date 2018/11/20 18:41
         * @version 1.0
         */
        val itemAnimator: RecyclerView.ItemAnimator?
            get() = DefaultItemAnimator()

        /**
         * 处理完成
         *
         * @author dingpeihua
         * @date 2019/9/22 13:19
         * @version 1.0
         */
        fun onComplete() {

        }

        fun isConnected(context: Context): Boolean {
            return isConnected(context, false)
        }

        fun isConnected(context: Context, showNetworkErrorTips: Boolean): Boolean {
            return true
        }
    }

    /**
     * 加载更多数据
     *
     * @author dingpeihua
     * @date 2018/11/21 15:53
     * @version 1.0
     */
    fun onLoadMore() {
        if (!helperListener.isConnected(context)) {
            return
        }
        if (isLoadingData) {
            return
        }
        ++currentPage
        onRequest(REQUEST_TYPE_LOAD_MORE)
    }

    fun onLoadingData() {
        currentPage = 1
        onRequest(REQUEST_TYPE_LOADING)
    }

    /**
     * 刷新数据，即请求第一页数据
     *
     * @author dingpeihua
     * @date 2019/3/4 14:07
     * @version 1.0
     */
    fun onRefresh() {
        currentPage = 1
        onRequest(REQUEST_TYPE_NORMAL)
    }

    /**
     * 重新尝试请求网络
     *
     * @author dingpeihua
     * @date 2018/11/20 19:23
     * @version 1.0
     */
    fun onRetry() {
        onLoadingData()
    }

    /**
     * 数据请求处理
     *
     * @author dingpeihua
     * @date 2018/11/20 19:23
     * @version 1.0
     */
    fun onRequest(@RequestType requestType: Int) {
        if (helperListener.isInitCheckNetwork
            && !helperListener.isConnected(context, true)
        ) {
            helperListener.onComplete()
            showErrorView()
            return
        }
        if (isLoadingData) {
            return
        }
        mRequestType = requestType
        isLoadingData = helperListener.onRequest(currentPage, pageSize)
        if (isLoadingData) {

            when (requestType) {
                REQUEST_TYPE_LOADING -> showLoadingView()
                else -> {
                }
            }
        }
    }

    /**
     * 计算总页数
     *
     * @param totalSize
     * @return
     */
    fun calTotalPage(totalSize: Int): Int {
        return calTotalPage(totalSize, pageSize)
    }

    /**
     * 刷新数据
     *
     * @param totalSize 数据总量
     * @param data      当前数据集合
     * @author dingpeihua
     * @date 2019/3/4 11:49
     * @version 1.0
     */
    fun refreshData(data: List<T>?) {
        refreshData(data, false)
    }

    /**
     * 刷新数据
     *
     * @param totalSize 数据总量
     * @param data      当前数据集合
     * @author dingpeihua
     * @date 2019/3/4 11:49
     * @version 1.0
     */
    fun refreshData(totalSize: Int, data: List<T>?) {
        refreshData(data, currentPage < calTotalPage(totalSize))
    }

    /**
     * 刷新数据
     *
     * @param data      当前数据集合
     * @param totalPage 数据总页数
     * @author dingpeihua
     * @date 2019/3/4 11:49
     * @version 1.0
     */
    fun refreshData(data: List<T>?, totalPage: Int) {
        refreshData(data, currentPage < totalPage)
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data        数据列表
     * @param isClearData 是否清除数据
     * @param totalPage   总页数
     * @author dingpeihua
     * @date 2019/2/16 10:22
     * @version 1.0
     */
    fun refreshData(data: List<T>?, isClearData: Boolean, totalPage: Int) {
        refreshData(data, totalPage, isClearData, true)
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data        数据列表
     * @param totalPage   总页数
     * @param isClearData 是否先清除数据
     * @param gone        是否隐藏加载更多
     * @author dingpeihua
     * @date 2019/2/16 10:20
     * @version 1.0
     */
    fun refreshData(data: List<T>?, totalPage: Int, isClearData: Boolean, gone: Boolean) {
        if (isClearData) {
            clearListData()
        }
        refreshData(data, totalPage, gone)
    }

    private fun clearListData() {
        val data = mAdapter.items
        val dataSize = data?.size ?: 0
        if (data is MutableList<*>) {
            data.clear()
        } else {
            mAdapter.items = arrayListOf()
        }
        //避免异常Inconsistency detected. Invalid view holder adapter positionViewHolder
        if (mAdapter is FootHeadDelegationAdapter<*>) {
            mAdapter.notifyItemRangeRemoved(mAdapter.headCount(), dataSize)
        } else {
            mAdapter.notifyDataSetChanged()
        }
    }

    /**
     * @param data      数据列表
     * @param totalPage 总页数
     * @param gone      是否隐藏加载更多
     * @author dingpeihua
     * @date 2019/2/16 10:23
     * @version 1.0
     */
    fun refreshData(data: List<T>?, totalPage: Int, gone: Boolean) {
        refreshData(data, currentPage < totalPage, gone)
    }
    /**
     * 刷新数据
     *
     * @param data       数据列表
     * @param isMoreData 是否有更多数据
     * @param gone       是否显示提示文案 true为隐藏提示，反之显示文案
     * @author dingpeihua
     * @date 2019/3/4 11:50
     * @version 1.0
     */
    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data       数据列表
     * @param isMoreData 是否有更多数据
     * @author dingpeihua
     * @date 2018/11/20 18:42
     * @version 1.0
     */
    @JvmOverloads
    fun refreshData(data: List<T>?, isMoreData: Boolean, gone: Boolean = true) {
        recyclerView?.stopScroll()
        if (data.isNonEmpty()) {
            when (mRequestType) {
                REQUEST_TYPE_LOAD_MORE -> mAdapter.addData(data)
                else -> mAdapter.items = ArrayList(data)
            }
        }
        refreshComplete(false, isMoreData, gone)
    }

    /**
     * 请求完成
     *
     * @author dingpeihua
     * @date 2018/11/21 15:38
     * @version 1.0
     */
    @JvmOverloads
    fun refreshComplete(isError: Boolean = false, isMoreData: Boolean = false, gone: Boolean = true) {
        val itemCount = itemCount
        if (itemCount > 0) {
            showContentView()
        } else {
            if (isError) {
                showErrorView()
            } else {
                showEmptyView()
            }
        }
        if (helperListener.isEnableLoadMore && mAdapter is LoadMoreDelegationAdapter<*>) {
            val adapter = mAdapter as LoadMoreDelegationAdapter<*>
            if (isMoreData) {
                adapter.loadMoreComplete()
            } else {
                adapter.loadMoreEnd(gone)
            }
        }
        isLoadingData = false
        helperListener.onComplete()
    }

    /**
     * 获取列表项目数量
     *
     * @author dingpeihua
     * @date 2018/11/21 15:51
     * @version 1.0
     */
    val itemCount: Int
        get() {
            val datas: List<*>? = mAdapter.items
            return datas?.size ?: 0
        }

    /**
     * 获取当前请求类型
     *
     * @author dingpeihua
     * @date 2018/11/20 19:35
     * @version 1.0
     */
    fun getRequestType(): Int {
        return mRequestType
    }

    /**
     * 获取当前是否正在加载中
     *
     * @author dingpeihua
     * @date 2018/11/20 19:35
     * @version 1.0
     */
    fun isLoadingData(): Boolean {
        return isLoadingData
    }

    /**
     * 获取当前是否是刷新事件
     *
     * @author dingpeihua
     * @date 2018/11/30 12:56
     * @version 1.0
     */
    fun isRefresh(): Boolean {
        return mRequestType != REQUEST_TYPE_LOAD_MORE
    }

    /**
     * 获取当前是否是加载更多事件
     *
     * @author dingpeihua
     * @date 2018/11/30 12:56
     * @version 1.0
     */
    fun isLoadMore(): Boolean {
        return mRequestType == REQUEST_TYPE_LOAD_MORE
    }

    /**
     * 用于判断是否是第一页
     *
     * @author dingpeihua
     * @date 2019/2/16 10:25
     * @version 1.0
     */
    open fun isFistPage(): Boolean {
        return currentPage == 1
    }

    companion object {

        /**
         * 正常加载,不显示loading
         */
        const val REQUEST_TYPE_NORMAL = 0x011

        /**
         * 正常加载,显示loading
         */
        const val REQUEST_TYPE_LOADING = 0x013

        /**
         * 上拉加载更多数据
         */
        const val REQUEST_TYPE_LOAD_MORE = 0x014

        /**
         * 计算总页数
         *
         * @param totalSize 总数据条数
         * @param pageSize 每页数据量
         * @return
         */
        @JvmStatic
        fun calTotalPage(totalSize: Int, pageSize: Int): Int {
            val pageNum = totalSize / pageSize
            val leftPage = if (totalSize % pageSize > 0) 1 else 0
            return pageNum + leftPage
        }
    }
}