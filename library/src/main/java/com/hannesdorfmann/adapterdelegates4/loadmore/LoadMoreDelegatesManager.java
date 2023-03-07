package com.hannesdorfmann.adapterdelegates4.loadmore;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates4.FootHeadDelegatesManager;

/**
 * 加载更多委托管理器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2023/3/6 15:43
 */
public class LoadMoreDelegatesManager<T> extends FootHeadDelegatesManager<T> {
    protected LoadMoreAdapterDelegate loadMoreAdapterDelegate;

    public void setLoadMoreAdapterDelegate(LoadMoreAdapterDelegate<T> loadMoreAdapterDelegate) {
        this.loadMoreAdapterDelegate = loadMoreAdapterDelegate;
    }

    public boolean isEnabledLoadMore() {
        return loadMoreAdapterDelegate != null;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getViewType(@NonNull AdapterDelegate<T> delegate) {
        if (delegate instanceof LoadMoreAdapterDelegate) {
            return LOAD_MORE_ITEM_VIEW_TYPE;
        }
        return super.getViewType(delegate);
    }

    @Nullable
    @Override
    public AdapterDelegate<T> getDelegateForViewType(int viewType) {
        if (viewType == LOAD_MORE_ITEM_VIEW_TYPE) {
            return loadMoreAdapterDelegate;
        }
        return super.getDelegateForViewType(viewType);
    }
    public void setEnableLoadMore(boolean isEnableLoadMore) {
        loadMoreAdapterDelegate.setEnableLoadMore(isEnableLoadMore);
    }
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        loadMoreAdapterDelegate.setOnLoadMoreListener(onLoadMoreListener);
    }

    public void setAdapter(LoadMoreDelegationAdapter adapter) {
        loadMoreAdapterDelegate.setAdapter(adapter);
    }

    public void setPreLoadNumber(int preLoadNumber) {
        loadMoreAdapterDelegate.setPreLoadNumber(preLoadNumber);
    }

    public boolean isLoading() {
        return loadMoreAdapterDelegate.isLoading();
    }

    public LoadMoreStatus loadMoreStatus() {
        return loadMoreAdapterDelegate.loadMoreStatus;
    }

    public void setLoadMoreStatus(LoadMoreStatus loadMoreStatus) {
        loadMoreAdapterDelegate.loadMoreStatus = loadMoreStatus;
    }

    public boolean loadMoreToLoading() {
        return loadMoreAdapterDelegate.loadMoreToLoading();
    }

    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    void loadMoreEnd(boolean gone) {
        loadMoreAdapterDelegate.loadMoreEnd(gone);
    }

    /**
     * Refresh complete
     */
    void loadMoreComplete() {
        loadMoreAdapterDelegate.loadMoreComplete();
    }

    /**
     * Refresh failed
     */
    void loadMoreFail() {
        loadMoreAdapterDelegate.loadMoreFail();
    }

    /**
     * 重置状态
     */
    void reset() {
        loadMoreAdapterDelegate.reset();
    }

    public boolean hasLoadMoreView() {
        return loadMoreAdapterDelegate.hasLoadMoreView();
    }
}
