package com.hannesdorfmann.adapterdelegates4.sample.base

import android.os.Bundle
import android.view.View
import androidx.annotation.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fz.common.network.NetworkUtil.isConnected
import com.fz.common.view.utils.getEndPosition
import com.fz.common.view.utils.getStartPosition
import com.fz.multistateview.MultiStateView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.sample.R
import com.hannesdorfmann.adapterdelegates4.sample.loadmore.SimpleLoadMoreDelegate

/**
 * RecyclerView 数据列表封装基础操作
 * <pre>
 * 刷新操作[onRefresh]
 * 加载更多数据[onLoadMore]
 * 网络请求操作[onRequestData]
 * 添加或更新数据[refreshData]
 * 添加或更新数据 [refreshData]
 * 添加装饰[addItemDecoration]
 * 是否启动下拉刷新[enableRefresh]
 * 获取适配器[mAdapter]
 * 获取列表布局文件[contentLayoutId]
 * 获取空视图布局文件[emptyViewResId]
 * 获取错误视图布局文件[errorViewResId]
 * 获取无网络视图布局文件[noNetworkViewResId]
 * 获取加载视图布局文件[loadingViewResId]
 * 获取状态布局资源Id[multiStateViewId]
 * 获取列表数据数量[itemCount]
 * 获取装饰[itemDecoration]
 * 获取布局管理器[getLayoutManager]
 * 快速滚动到指定位置[scrollBy]
 * 滚动到指定列表居中位置[scrollToCenter]
 * 平滑滚动到指定位置[smoothScrollBy]
 * 平滑滚动到指定索引位置[smoothScrollToPosition]
 * 平滑滚动到顶部[smoothScrollToTop]
 * 快速滚动到顶部[scrollToTop]
 * 接口请求完成[requestComplete]
 * 接口请求完成[requestComplete]
 * [refreshStart]开始刷新，控制[SwipeRefreshLayout]控显示动画件操作
 * [refreshEnd] 结束刷新，控制[SwipeRefreshLayout]控件隐藏动画操作
 * [isFailureSaveCurPage]如果为true则当前页请求失败时，继续上拉不会加载下一页，只会继续加载本页
</pre> *
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/8/19 15:46
 */
abstract class BaseRecyclerViewActivity<ADAPTER : RecyclerView.Adapter<*>>(
    @LayoutRes contentLayoutId: Int = R.layout.activity_toolbar_recycler_view,
) : BaseActivity(contentLayoutId), SwipeRefreshLayout.OnRefreshListener {
    @IntDef(REQUEST_TYPE_REFRESH, REQUEST_TYPE_LOADING, REQUEST_TYPE_NORMAL, REQUEST_TYPE_LOAD_MORE)
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class RequestType

    /**
     * 列表数据请求类型
     */
    @RequestType
    var requestType = REQUEST_TYPE_REFRESH
        protected set
    protected val mAdapter: ADAPTER by lazy {
        getAdapter()
    }
    var recyclerView: RecyclerView? = null
        protected set
    protected var mSwipeRefresh: SwipeRefreshLayout? = null
    protected var currentPage = 1
    protected var pageSize = 20

    /**
     * 用于判断当前是否正在拉取数据
     */
    var isLoadingData = false
        private set
    val mLayoutManager: RecyclerView.LayoutManager by lazy { getLayoutManager() }
    protected var multiStateView: MultiStateView? = null

    /**
     * 标记是否已经在初始化完成加载过数据
     */
    var isInitDataFinish = false
        private set

    /**
     * 标记是否请求失败
     */
    private var isRequestFailure = false
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInitDataFinish = false
        multiStateView = findViewById(multiStateViewId)
        mSwipeRefresh = findViewById(swipeRefreshId)
        recyclerView = findViewById(recyclerViewId)
        if (multiStateView != null) {
//            multiStateView!!.setEmptyViewResId(emptyViewResId)
            multiStateView!!.setErrorViewResId(errorViewResId)
            multiStateView!!.setNoNetworkViewResId(noNetworkViewResId)
            multiStateView!!.setLoadingViewResId(loadingViewResId)
            setTryClickListener(multiStateView!!.errorView)
            setTryClickListener(multiStateView!!.noNetworkView)
        }
        setLoadMoreView(isEnableLoadMore)
        addItemDecoration(itemDecoration)
        //由于使用BaseQuickAdapter,则需要先layout 再设置adapter
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.adapter = mAdapter
        recyclerView!!.itemAnimator = itemAnimator
        mSwipeRefresh?.setOnRefreshListener(this)
        enableSwipeRefresh(enableRefresh())
    }

    /**
     * 指示第一次加载数据
     */
    private var isLoadFirstData = true
    override fun onStart() {
        super.onStart()
        if (isInitializedLoadData && isLoadFirstData) {
            isLoadFirstData = false
            onRequestData(REQUEST_TYPE_LOADING)
        }
    }

    protected fun setLoadMoreView(isEnableLoadMore: Boolean) {
        if (mAdapter is LoadMoreDelegationAdapter<*>) {
            val adapter = mAdapter as LoadMoreDelegationAdapter<*>
            if (isEnableLoadMore) {
                adapter.setLoadMoreDelegate(SimpleLoadMoreDelegate())
                adapter.setEnableLoadMore(true)
                adapter.setOnLoadMoreListener { onLoadMore() }
            }
        }
    }

//    protected open val loadMoreView: BaseLoadMoreView
//        get() = ZLoadMoreView()

//    protected open fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {}

    /**
     * 请求失败是否保持页面不自动++
     *
     * @author dingpeihua
     * @date 2020/1/10 11:51
     * @version 1.0
     */
    protected open val isFailureSaveCurPage: Boolean
        get() = true

    /**
     * 是否允许初始化完成即加载数据，默认初始化化完成即加载数据
     *
     * @author dingpeihua
     * @date 2019/9/24 11:13
     * @version 1.0
     */
    protected open val isInitializedLoadData: Boolean
        get() = true

    /**
     * 设置重试按钮事件
     *
     * @author dingpeihua
     * @date 2018/12/10 11:13
     * @version 1.0
     */
    fun setTryClickListener(view: View?) {
        setTryClickListener(view, R.id.btn_retry) {
            onRetry()
        }
    }

    /**
     * 获取空视图
     *
     * @author dingpeihua
     * @date 2018/11/21 19:57
     * @version 1.0
     */
    @get:LayoutRes
    protected open val emptyViewResId: Int
        get() = 0

    /**
     * 获取无网络视图
     *
     * @author dingpeihua
     * @date 2018/11/21 19:57
     * @version 1.0
     */
    @get:LayoutRes
    protected open val noNetworkViewResId: Int
        get() =R.layout.error_no_network_center_layout

    /**
     * 获取错误视图
     *
     * @author dingpeihua
     * @date 2018/11/21 19:57
     * @version 1.0
     */
    @get:LayoutRes
    protected open val errorViewResId: Int
        get() = R.layout.error_no_network_center_layout

    /**
     * 获取加载中视图
     *
     * @author dingpeihua
     * @date 2018/11/21 19:58
     * @version 1.0
     */
    @get:LayoutRes
    protected open val loadingViewResId: Int
        get() = R.layout.progress_loading_view

    /**
     * 获取适配器对象
     *
     * @return 当前列表的适配器对象
     * @author dingpeihua
     * @date 2016/7/6 13:57
     * @version 1.0
     */
    protected abstract fun getAdapter(): ADAPTER
    protected open val multiStateViewId: Int
        get() = R.id.multi_state_view
    protected open val recyclerViewId: Int
        get() = R.id.recycler_view
    protected open val swipeRefreshId: Int
        get() = R.id.swipe_refresh_layout

    protected open fun enableSwipeRefresh(isEnabled: Boolean) {
        mSwipeRefresh?.isEnabled = isEnabled
    }

    fun refreshStart() {
        mSwipeRefresh?.isRefreshing = true
    }

    fun refreshEnd() {
        mSwipeRefresh?.isRefreshing = false
    }

    protected open fun enableRefresh(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        enableSwipeRefresh(enableRefresh())
    }

    override fun onPause() {
        super.onPause()
        enableSwipeRefresh(false)
    }

    /**
     * 重新尝试请求网络
     *
     * @author dingpeihua
     * @date 2018/11/20 19:23
     * @version 1.0
     */
    fun onRetry() {
        onRequestData(REQUEST_TYPE_LOADING)
    }

    fun onNormalRefresh() {
        currentPage = 1
        onRequestData(REQUEST_TYPE_NORMAL)
    }

    fun onLoadingData() {
        currentPage = 1
        onRequestData(REQUEST_TYPE_LOADING)
    }

    fun onRefreshData() {
        currentPage = 1
        onRequestData(REQUEST_TYPE_LOADING)
    }

    fun onSwipeRefreshData() {
        currentPage = 1
        onRequestData(REQUEST_TYPE_REFRESH)
    }

    /**
     * 数据请求处理
     *
     * @author dingpeihua
     * @date 2018/11/20 19:23
     * @version 1.0
     */
    fun onRequestData(@RequestType requestType: Int) {
        if (isLoadingData) {
            refreshEnd()
            return
        }
        if (isInitCheckNetwork && !isConnected(context, true)) {
            requestComplete(true)
            return
        }
        this.requestType = requestType
        isLoadingData = sendRequest()
        if (isLoadingData) {
            when (requestType) {
                REQUEST_TYPE_LOADING -> showLoadingView()
                REQUEST_TYPE_REFRESH, REQUEST_TYPE_NORMAL, REQUEST_TYPE_LOAD_MORE -> {
                }
                else -> {
                }
            }
        }
    }

    /**
     * 显示加载视图
     *
     * @author dingpeihua
     * @date 2018/11/21 15:54
     * @version 1.0
     */
    fun showLoadingView() {
        multiStateView?.showLoadingView()
    }

    /**
     * 显示列表内容
     *
     * @author dingpeihua
     * @date 2018/11/21 15:54
     * @version 1.0
     */
    fun showContentView() {
        multiStateView?.showContentView()
    }

    /**
     * 显示列表内容
     *
     * @author dingpeihua
     * @date 2018/11/21 15:54
     * @version 1.0
     */
    fun showNoNetworkView() {
        multiStateView?.showNoNetworkView()
    }

    /**
     * 显示空页面
     *
     * @author dingpeihua
     * @date 2018/11/21 15:54
     * @version 1.0
     */
    fun showEmptyView() {
        multiStateView?.showEmptyView()
    }

    /**
     * 显示错误页面
     *
     * @author dingpeihua
     * @date 2018/11/21 15:54
     * @version 1.0
     */
    fun showErrorView() {
        multiStateView?.showErrorView()
    }

    /**
     * 加载更多数据
     *
     * @author dingpeihua
     * @date 2018/11/21 15:53
     * @version 1.0
     */
    fun onLoadMore() {
        if (isLoadingData) {
            return
        }
        if (!isConnected(context, true)) {
            return
        }
        if (!isFailureSaveCurPage || !isRequestFailure) {
            ++currentPage
        }
        onRequestData(REQUEST_TYPE_LOAD_MORE)
    }

    /**
     * 发送网络请求
     *
     * @return true 发送网络请求成功，否则发送网络请求失败
     * @author dingpeihua
     * @date 2018/11/21 19:42
     * @version 1.0
     */
    protected abstract fun sendRequest(): Boolean

    /**
     * 初始化请求接口时是否需要检查网络状态
     *
     * @return true 需要检查网络状态，否则不检查
     * @author dingpeihua
     * @date 2018/11/21 19:43
     * @version 1.0
     */
    protected open val isInitCheckNetwork: Boolean
        get() = true

    /**
     * 快速滚动到顶部
     *
     * @author dingpeihua
     * @date 2018/11/20 19:32
     * @version 1.0
     */
    fun scrollToTop() {
        scrollToPosition(0)
    }

    /**
     * 平滑滚动到顶部
     *
     * @author dingpeihua
     * @date 2018/11/20 19:31
     * @version 1.0
     */
    fun smoothScrollToTop() {
        smoothScrollToPosition(0)
    }

    /**
     * 平滑滚动到指定位置
     *
     * @author dingpeihua
     * @date 2018/11/20 19:31
     * @version 1.0
     */
    fun smoothScrollBy(@Px dx: Int, @Px dy: Int) {
        recyclerView?.smoothScrollBy(dx, dy)
    }

    /**
     * 快速滚动到指定位置
     *
     * @author dingpeihua
     * @date 2018/11/20 19:32
     * @version 1.0
     */
    fun scrollBy(@Px dx: Int, @Px dy: Int) {
        recyclerView?.scrollBy(dx, dy)
    }

    /**
     * 滚动到列表中间位置
     *
     * @param position
     */
    fun scrollToCenter(position: Int) {
        //将点击的position转换为当前屏幕上可见的item的位置以便于计算距离顶部的高度，从而进行移动居中
        if (mLayoutManager is LinearLayoutManager) {
            recyclerView?.apply {
                val childAt =
                    getChildAt(position - (mLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                if (childAt != null) {
                    val y = childAt.top - height / 2
                    smoothScrollBy(0, y)
                }
            }
        }
    }

    /**
     * 获取布局管理器
     *
     * @author dingpeihua
     * @date 2018/11/20 18:39
     * @version 1.0
     */
    open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(this)
    }

    /**
     * 获取item 装饰
     *
     * @author dingpeihua
     * @date 2018/11/20 18:40
     * @version 1.0
     */
    protected open val itemDecoration: ItemDecoration?
        get() = null

    /**
     * 获取item 动画
     *
     * @author dingpeihua
     * @date 2018/11/20 18:41
     * @version 1.0
     */
    protected open val itemAnimator: ItemAnimator?
        get() = DefaultItemAnimator()

    protected fun addItemDecoration(itemDecoration: ItemDecoration?) {
        if (itemDecoration != null) {
            recyclerView?.addItemDecoration(itemDecoration)
        }
    }

    /**
     * Remove an [RecyclerView.ItemDecoration] from this RecyclerView.
     *
     *
     * The given decoration will no longer impact the measurement and drawing of
     * item views.
     *
     * @param decor Decoration to remove
     * @see .removeItemDecoration
     */
    fun removeItemDecoration(decor: ItemDecoration?) {
        if (decor != null) {
            recyclerView?.removeItemDecoration(decor)
        }
    }

    /**
     * 控制列表平稳滚动到指定位置
     *
     * @param position 滚动位置
     * @author dingpeihua
     * @date 2016/8/10 17:02
     * @version 1.0
     */
    protected fun smoothScrollToPosition(position: Int) {
        recyclerView?.smoothScrollToPosition(position)
    }

    /**
     * 控制列表滚动到指定位置
     *
     * @param position 滚动位置
     * @author dingpeihua
     * @date 2016/8/10 17:02
     * @version 1.0
     */
    protected fun scrollToPosition(position: Int) {
        recyclerView?.scrollToPosition(position)
    }

    protected fun scrollToPositionWithOffset(position: Int, offset: Int) {
        if (mLayoutManager is LinearLayoutManager) {
            (mLayoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, offset)
        } else if (mLayoutManager is StaggeredGridLayoutManager) {
            (mLayoutManager as StaggeredGridLayoutManager).scrollToPositionWithOffset(
                position,
                offset
            )
        }
    }

    override fun onRefresh() {
        refreshStart()
        onSwipeRefreshData()
    }

    /**
     * 是否启用加载更多
     *
     * @author dingpeihua
     * @date 2019/2/16 10:24
     * @version 1.0
     */
    protected open val isEnableLoadMore: Boolean
        get() = true

    /**
     * 计算总页数
     *
     * @param totalSize
     * @param pageSize
     * @return
     */
    @JvmOverloads
    fun calTotalPage(totalSize: Int, pageSize: Int = this.pageSize): Int {
        val pageNum = totalSize / pageSize
        val leftPage = if (totalSize % pageSize > 0) 1 else 0
        return pageNum + leftPage
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data      数据列表
     * @param totalSize 总数据量
     * @author dingpeihua
     * @date 2018/11/20 18:42
     * @version 1.0
     */
    @JvmOverloads
    protected fun <T> refreshData(
        totalSize: Int,
        data: MutableList<T>?,
        isClearData: Boolean = false
    ) {
        refreshData(data, currentPage < calTotalPage(totalSize), isClearData)
    }

    protected fun refreshData(isMoreData: Boolean = false) {
        refreshData(mutableListOf<Any>(), isMoreData)
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data      数据列表
     * @param totalPage 总页数
     * @author dingpeihua
     * @date 2018/11/20 18:42
     * @version 1.0
     */
    protected fun <T> refreshData(data: MutableList<T>?, totalPage: Int) {
        refreshData(data, currentPage < totalPage)
    }

    /**
     * @param data      数据列表
     * @param totalPage 总页数
     * @param gone      是否隐藏加载更多
     * @author dingpeihua
     * @date 2019/2/16 10:23
     * @version 1.0
     */
    protected fun <T> refreshData(
        data: MutableList<T>?,
        totalPage: Int,
        gone: Boolean
    ) {
        refreshData(data, currentPage < totalPage, false, gone)
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data        数据列表
     * @param isClearData 是否清除数据
     * @param isMoreData  是否有更多数据
     * @author dingpeihua
     * @date 2019/2/16 10:22
     * @version 1.0
     */
    protected fun <T> refreshData(
        isClearData: Boolean,
        data: MutableList<T>?,
        isMoreData: Boolean
    ) {
        if (isClearData) {
            clearListData()
        }
        refreshData(data, isMoreData)
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
    protected fun <T> refreshData(
        data: MutableList<T>?,
        isClearData: Boolean,
        totalPage: Int
    ) {
        refreshData(data, totalPage > currentPage, isClearData, true)
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
    protected fun <T> refreshData(
        data: MutableList<T>?,
        totalPage: Int,
        isClearData: Boolean,
        gone: Boolean
    ) {
        refreshData(data, totalPage > currentPage, isClearData, gone)
    }

    /**
     * 添加或者更新数据源,并刷新列表
     *
     * @param data       数据列表
     * @param isMoreData 是否有更多数据
     * @param gone       是否显示提示文案
     * @author dingpeihua
     * @date 2018/11/20 18:42
     * @version 1.0
     */
    @JvmOverloads
    protected fun <T> refreshData(
        data: MutableList<T>? = null,
        isMoreData: Boolean = false,
        isClearData: Boolean = false,
        gone: Boolean = true
    ) {
        if (recyclerView != null) {
            postData(data, isMoreData, isClearData, gone)
        }
    }

    private fun <T> postData(
        data: MutableList<T>?,
        isMoreData: Boolean,
        isClearData: Boolean = false,
        gone: Boolean
    ) {
        recyclerView?.stopScroll()
        if (isClearData) {
            clearListData()
        }
     if (mAdapter is ListDelegationAdapter<*>) {
            val adapter = mAdapter as ListDelegationAdapter<List<T>>
            when (requestType) {
                REQUEST_TYPE_LOAD_MORE -> {
                    adapter.addItems(data)
                }
                else -> adapter.items = data
            }
        }
        requestComplete(false, isMoreData, gone)
        if (isLessOneScreenAutoLoad && isMoreData) {
            recyclerView?.post {
                lessOneScreenAutoLoadNextPage()
            }
        }
    }

    fun clearListData() {
       if (mAdapter is ListDelegationAdapter<*>) {
            val adapter = mAdapter as ListDelegationAdapter<*>
            val dataSize = adapter.itemCount
            adapter.items?.clear()
            //避免异常Inconsistency detected. Invalid view holder adapter positionViewHolder
            adapter.notifyItemRangeRemoved(0, dataSize)
        }
    }

    /**
     * 是否启用不足一屏自动加载下一页
     */
    open val isLessOneScreenAutoLoad: Boolean
        get() = false

    /**
     * 判断当前显示的数据是否满一屏，如果不足一屏，则自动加载下一页数据
     */
    private fun lessOneScreenAutoLoadNextPage() {
        if (!isFullScreen()) {
            onLoadMore()
        }
    }

    /**
     * 判断数据是否满一屏
     */
    private fun isFullScreen(): Boolean {
        val endPosition: Int = recyclerView.getEndPosition()
        val startPosition: Int = recyclerView.getStartPosition()
        return (endPosition + 1) != itemCount || startPosition != 0
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
            return mAdapter.itemCount
        }

    /**
     * 获取列表项目数量
     *
     * @author dingpeihua
     * @date 2018/11/21 15:51
     * @version 1.0
     */
    fun <T> getItem(@androidx.annotation.IntRange(from = 0) position: Int): T? {
       if (mAdapter is ListDelegationAdapter<*>) {
            val adapter = mAdapter as ListDelegationAdapter<*>
            return adapter.getItem(position)
        }
        return null
    }

      /**
     * 请求完成
     *
     * @param isError    是否请求错误
     * @param isMoreData 是否有更多数据
     * @param gone       是否隐藏底部加载更多
     * @author dingpeihua
     * @date 2018/11/21 15:38
     * @version 1.0
     */
    @JvmOverloads
    fun requestComplete(
        isError: Boolean = false,
        isMoreData: Boolean = false,
        gone: Boolean = true
    ) {
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
        if (isEnableLoadMore) {
            if (mAdapter is LoadMoreDelegationAdapter<*>) {
                val adapter = mAdapter as LoadMoreDelegationAdapter<*>
                if (isMoreData) {
                    adapter.loadMoreComplete()
                } else {
                    adapter.loadMoreEnd(gone)
                }
            }
        }
        //标记初始化加载数据完成
        isInitDataFinish = true
        isLoadingData = false
        isRequestFailure = isError
        refreshEnd()
    }

    /**
     * 获取当前是否是刷新事件
     *
     * @author dingpeihua
     * @date 2018/11/30 12:56
     * @version 1.0
     */
    val isRefresh: Boolean
        get() = requestType != REQUEST_TYPE_LOAD_MORE

    /**
     * 获取当前是否是加载更多事件
     *
     * @author dingpeihua
     * @date 2018/11/30 12:56
     * @version 1.0
     */
    val isLoadMore: Boolean
        get() = requestType == REQUEST_TYPE_LOAD_MORE

    /**
     * 用于判断是否是第一页
     *
     * @author dingpeihua
     * @date 2019/2/16 10:25
     * @version 1.0
     */
    val isFistPage: Boolean
        get() = currentPage == 1

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val adapter = mAdapter
        if (adapter is ListDelegationAdapter<*>) {
            adapter.onRestoreInstanceState(savedInstanceState)
        } else if (adapter is AsyncListDifferDelegationAdapter<*>) {
            adapter.onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val adapter = mAdapter
        if (adapter is ListDelegationAdapter<*>) {
            adapter.onSaveInstanceState(outState)
        } else if (adapter is AsyncListDifferDelegationAdapter<*>) {
            adapter.onSaveInstanceState(outState)
        }
    }

    companion object {
        /**
         * 下拉刷新列表
         */
        const val REQUEST_TYPE_REFRESH = 0x011

        /**
         * 正常加载,不显示loading
         */
        const val REQUEST_TYPE_NORMAL = 0x012

        /**
         * 正常加载,显示loading
         */
        const val REQUEST_TYPE_LOADING = 0x013

        /**
         * 上拉加载更多数据
         */
        const val REQUEST_TYPE_LOAD_MORE = 0x014
    }
}