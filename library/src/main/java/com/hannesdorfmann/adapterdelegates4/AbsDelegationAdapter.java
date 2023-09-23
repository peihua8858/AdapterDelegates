/*
 * Copyright (c) 2015 Hannes Dorfmann.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hannesdorfmann.adapterdelegates4;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An implementation of an Adapter that already uses a {@link AdapterDelegatesManager} and calls
 * the corresponding {@link AdapterDelegatesManager} methods from Adapter's method like {@link
 * #onCreateViewHolder(ViewGroup, int)}, {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}
 * and {@link #getItemViewType(int)}. So everything is already setup for you. You just have to add
 * the {@link AdapterDelegate}s i.e. in the constructor of a subclass that inheritance from this
 * class:
 * <pre>
 * {@code
 *    class MyAdapter extends AbsDelegationAdapter<MyDataSourceType>{
 *        public MyAdapter(){
 *            this.delegatesManager.add(new FooAdapterDelegate());
 *            this.delegatesManager.add(new BarAdapterDelegate());
 *        }
 *    }
 * }
 * </pre>
 * <p>
 * or you can pass a already prepared {@link AdapterDelegatesManager} via constructor like this:
 * <pre>
 * {@code
 *    class MyAdapter extends AbsDelegationAdapter<MyDataSourceType>{
 *        public MyAdapter(AdapterDelegatesManager manager){
 *          super(manager)
 *        }
 *    }
 * }
 * </pre>
 *
 * @param <T> The type of the datasource / items
 * @author Hannes Dorfmann
 */
public abstract class AbsDelegationAdapter<T> extends RecyclerView.Adapter {

    protected AdapterDelegatesManager<T> delegatesManager;
    @Nullable
    protected List<T> items;

    public AbsDelegationAdapter() {
        this(new AdapterDelegatesManager<T>());
    }

    public AbsDelegationAdapter(@NonNull AdapterDelegatesManager<T> delegatesManager) {
        if (delegatesManager == null) {
            throw new NullPointerException("AdapterDelegatesManager is null");
        }

        this.delegatesManager = delegatesManager;
    }

    /**
     * Adds a list of {@link AdapterDelegate}s
     *
     * @param delegates Items to add
     * @since 4.1.0
     */
    public AbsDelegationAdapter(@NonNull AdapterDelegate<T>... delegates) {
        this(new AdapterDelegatesManager<T>(delegates));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder, null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        delegatesManager.onBindViewHolder(items, position, holder, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
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
    @Nullable
    public List<T> getItems() {
        return items;
    }

    /**
     * Set the items / data source of this adapter
     *
     * @param items The items / data source
     */
    public void setItems(@Nullable List<T> items) {
        this.items = items;
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
