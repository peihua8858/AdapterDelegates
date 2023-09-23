package com.hannesdorfmann.adapterdelegates4;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class AbsAdapterDelegate<T> extends AdapterDelegate<T> {
    @Override
    public boolean isForViewType(List<T> items, int position) {
        return isForViewType(items.get(position));
    }

    public boolean isForViewType(T item) {
        return false;
    }

    @Override
    public void onBindViewHolder(T item, int position, RecyclerView.ViewHolder holder, List<Object> payloads) {
        onBindViewHolder(item, position, holder);
    }

    abstract public void onBindViewHolder(T item, int position, RecyclerView.ViewHolder holder);
}

