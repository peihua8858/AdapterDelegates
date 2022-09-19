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
 * An implementation of an Adapter that already uses a {@link AdapterDelegatesManager} pretty same as
 * {@link AbsDelegationAdapter} but also uses {@link AsyncListDiffer} from support library 27.0.1 for
 * calculating diffs between old and new collections of items and does this on background thread.
 * That means that now you should not carry about {@link RecyclerView.Adapter#notifyItemChanged(int)}
 * and other methods of adapter, all you need to do is to submit a new list into adapter and all diffs will be
 * calculated for you.
 * You just have to add the {@link AdapterDelegate}s i.e. in the constructor of a subclass that inheritance from this
 * class:
 * <pre>
 * {@code
 *    class MyAdapter extends AsyncListDifferDelegationAdapter<MyDataSourceType> {
 *        public MyAdapter() {
 *            this.delegatesManager.add(new FooAdapterDelegate())
 *                                 .add(new BarAdapterDelegate());
 *        }
 *    }
 * }
 * </pre>
 *
 * @param <T> The type of the datasource / items. Internally we will use List&lt;T&gt; but you only have
 *            to provide T (and not List&lt;T&gt;). Its safe to use this with
 *            {@link AbsListItemAdapterDelegate}.
 * @author Sergey Opivalov
 * @author Hannes Dorfmann
 */

public class AsyncListDifferDelegationAdapter<T> extends RecyclerView.Adapter {

    protected final AdapterDelegatesManager<List<T>> delegatesManager;
    protected final AsyncListDiffer<T> differ;

    public AsyncListDifferDelegationAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        this(diffCallback, new AdapterDelegatesManager<List<T>>());
    }

    public AsyncListDifferDelegationAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                            @NonNull AdapterDelegatesManager<List<T>> delegatesManager) {

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
                                            @NonNull AdapterDelegatesManager<List<T>> delegatesManager) {

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
                                            @NonNull AdapterDelegate<List<T>>... delegates) {

        if (diffCallback == null) {
            throw new NullPointerException("ItemCallback is null");
        }

        this.differ = new AsyncListDiffer<T>(this, diffCallback);
        this.delegatesManager = new AdapterDelegatesManager<List<T>>(delegates);
    }


    /**
     * Adds a list of {@link AdapterDelegate}s
     *
     * @param delegates
     * @since 4.2.0
     */
    public AsyncListDifferDelegationAdapter(@NonNull AsyncDifferConfig differConfig,
                                            @NonNull AdapterDelegate<List<T>>... delegates) {

        if (differConfig == null) {
            throw new NullPointerException("AsyncDifferConfig is null");
        }

        this.differ = new AsyncListDiffer<T>(new AdapterListUpdateCallback(this), differConfig);
        this.delegatesManager = new AdapterDelegatesManager<List<T>>(delegates);
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

    public SparseArrayCompat<AdapterDelegate<List<T>>> getDeletes() {
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
        SparseArrayCompat<AdapterDelegate<List<T>>> delegates = getDeletes();
        if (delegates.isEmpty()) {
            return;
        }
        for (int i = 0; i < delegates.size(); i++) {
            int key = delegates.keyAt(i);
            AdapterDelegate<List<T>> delegate = delegates.get(key);
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
        SparseArrayCompat<AdapterDelegate<List<T>>> delegates = getDeletes();
        if (delegates.isEmpty()) {
            return;
        }
        for (int i = 0; i < delegates.size(); i++) {
            int key = delegates.keyAt(i);
            AdapterDelegate<List<T>> delegate = delegates.get(key);
            if (delegate != null) {
                delegate.onRestoreInstanceState(state);
            }
        }
    }
}
