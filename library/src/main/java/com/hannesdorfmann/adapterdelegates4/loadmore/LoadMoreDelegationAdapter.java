package com.hannesdorfmann.adapterdelegates4.loadmore;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager;

import java.util.List;

/**
 * 加载更多适配器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2023/3/6 10:21
 */
public class LoadMoreDelegationAdapter<T extends List> extends FootHeadDelegationAdapter<T> {
    protected final LoadMoreDelegatesManager loadMoreDelegatesManager;

    protected RecyclerView recyclerView = null;


    public LoadMoreDelegationAdapter() {
        this(new LoadMoreDelegatesManager<>());
    }

    public LoadMoreDelegationAdapter(@NonNull AdapterDelegatesManager<T> delegatesManager) {
        super(delegatesManager);
        if (!(delegatesManager instanceof LoadMoreDelegatesManager)) {
            throw new IllegalArgumentException("delegatesManager must be extends LoadMoreDelegatesManager");
        }
        loadMoreDelegatesManager = (LoadMoreDelegatesManager) delegatesManager;
    }

    protected boolean isFixedViewType(int type, int position) {
        return type == LoadMoreDelegatesManager.LOAD_MORE_ITEM_VIEW_TYPE;
    }

    /**
     * When set to true, the item will layout using all span area. That means, if orientation
     * is vertical, the view will have full width; if orientation is horizontal, the view will
     * have full height.
     * if the hold view use StaggeredGridLayoutManager they should using all span area
     *
     * @param holder
     * @author dingpeihua
     * @date 2023/3/8 10:15
     * @version 1.0
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
        }
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) manager;
            GridLayoutManager.SpanSizeLookup defSpanSizeLookup = layoutManager.getSpanSizeLookup();
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (isFixedViewType(type, position)) {
                        return layoutManager.getSpanCount();
                    }
                    return defSpanSizeLookup.getSpanSize(position);
                }
            });
        }
    }

    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (isFixedViewType(type, holder.getBindingAdapterPosition())) {
            setFullSpan(holder);
        }
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void setLoadMoreDelegate(@NonNull LoadMoreAdapterDelegate loadMoreDelegate) {
        loadMoreDelegate.setAdapter(this);
        loadMoreDelegatesManager.setLoadMoreAdapterDelegate(loadMoreDelegate);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        loadMoreDelegatesManager.setOnLoadMoreListener(onLoadMoreListener);
    }

    public int loadMoreViewPosition() {
        return headCount() + itemCount() + footCount();
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        if (loadMoreDelegatesManager != null && loadMoreDelegatesManager.hasLoadMoreView()) {
            return itemCount + 1;
        }
        return super.getItemCount();
    }

    protected boolean isLoadMoreData(int position) {
        return position == (itemCount() + headCount() + footCount());
    }

    @Override
    public int getRealPosition(int position) {
        if (isLoadMoreData(position)) {
            return position;
        }
        return super.getRealPosition(position);
    }

    @Override
    public void setItems(@Nullable T items) {
        reset();
        super.setItems(items);
    }

    @Override
    public T getItems(int position) {
        if (isLoadMoreData(position)) {
            return null;
        }
        return super.getItems(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (loadMoreDelegatesManager.hasLoadMoreView() && isLoadMoreData(position)) {
            return LoadMoreDelegatesManager.LOAD_MORE_ITEM_VIEW_TYPE;
        }
        return delegatesManager.getItemViewType(getItems(position), getRealPosition(position));
    }

    public void setEnableLoadMore(boolean isEnableLoadMore) {
        if (loadMoreDelegatesManager != null) {
            loadMoreDelegatesManager.setEnableLoadMore(isEnableLoadMore);
        }
    }

    public void loadMoreToLoading() {
        if (loadMoreDelegatesManager == null) {
            return;
        }
        loadMoreDelegatesManager.loadMoreToLoading();
    }


    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    public void loadMoreEnd(boolean gone) {
        if (loadMoreDelegatesManager != null) {
            loadMoreDelegatesManager.loadMoreEnd(gone);
        }
    }

    /**
     * Refresh complete
     */
    public void loadMoreComplete() {
        if (loadMoreDelegatesManager != null) {
            loadMoreDelegatesManager.loadMoreComplete();
        }
    }

    /**
     * Refresh failed
     */
    public void loadMoreFail() {
        if (loadMoreDelegatesManager != null) {
            loadMoreDelegatesManager.loadMoreFail();
        }
    }

    /**
     * 重置状态
     */
    public void reset() {
        if (loadMoreDelegatesManager != null) {
            loadMoreDelegatesManager.reset();
        }
    }
}
