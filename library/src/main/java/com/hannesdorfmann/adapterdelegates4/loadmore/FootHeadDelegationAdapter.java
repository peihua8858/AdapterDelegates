package com.hannesdorfmann.adapterdelegates4.loadmore;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager;
import com.hannesdorfmann.adapterdelegates4.FootHeadDelegatesManager;
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 包含头部或底部适配器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2023/3/6 10:21
 */
public class FootHeadDelegationAdapter<T> extends ListDelegationAdapter<T> {
    protected List<T> headData;
    protected List<T> footData;
    protected final FootHeadDelegatesManager footHeadDelegatesManager;

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

    public void setHeadData(List<T> headData) {
        this.headData = headData;
    }

    public void setFootData(List<T> footData) {
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
    public void setItems(@Nullable List<T> items) {
        super.setItems(items);
    }

    @Override
    public <I> I getItem(int position) {
        int realPosition = getRealPosition(position);
        if (realPosition < 0) {
            return null;
        }
        if (isHeadData(position)) {
            if (headData != null && headData.size() > realPosition) {
                return (I) headData.get(realPosition);
            }
            return null;
        } else if (isFootData(position)) {
            if (footData != null && footData.size() > realPosition) {
                return (I) footData.get(realPosition);
            }
            return null;
        }
        return super.getItem(realPosition);
    }

    public int getRealPosition(int position) {
        if (isHeadData(position)) {
            return position;
        } else if (isFootData(position)) {
            return position - headCount() - itemCount();
        }
        return position - headCount();
    }

    public List<T> getItems(int position) {
        List<T> items = this.items;
        if (isHeadData(position)) {
            items = headData;
        } else if (isFootData(position)) {
            items = footData;
        }
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        List<T> items = getItems(position);
        if (items == null || items.isEmpty()) {
            return -1;
        }
        return delegatesManager.getItemViewType(getItems(position), getRealPosition(position));
    }

    @Override
    public void removeAt(@IntRange(from = 0) int position) {
        if (items == null) {
            return;
        }
        if (position >= items.size()) {
            return;
        }
        this.items.remove(position);
        int internalPosition = position + headCount();
        notifyItemRemoved(internalPosition);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(internalPosition, this.items.size() - internalPosition);
    }

    @Override
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
    @Override
    public void addData(@IntRange(from = 0) int position, T data) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items.add(position, data);
        notifyItemInserted(position + headCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * add one new data
     * 添加一条新数据
     */
    @Override
    public void addData(@NonNull T data) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items.add(data);
        notifyItemInserted(this.items.size() + headCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * add new data in to certain location
     * 在指定位置添加数据
     *
     * @param position the insert position
     * @param newData  the new data collection
     */
    @Override
    public void addData(@IntRange(from = 0) int position, Collection newData) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items.addAll(position, newData);
        notifyItemRangeInserted(position + headCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    @Override
    public void addData(@NonNull Collection newData) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items.addAll(newData);
        notifyItemRangeInserted(this.items.size() - newData.size() + headCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }
}
