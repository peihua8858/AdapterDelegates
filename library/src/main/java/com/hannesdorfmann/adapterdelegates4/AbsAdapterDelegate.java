package com.hannesdorfmann.adapterdelegates4;

import androidx.annotation.NonNull;

public abstract class AbsAdapterDelegate<T> extends AdapterDelegate<T> {
    @Override
    public boolean isForViewType(@NonNull T item, int position) {
        return isForViewType(item);
    }

    public boolean isForViewType(T item) {
        return false;
    }
}

