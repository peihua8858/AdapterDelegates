package com.hannesdorfmann.adapterdelegates4.loadmore;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hannesdorfmann.adapterdelegates4.AbsAdapterDelegate;

import java.util.List;

public abstract class LoadMoreAdapterDelegate<T> extends AbsAdapterDelegate<T> {
    private LoadMoreDelegationAdapter mAdapter;
    protected LoadMoreStatus loadMoreStatus = LoadMoreStatus.Complete;
    /**
     * 加载完成后是否允许点击
     */
    protected boolean enableLoadMoreEndClick = false;
    /**
     * 是否打开自动加载更多
     */
    protected boolean isAutoLoadMore = true;
    /**
     * 当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多
     */
    protected boolean isEnableLoadMoreIfNotFullPage = true;
    /**
     * 预加载
     */
    protected int preLoadNumber = 1;
    private boolean isEnableLoadMore = false;
    protected OnLoadMoreListener mLoadMoreListener;
    /**
     * 不满一屏时，是否可以继续加载的标记位
     */
    private boolean mNextLoadEnable = true;

    private boolean isLoadEndMoreGone = false;

    public boolean isLoading() {
        return loadMoreStatus == LoadMoreStatus.Loading;
    }

    public void setAdapter(LoadMoreDelegationAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mLoadMoreListener = onLoadMoreListener;
        setEnableLoadMore(true);
    }

    public void setPreLoadNumber(int preLoadNumber) {
        this.preLoadNumber = preLoadNumber;
    }

    @Override
    public boolean isForViewType(@NonNull T item, int position) {
        return mAdapter.isLoadMoreData(position);
    }

    @NonNull
    @Override
    protected final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        View rootView = getRootView(parent);
        rootView.setOnClickListener(v -> {
            if (loadMoreStatus == LoadMoreStatus.Fail) {
                loadMoreToLoading();
            } else if (loadMoreStatus == LoadMoreStatus.Complete) {
                loadMoreToLoading();
            } else if (enableLoadMoreEndClick && loadMoreStatus == LoadMoreStatus.End) {
                loadMoreToLoading();
            }
        });
        return new RecyclerView.ViewHolder(rootView) {
        };
    }

    @Override
    public void bindViewHolder(@NonNull List<T> items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        convert(holder, position);
    }

    @Override
    public final void onBindViewHolder(@NonNull T item, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        convert(holder, position);
    }

    public boolean hasLoadMoreView() {
        if (mLoadMoreListener == null || !isEnableLoadMore) {
            return false;
        }
        if (loadMoreStatus == LoadMoreStatus.End && isLoadEndMoreGone) {
            return false;
        }
        return mAdapter.itemCount() > 0;
    }

    public boolean loadMoreToLoading() {
        if (isLoading()) {
            return false;
        }
        loadMoreStatus = LoadMoreStatus.Loading;
        mAdapter.notifyItemChanged(mAdapter.loadMoreViewPosition());
        invokeLoadMoreListener();
        return true;
    }

    /**
     * 触发加载更多监听
     */
    private void invokeLoadMoreListener() {
        if (mLoadMoreListener != null) {
            loadMoreStatus = LoadMoreStatus.Loading;
            RecyclerView recyclerView = mAdapter.recyclerView;
            if (recyclerView != null) {
                recyclerView.post(() -> mLoadMoreListener.onLoadMore());
            }
        }
    }

    /**
     * 根布局
     *
     * @param parent ViewGroup
     * @return View
     */
    public abstract View getRootView(@NonNull ViewGroup parent);

    /**
     * 布局中的 加载更多视图
     *
     * @param holder BaseViewHolder
     * @return View
     */
    public abstract View getLoadingView(RecyclerView.ViewHolder holder);

    /**
     * 布局中的 加载完成布局
     *
     * @param holder BaseViewHolder
     * @return View
     */
    public abstract View getLoadComplete(RecyclerView.ViewHolder holder);

    /**
     * 布局中的 加载结束布局
     *
     * @param holder BaseViewHolder
     * @return View
     */
    public abstract View getLoadEndView(RecyclerView.ViewHolder holder);

    /**
     * 布局中的 加载失败布局
     *
     * @param holder BaseViewHolder
     * @return View
     */
    public abstract View getLoadFailView(RecyclerView.ViewHolder holder);

    /**
     * 可重写此方式，实行自定义逻辑
     *
     * @param holder   BaseViewHolder
     * @param position Int
     */
    void convert(RecyclerView.ViewHolder holder, int position) {
        autoLoadMore(position);
        switch (loadMoreStatus) {
            case Complete:
                isVisible(getLoadingView(holder), false);
                isVisible(getLoadComplete(holder), true);
                isVisible(getLoadFailView(holder), false);
                isVisible(getLoadEndView(holder), false);
                break;
            case Loading:
                isVisible(getLoadingView(holder), true);
                isVisible(getLoadComplete(holder), false);
                isVisible(getLoadFailView(holder), false);
                isVisible(getLoadEndView(holder), false);
                break;
            case Fail:
                isVisible(getLoadingView(holder), false);
                isVisible(getLoadComplete(holder), false);
                isVisible(getLoadFailView(holder), true);
                isVisible(getLoadEndView(holder), false);
                break;
            case End:
                isVisible(getLoadingView(holder), false);
                isVisible(getLoadComplete(holder), false);
                isVisible(getLoadFailView(holder), false);
                isVisible(getLoadEndView(holder), true);
                break;
            default:
                break;
        }
    }

    private void isVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setEnableLoadMore(boolean isEnableLoadMore) {
        boolean oldHasLoadMore = hasLoadMoreView();
        this.isEnableLoadMore = isEnableLoadMore;
        boolean newHasLoadMore = hasLoadMoreView();
        if (oldHasLoadMore) {
            if (!newHasLoadMore) {
                mAdapter.notifyItemRemoved(mAdapter.loadMoreViewPosition());
            }
        } else {
            if (newHasLoadMore) {
                loadMoreStatus = LoadMoreStatus.Complete;
                mAdapter.notifyItemInserted(mAdapter.loadMoreViewPosition());
            }
        }
    }

    /**
     * 自动加载数据
     *
     * @param position
     * @author dingpeihua
     * @date 2023/3/7 9:26
     * @version 1.0
     */
    private void autoLoadMore(int position) {
        if (!isAutoLoadMore) {
            //如果不需要自动加载更多，直接返回
            return;
        }
        if (!hasLoadMoreView()) {
            return;
        }
        if (position < mAdapter.itemCount() - preLoadNumber) {
            return;
        }
        if (loadMoreStatus != LoadMoreStatus.Complete) {
            return;
        }
        if (isLoading()) {
            return;
        }
        if (!mNextLoadEnable) {
            return;
        }
        invokeLoadMoreListener();
    }

    /**
     * check if full page after [BaseQuickAdapter.setNewInstance] [BaseQuickAdapter.setList],
     * if full, it will enable load more again.
     * <p>
     * 用来检查数据是否满一屏，如果满足条件，再开启
     */
    void checkDisableLoadMoreIfNotFullPage() {
        if (isEnableLoadMoreIfNotFullPage) {
            return;
        }
        // 先把标记位设置为false
        mNextLoadEnable = false;
        RecyclerView recyclerView = mAdapter.recyclerView;
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFullScreen((LinearLayoutManager) manager)) {
                        mNextLoadEnable = true;
                    }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            recyclerView.postDelayed(() -> {
                StaggeredGridLayoutManager manager1 = (StaggeredGridLayoutManager) manager;
                int[] positions = new int[manager1.getSpanCount()];
                manager1.findLastCompletelyVisibleItemPositions(positions);
                int pos = getTheBiggestNumber(positions) + 1;
                if (pos != mAdapter.getItemCount()) {
                    mNextLoadEnable = true;
                }
            }, 50);
        }
    }

    private boolean isFullScreen(LinearLayoutManager llm) {
        return (llm.findLastCompletelyVisibleItemPosition() + 1) != mAdapter.getItemCount() ||
                llm.findFirstCompletelyVisibleItemPosition() != 0;
    }

    private int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    /**
     * Refresh end, no more data
     *
     * @param gone if true gone the load more view
     */
    void loadMoreEnd(boolean gone) {
        if (!hasLoadMoreView()) {
            return;
        }
//        mNextLoadEnable = false
        isLoadEndMoreGone = gone;
        loadMoreStatus = LoadMoreStatus.End;
        if (gone) {
            mAdapter.notifyItemRemoved(mAdapter.loadMoreViewPosition());
        } else {
            mAdapter.notifyItemChanged(mAdapter.loadMoreViewPosition());
        }
    }

    /**
     * Refresh complete
     */
    void loadMoreComplete() {
        if (!hasLoadMoreView()) {
            return;
        }
        loadMoreStatus = LoadMoreStatus.Complete;
        mAdapter.notifyItemChanged(mAdapter.loadMoreViewPosition());
        checkDisableLoadMoreIfNotFullPage();
    }

    /**
     * Refresh failed
     */
    void loadMoreFail() {
        if (!hasLoadMoreView()) {
            return;
        }
        loadMoreStatus = LoadMoreStatus.Fail;
        mAdapter.notifyItemChanged(mAdapter.loadMoreViewPosition());
    }

    /**
     * 重置状态
     */
    void reset() {
        if (mLoadMoreListener != null) {
            isEnableLoadMore = true;
            loadMoreStatus = LoadMoreStatus.Complete;
        }
    }
}
