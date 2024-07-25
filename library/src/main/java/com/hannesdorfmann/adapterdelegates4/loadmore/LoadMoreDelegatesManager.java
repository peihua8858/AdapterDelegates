package com.hannesdorfmann.adapterdelegates4.loadmore;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates4.FootHeadDelegatesManager;

import java.util.ArrayList;
import java.util.List;

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

    void onBindLoadMoreViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        autoLoadMore(position);
        if (loadMoreAdapterDelegate != null) {
            loadMoreAdapterDelegate.onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull List<T> items, int position, @NonNull RecyclerView.ViewHolder holder, List payloads) {
        autoLoadMore(position);
        if (loadMoreAdapterDelegate != null && loadMoreAdapterDelegate.isLoadMoreData(position)) {
            loadMoreAdapterDelegate.onBindViewHolder(holder, position);
            return;
        }
        super.onBindViewHolder(items, position, holder, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder holder, List payloads) {
        autoLoadMore(position);
        if (loadMoreAdapterDelegate != null && loadMoreAdapterDelegate.isLoadMoreData(position)) {
            loadMoreAdapterDelegate.bindViewHolder(items, position, holder, payloads);
            return;
        }
        super.onBindViewHolder(items, position, holder, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder holder) {
        autoLoadMore(position);
        if (loadMoreAdapterDelegate != null && loadMoreAdapterDelegate.isLoadMoreData(position)) {
            loadMoreAdapterDelegate.bindViewHolder(items, position, holder, new ArrayList<>());
            return;
        }
        super.onBindViewHolder(items, position, holder);
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
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.setEnableLoadMore(isEnableLoadMore);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.setOnLoadMoreListener(onLoadMoreListener);
    }

    public void setAdapter(LoadMoreDelegationAdapter adapter) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.setAdapter(adapter);
    }

    public void setPreLoadNumber(int preLoadNumber) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.setPreLoadNumber(preLoadNumber);
    }

    public boolean isLoading() {
        if (isEnabledLoadMore()) return loadMoreAdapterDelegate.isLoading();
        return false;
    }

    public LoadMoreStatus loadMoreStatus() {
        if (isEnabledLoadMore()) return loadMoreAdapterDelegate.loadMoreStatus;
        return null;
    }

    public void setLoadMoreStatus(LoadMoreStatus loadMoreStatus) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.loadMoreStatus = loadMoreStatus;
    }

    public boolean loadMoreToLoading() {
        if (isEnabledLoadMore()) return loadMoreAdapterDelegate.loadMoreToLoading();
        return false;
    }

    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    void loadMoreEnd(boolean gone) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.loadMoreEnd(gone);
    }

    /**
     * Refresh complete
     */
    void loadMoreComplete() {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.loadMoreComplete();
    }

    /**
     * Refresh failed
     */
    void loadMoreFail() {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.loadMoreFail();
    }

    /**
     * Refresh failed
     */
    void loadMoreNoNetwork() {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.loadMoreNoNetwork();
    }

    /**
     * 重置状态
     */
    void reset() {
        if (isEnabledLoadMore()) {
            loadMoreAdapterDelegate.reset();
        }
    }

    public boolean hasLoadMoreView() {
        if (isEnabledLoadMore()) return loadMoreAdapterDelegate.hasLoadMoreView();
        return false;
    }

    void autoLoadMore(int position) {
        if (isEnabledLoadMore()) loadMoreAdapterDelegate.autoLoadMore(position);
    }
}
