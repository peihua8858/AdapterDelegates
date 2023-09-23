package com.hannesdorfmann.adapterdelegates4;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 已经使用 {@link AdapterDelegatesManager} 的适配器的实现与以下内容非常相同
 * {@link AbsDelegationAdapter} 但也使用支持库 27.0.1 中的 {@link AsyncListDiffer}
 * 计算新旧项目集合之间的差异，并在后台线程上执行此操作。
 * 这意味着现在你不应该携带 {@link RecyclerView.Adapter#notifyItemChanged(int)}
 * 和适配器的其他方法一样，您所需要做的就是向适配器提交一个新列表，所有差异都会被
 * 为您计算。
 * 你只需添加 {@link AdapterDelegate} 即可，即在继承于此的子类的构造函数中
 * 班级：
 * <前>
 * {@代码
 * class MyAdapter extends AsyncListDifferDelegationAdapter<MyDataSourceType> {
 * public MyAdapter（）{
 * this.delegatesManager.add(new FooAdapterDelegate())
 * .add(new BarAdapterDelegate());
 * }
 * }
 * }
 * </前>
 *
 * @param <T> 数据源/项目的类型。在内部我们将使用 List<T> 但你只有
 *            提供 T （而不是 List<T>）。与它一起使用是安全的 {@link AbsListItemAdapterDelegate}。
 * @author Sergey Opivalov
 * @author Hannes Dorfmann
 */

public class AsyncListDifferDelegationAdapter<T> extends RecyclerView.Adapter {

    protected final AdapterDelegatesManager<T> delegatesManager;
    protected final AsyncListDiffer<T> differ;

    public AsyncListDifferDelegationAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        this(diffCallback, new AdapterDelegatesManager<T>());
    }

    public AsyncListDifferDelegationAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                            @NonNull AdapterDelegatesManager<T> delegatesManager) {

        if (diffCallback == null) {
            throw new NullPointerException("ItemCallback is null");
        }

        if (delegatesManager == null) {
            throw new NullPointerException("AdapterDelegatesManager is null");
        }
        this.differ = new AsyncListDiffer<T>(this, diffCallback);
        this.delegatesManager = delegatesManager;
    }

    public AsyncListDifferDelegationAdapter(@NonNull AsyncDifferConfig differConfig,
                                            @NonNull AdapterDelegatesManager<T> delegatesManager) {

        if (differConfig == null) {
            throw new NullPointerException("AsyncDifferConfig is null");
        }

        if (delegatesManager == null) {
            throw new NullPointerException("AdapterDelegatesManager is null");
        }

        this.differ = new AsyncListDiffer<T>(new AdapterListUpdateCallback(this), differConfig);
        this.delegatesManager = delegatesManager;
    }

    /**
     * Adds a list of {@link AdapterDelegate}s
     *
     * @param delegates
     * @since 4.2.0
     */
    public AsyncListDifferDelegationAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                            @NonNull AdapterDelegate<T>... delegates) {

        if (diffCallback == null) {
            throw new NullPointerException("ItemCallback is null");
        }

        this.differ = new AsyncListDiffer<T>(this, diffCallback);
        this.delegatesManager = new AdapterDelegatesManager<T>(delegates);
    }


    /**
     * Adds a list of {@link AdapterDelegate}s
     *
     * @param delegates
     * @since 4.2.0
     */
    public AsyncListDifferDelegationAdapter(@NonNull AsyncDifferConfig differConfig,
                                            @NonNull AdapterDelegate<T>... delegates) {

        if (differConfig == null) {
            throw new NullPointerException("AsyncDifferConfig is null");
        }

        this.differ = new AsyncListDiffer<T>(new AdapterListUpdateCallback(this), differConfig);
        this.delegatesManager = new AdapterDelegatesManager<T>(delegates);
    }

    public AsyncListDifferDelegationAdapter<T> addDelegate(@NonNull AdapterDelegate<T>... delegates) {
        for (AdapterDelegate<T> delegate : delegates) {
            delegatesManager.addDelegate(delegate);
        }
        return this;
    }

    public AsyncListDifferDelegationAdapter<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
        delegatesManager.addDelegate(delegate);
        return this;
    }

    public AsyncListDifferDelegationAdapter<T> addDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        delegatesManager.addDelegate(viewType, delegate);
        return this;
    }

    public AdapterDelegate<T> getAdapterDelegate(int viewType) {
        return delegatesManager.getDelegateForViewType(viewType);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(differ.getCurrentList(), position, holder, null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        delegatesManager.onBindViewHolder(differ.getCurrentList(), position, holder, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(differ.getCurrentList(), position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        delegatesManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return delegatesManager.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        delegatesManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        delegatesManager.onViewDetachedFromWindow(holder);
    }

    public SparseArrayCompat<AdapterDelegate<T>> getDeletes() {
        return delegatesManager.delegates;
    }

    /**
     * Get the items / data source of this adapter
     *
     * @return The items / data source
     */
    public List<T> getItems() {
        return differ.getCurrentList();
    }

    /**
     * Set the items / data source of this adapter
     *
     * @param items The items / data source
     */
    public void setItems(List<T> items) {
        differ.submitList(items);
    }

    /**
     * Set the items / data source of this adapter
     *
     * @param items          The items / data source
     * @param commitCallback Runnable that is executed when the List is committed, if it is committed
     */
    public void setItems(List<T> items, Runnable commitCallback) {
        differ.submitList(items, commitCallback);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    /**
     * Called to ask the delegate to save its current dynamic state, so it
     * can later be reconstructed in a new instance if its process is
     * restarted.  If a new instance of the delegate later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onRestoreInstanceState(Bundle)}.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle outState) {
        SparseArrayCompat<AdapterDelegate<T>> delegates = getDeletes();
        if (delegates.isEmpty()) {
            return;
        }
        for (int i = 0; i < delegates.size(); i++) {
            int key = delegates.keyAt(i);
            AdapterDelegate<T> delegate = delegates.get(key);
            if (delegate != null) {
                delegate.onSaveInstanceState(outState);
            }
        }
    }

    /**
     * The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     *
     * @param state the data most recently supplied in {@link #onSaveInstanceState}.
     */
    @CallSuper
    public void onRestoreInstanceState(Bundle state) {
        SparseArrayCompat<AdapterDelegate<T>> delegates = getDeletes();
        if (delegates.isEmpty()) {
            return;
        }
        for (int i = 0; i < delegates.size(); i++) {
            int key = delegates.keyAt(i);
            AdapterDelegate<T> delegate = delegates.get(key);
            if (delegate != null) {
                delegate.onRestoreInstanceState(state);
            }
        }
    }
}
