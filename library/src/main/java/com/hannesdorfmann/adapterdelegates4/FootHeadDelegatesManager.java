package com.hannesdorfmann.adapterdelegates4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import java.util.List;

/**
 * 加载更多委托管理器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2023/3/6 10:22
 */
public class FootHeadDelegatesManager<T> extends AdapterDelegatesManager<T> {
    /**
     * 加载更多
     */
    public static final int LOAD_MORE_ITEM_VIEW_TYPE = FALLBACK_DELEGATE_VIEW_TYPE - 1;
    /**
     * 头部和底部类型起始值
     */
    public static final int FOOT_ITEM_VIEW_TYPE = LOAD_MORE_ITEM_VIEW_TYPE - 1;
    public static final int HEAD_ITEM_VIEW_TYPE = Integer.MIN_VALUE + 1;
    /**
     * Map for ViewType to AdapterDelegate for header
     */
    protected SparseArrayCompat<AdapterDelegate<T>> headerDelegates = new SparseArrayCompat();
    /**
     * Map for ViewType to AdapterDelegate for footer
     */
    protected SparseArrayCompat<AdapterDelegate<T>> footerDelegates = new SparseArrayCompat();

    public AdapterDelegatesManager<T> addHeadDelegate(@NonNull AdapterDelegate<T> delegate) {
        int viewType = delegate.getItemType();
        if (viewType == -1) {
            // algorithm could be improved since there could be holes,
            // but it's very unlikely that we reach Integer.MAX_VALUE and run out of unused indexes
            viewType = HEAD_ITEM_VIEW_TYPE + headerDelegates.size();
            while (headerDelegates.get(viewType) != null) {
                viewType++;
                if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
                    throw new IllegalArgumentException(
                            "Oops, we are very close to Integer.MAX_VALUE. It seems that there are no more free and unused view type integers left to add another AdapterDelegate.");
                }
            }
        }
        return addHeadDelegate(viewType, false, delegate);
    }

    public AdapterDelegatesManager<T> addHeadDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        return addHeadDelegate(viewType, false, delegate);
    }

    public AdapterDelegatesManager<T> addHeadDelegate(int viewType, boolean allowReplacingDelegate, @NonNull AdapterDelegate<T> delegate) {
        if (!allowReplacingDelegate && headerDelegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered AdapterDelegate is "
                            + headerDelegates.get(viewType));
        }
        headerDelegates.put(viewType, delegate);
        return this;
    }

    public AdapterDelegatesManager<T> addFootDelegate(@NonNull AdapterDelegate<T> delegate) {
        int viewType = delegate.getItemType();
        if (viewType == -1) {
            // algorithm could be improved since there could be holes,
            // but it's very unlikely that we reach Integer.MAX_VALUE and run out of unused indexes
            viewType = FOOT_ITEM_VIEW_TYPE - footerDelegates.size();
            while (footerDelegates.get(viewType) != null) {
                viewType--;
                if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
                    throw new IllegalArgumentException(
                            "Oops, we are very close to Integer.MAX_VALUE. It seems that there are no more free and unused view type integers left to add another AdapterDelegate.");
                }
            }
        }
        return addFootDelegate(viewType, false, delegate);
    }

    public AdapterDelegatesManager<T> addFootDelegate(int viewType, @NonNull AdapterDelegate<T> delegate) {
        return addHeadDelegate(viewType, false, delegate);
    }

    public AdapterDelegatesManager<T> addFootDelegate(int viewType, boolean allowReplacingDelegate, @NonNull AdapterDelegate<T> delegate) {
        if (!allowReplacingDelegate && footerDelegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered AdapterDelegate is "
                            + footerDelegates.get(viewType));
        }
        footerDelegates.put(viewType, delegate);
        return this;
    }

    public AdapterDelegatesManager<T> removeHeadDelegate(@NonNull AdapterDelegate<T> delegate) {
        int indexToRemove = headerDelegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            headerDelegates.removeAt(indexToRemove);
        }
        return this;
    }

    public AdapterDelegatesManager<T> removeFootDelegate(@NonNull AdapterDelegate<T> delegate) {
        int indexToRemove = footerDelegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            footerDelegates.removeAt(indexToRemove);
        }
        return this;
    }

    public AdapterDelegatesManager<T> removeHeadDelegate(int viewType) {
        headerDelegates.remove(viewType);
        return this;
    }

    public AdapterDelegatesManager<T> removeFootDelegate(int viewType) {
        footerDelegates.remove(viewType);
        return this;
    }

    @Override
    public int getItemViewType(@NonNull T item, int position) {
        if (item == null) {
            throw new NullPointerException("Items datasource is null!");
        }
        Integer itemType = getFootHeadItemViewType(item, position);
        if (itemType != null) {
            return itemType;
        }
        return super.getItemViewType(item, position);
    }

    public Integer getFootHeadItemViewType(@NonNull T item, int position) {
        int delegatesCount = headerDelegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            AdapterDelegate<T> delegate = headerDelegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                if (delegate.getItemType() != -1) {
                    return delegate.getItemType();
                }
                return headerDelegates.keyAt(i);
            }
        }
        int delegatesCount1 = footerDelegates.size();
        for (int i = 0; i < delegatesCount1; i++) {
            AdapterDelegate<T> delegate = footerDelegates.valueAt(i);
            if (delegate.isForViewType(item, position)) {
                if (delegate.getItemType() != -1) {
                    return delegate.getItemType();
                }
                return footerDelegates.keyAt(i);
            }
        }
        return null;
    }

    @Override
    public int getViewType(@NonNull AdapterDelegate<T> delegate) {
        if (delegate.getItemType() != -1) {
            return delegate.getItemType();
        }
        int result = super.getViewType(delegate);
        if (result == -1) {
            int index = headerDelegates.indexOfValue(delegate);
            if (index == -1) {
                index = footerDelegates.indexOfValue(delegate);
                if (index == -1) {
                    return -1;
                }
                return footerDelegates.keyAt(index);
            }
            return headerDelegates.keyAt(index);
        }
        return result;
    }

    @Nullable
    @Override
    public AdapterDelegate<T> getDelegateForViewType(int viewType) {
        if (footerDelegates.containsKey(viewType)) {
            return footerDelegates.get(viewType, fallbackDelegate);
        } else if (headerDelegates.containsKey(viewType)) {
            return headerDelegates.get(viewType, fallbackDelegate);
        }
        return super.getDelegateForViewType(viewType);
    }
}
