package com.hannesdorfmann.adapterdelegates4.loadmore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager;
import com.hannesdorfmann.adapterdelegates4.FootHeadDelegatesManager;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.List;

/**
 * 包含头部或底部适配器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2023/3/6 10:21
 */
public class FootHeadDelegationAdapter<T extends List<?>> extends ListDelegationAdapter<T> {
    protected T headData;
    protected T footData;
    protected FootHeadDelegatesManager footHeadDelegatesManager;

    public FootHeadDelegationAdapter() {
        this(new FootHeadDelegatesManager<T>());
    }

    public FootHeadDelegationAdapter(@NonNull AdapterDelegatesManager<T> delegatesManager) {
        super(delegatesManager);
        if (!(delegatesManager instanceof FootHeadDelegatesManager)) {
            throw new IllegalArgumentException("delegatesManager must be extends FootHeadDelegatesManager");
        }
        footHeadDelegatesManager = (FootHeadDelegatesManager) delegatesManager;
    }

    public void setHeadData(T headData) {
        this.headData = headData;
    }

    public void setFootData(T footData) {
        this.footData = footData;
    }

    protected boolean hasHeadData() {
        return headData != null && !headData.isEmpty();
    }

    protected boolean hasFootData() {
        return footData != null && !footData.isEmpty();
    }

    protected boolean hasItemData() {
        return items != null && !items.isEmpty();
    }

    protected boolean isHeadData(int position) {
        return hasHeadData() && position < headData.size();
    }

    public int headCount() {
        return hasHeadData() ? headData.size() : 0;
    }

    public int footCount() {
        return hasFootData() ? footData.size() : 0;
    }

    public ListDelegationAdapter<T> addHeadDelegate(@NonNull AdapterDelegate<T>... delegates) {
        for (AdapterDelegate<T> delegate : delegates) {
            footHeadDelegatesManager.addHeadDelegate(delegate);
        }
        return this;
    }

    public ListDelegationAdapter<T> addHeadDelegate(@NonNull AdapterDelegate<T> delegate) {
        footHeadDelegatesManager.addHeadDelegate(delegate);
        return this;
    }

    public ListDelegationAdapter<T> addHeadDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        footHeadDelegatesManager.addHeadDelegate(viewType, delegate);
        return this;
    }

    public ListDelegationAdapter<T> addFootDelegate(@NonNull AdapterDelegate<T>... delegates) {
        for (AdapterDelegate<T> delegate : delegates) {
            footHeadDelegatesManager.addFootDelegate(delegate);
        }
        return this;
    }

    public ListDelegationAdapter<T> addFootDelegate(@NonNull AdapterDelegate<T> delegate) {
        footHeadDelegatesManager.addFootDelegate(delegate);
        return this;
    }

    public ListDelegationAdapter<T> addFootDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        footHeadDelegatesManager.addFootDelegate(viewType, delegate);
        return this;
    }

    @Override
    public int contentPosition() {
        return headCount() + itemCount();
    }

    public int itemCount() {
        return hasItemData() ? items.size() : 0;
    }

    protected boolean isFootData(int position) {
        int itemCount = itemCount();
        int headCount = headCount();
        return position >= (itemCount + headCount) && position < (itemCount + headCount + footCount());
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount + headCount() + footCount();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(getItems(position), getRealPosition(position), holder, null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        delegatesManager.onBindViewHolder(getItems(position), getRealPosition(position), holder, payloads);
    }

    @Override
    public void setItems(@Nullable T items) {
        super.setItems(items);
    }

    @Override
    public <I> I getItem(int position) {
        T items = getItems(position);
        if (items != null && !items.isEmpty()) {
            if (position >= 0 && position < items.size()) {
                return (I) items.get(position);
            }
        }
        return null;
    }

    public int getRealPosition(int position) {
        if (isHeadData(position)) {
            return position;
        } else if (isFootData(position)) {
            return position - headCount() - itemCount();
        }
        return position - headCount();
    }

    public T getItems(int position) {
        T items = this.items;
        if (isHeadData(position)) {
            items = headData;
        } else if (isFootData(position)) {
            items = footData;
        }
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(getItems(position), getRealPosition(position));
    }
}
