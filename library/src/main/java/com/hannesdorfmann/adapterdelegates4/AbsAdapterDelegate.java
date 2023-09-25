package com.hannesdorfmann.adapterdelegates4;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class AbsAdapterDelegate<T> extends AdapterDelegate<T> {
    @Override
    public boolean isForViewType(List<T> items, int position) {
        return isForViewType(items.get(position),items,position);
    }

    @Override
    protected boolean isForViewType(@NonNull T item, @NonNull List<T> items, int position) {
        return isForViewType(item);
    }

    public boolean isForViewType(T item) {
        return false;
    }


//    @Override
//    public void onBindViewHolder(T item, int position, RecyclerView.ViewHolder holder, List<Object> payloads) {
//        onBindViewHolder(item, position, holder);
//    }

}

