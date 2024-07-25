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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncListDiffer;

/**
 * An adapter implementation designed for items organized in a {@link List}. This adapter
 * implementation is ready to go. All you have to do is to add {@link AdapterDelegate}s to the
 * internal {@link AdapterDelegatesManager} i.e. in the constructor:
 *
 * <pre>
 * {@code
 *    class MyAdapter extends AbsDelegationAdapter<List<Foo>>{
 *        public MyAdapter(){
 *            this.delegatesManager.add(new FooAdapterDelegate());
 *            this.delegatesManager.add(new BarAdapterDelegate());
 *        }
 *    }
 * }
 * </pre>
 *
 * @param <T> The type of the items. Must be something that extends from List like List<Foo>
 * @author Hannes Dorfmann
 */
public class ListDelegationAdapter<T> extends AbsDelegationAdapter<T> {
    public ListDelegationAdapter() {
        super();
    }

    public ListDelegationAdapter(@NonNull AdapterDelegatesManager<T> delegatesManager) {
        super(delegatesManager);
    }

    /**
     * Adds a list of {@link AdapterDelegate}s
     *
     * @param delegates
     * @since 4.1.0
     */
    public ListDelegationAdapter(@NonNull AdapterDelegate<T>... delegates) {
        super(delegates);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    /**
     * 如果返回 -1，表示不存在
     *
     * @param item T?
     * @return Int
     */
    public <E> int getItemPosition(E item) {
        if (item != null && items != null && !items.isEmpty()) {
            return items.indexOf(item);
        }
        return -1;
    }

    public ListDelegationAdapter<T> addDelegate(@NonNull AdapterDelegate<T>... delegates) {
        if (delegatesManager == null) {
            delegatesManager = new AdapterDelegatesManager<>(delegates);
        } else {
            for (AdapterDelegate<T> delegate : delegates) {
                delegatesManager.addDelegate(delegate);
            }
        }
        return this;
    }

    public ListDelegationAdapter<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
        delegatesManager.addDelegate(delegate);
        return this;
    }

    public ListDelegationAdapter<T> addDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        delegatesManager.addDelegate(viewType, delegate);
        return this;
    }

    public AdapterDelegate<T> getAdapterDelegate(int viewType) {
        return delegatesManager.getDelegateForViewType(viewType);
    }

    public AdapterDelegate<T> getFallbackDelegate() {
        return delegatesManager.getFallbackDelegate();
    }

    /**
     * Set a fallback delegate that should be used if no {@link AdapterDelegate} has been found that
     * can handle a certain view type.
     *
     * @param fallbackDelegate The {@link AdapterDelegate} that should be used as fallback if no
     *                         other AdapterDelegate has handled a certain view type. <code>null</code> you can set this to
     *                         null if
     *                         you want to remove a previously set fallback AdapterDelegate
     */
    public void setFallbackDelegate(
            @Nullable AdapterDelegate<T> fallbackDelegate) {
        delegatesManager.setFallbackDelegate(fallbackDelegate);
    }

    /**
     * Removes a previously registered delegate if and only if the passed delegate is registered
     * (checks the reference of the object). This will not remove any other delegate for the same
     * viewType (if there is any).
     *
     * @param delegate The delegate to remove
     * @return self
     */
    public void removeDelegate(@NonNull AdapterDelegate<T> delegate) {
        delegatesManager.removeDelegate(delegate);
    }

    /**
     * Removes the adapterDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public void removeDelegate(int viewType) {
        delegatesManager.removeDelegate(viewType);
    }

    public AdapterDelegate<T> getDelegate(int viewType) {
        return delegatesManager.getDelegateForViewType(viewType);
    }

    public int getItemType(AdapterDelegate<T> delegate) {
        return delegatesManager.getViewType(delegate);
    }

    @Nullable
    public T getItem(int position) {
        if (items != null && !items.isEmpty()) {
            if (position >= 0 && position < items.size()) {
                return items.get(position);
            }
        }
        return null;
    }

    @Override
    public void setItems(@Nullable List<T> items) {
        if (items != null) {
            super.setItems(new ArrayList(items));
        }else{
            super.setItems(new ArrayList<>());
        }
        notifyDataSetChanged();
    }

    public void removeAt(@IntRange(from = 0) int position) {
        if (items == null) {
            return;
        }
        if (position >= items.size()) {
            return;
        }
        this.items.remove(position);
        int internalPosition = position;
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, this.items.size() - internalPosition);
    }

    public void remove(Object data) {
        int index = this.items.indexOf(data);
        if (index == -1) {
            return;
        }
        removeAt(index);
    }

    /**
     * add one new data in to certain location
     * 在指定位置添加一条新数据
     *
     * @param position
     */
    public void addData(@IntRange(from = 0) int position, T data) {
        if (items == null) {
            return;
        }
        this.items.add(position, data);
        notifyItemInserted(position);
        compatibilityDataSizeChanged(1);
    }

    /**
     * add one new data
     * 添加一条新数据
     */
    public void addData(@NonNull T data) {
        if (items == null) {
            return;
        }
        this.items.add(data);
        notifyItemInserted(this.items.size());
        compatibilityDataSizeChanged(1);
    }

    /**
     * add new data in to certain location
     * 在指定位置添加数据
     *
     * @param position the insert position
     * @param newData  the new data collection
     */
    public void addData(@IntRange(from = 0) int position, Collection newData) {
        if (items == null) {
            return;
        }
        this.items.addAll(position, newData);
        notifyItemRangeInserted(position, newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    public void addData(@NonNull Collection newData) {
        if (items == null) {
            return;
        }
        this.items.addAll(newData);
        notifyItemRangeInserted(this.items.size() - newData.size(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * compatible getLoadMoreViewCount and getEmptyViewCount may change
     *
     * @param size Need compatible data size
     */
    protected void compatibilityDataSizeChanged(int size) {
        if (items == null) {
            return;
        }
        if (this.items.size() == size) {
            notifyDataSetChanged();
        }
    }

    public int contentPosition() {
        return items != null ? this.items.size() : 0;
    }

    public void addItems(List<T> items) {
        if (items != null && !items.isEmpty()) {
            if (this.items == null || this.items.isEmpty()) {
                setItems(items);
            } else {
                int position = contentPosition();
                this.items.addAll((Collection) items);
                notifyItemRangeInserted(position, items.size());
                if (this.items.size() == items.size()) {
                    notifyDataSetChanged();
                }
            }
        }
    }
}
