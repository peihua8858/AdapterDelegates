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

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

/**
 * This class is the element that ties {@link RecyclerView.Adapter} together with {@link
 * AdapterDelegate}.
 * <p>
 * So you have to add / register your {@link AdapterDelegate}s to this manager by calling {@link
 * #addDelegate(AdapterDelegate)}
 * </p>
 *
 * <p>
 * Next you have to add this AdapterDelegatesManager to the {@link RecyclerView.Adapter} by calling
 * corresponding methods:
 * <ul>
 * <li> {@link #getItemViewType(Object, int)}: Must be called from {@link
 * RecyclerView.Adapter#getItemViewType(int)}</li>
 * <li> {@link #onCreateViewHolder(ViewGroup, int)}: Must be called from {@link
 * RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)}</li>
 * <li> {@link #onBindViewHolder(Object, int, RecyclerView.ViewHolder)}: Must be called from {@link
 * RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}</li>
 * </ul>
 * <p>
 * You can also set a fallback {@link AdapterDelegate} by using {@link
 * #setFallbackDelegate(AdapterDelegate)} that will be used if no {@link AdapterDelegate} is
 * responsible to handle a certain view type. If no fallback is specified, an Exception will be
 * thrown if no {@link AdapterDelegate} is responsible to handle a certain view type
 * </p>
 *
 * @param <T> The type of the datasource of the adapter
 * @author Hannes Dorfmann
 */
public class AdapterDelegatesManager<T> {

    /**
     * ViewType for the fallback delegate
     */
    public static final int FALLBACK_DELEGATE_VIEW_TYPE = Integer.MAX_VALUE - 1;

    /**
     * Used internally for {@link #onBindViewHolder(Object, int, RecyclerView.ViewHolder)} as empty
     * payload parameter
     */
    private static final List<Object> PAYLOADS_EMPTY_LIST = Collections.emptyList();

    /**
     * Map for ViewType to AdapterDelegate
     */
    protected SparseArrayCompat<AdapterDelegate<T>> delegates = new SparseArrayCompat();
    protected AdapterDelegate<T> fallbackDelegate;

    /**
     * Creates a AdapterDelegatesManager without any delegates.
     */
    public AdapterDelegatesManager() {
    }

    /**
     * Creates a AdapterDelegatesManager which already has the gived delegates added to it.
     */
    public AdapterDelegatesManager(@NonNull AdapterDelegate<T>... delegates) {
        for (int i = 0; i < delegates.length; i++) {
            addDelegate(delegates[i]);
        }
    }

    /**
     * Adds an {@link AdapterDelegate}.
     * <b>This method automatically assign internally the view type integer by using the next
     * unused</b>
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, AdapterDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(int, AdapterDelegate)
     * @see #addDelegate(int, boolean, AdapterDelegate)
     */
    public AdapterDelegatesManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
        int viewType = delegate.getItemType();
        if (viewType == -1) {
            // algorithm could be improved since there could be holes,
            // but it's very unlikely that we reach Integer.MAX_VALUE and run out of unused indexes
            viewType = delegates.size();
            while (delegates.get(viewType) != null) {
                viewType++;
                if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
                    throw new IllegalArgumentException(
                            "Oops, we are very close to Integer.MAX_VALUE. It seems that there are no more free and unused view type integers left to add another AdapterDelegate.");
                }
            }
        }
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Adds an {@link AdapterDelegate} with the specified view type.
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, AdapterDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param viewType the view type integer if you want to assign manually the view type. Otherwise
     *                 use {@link #addDelegate(AdapterDelegate)} where a viewtype will be assigned automatically.
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(AdapterDelegate)
     * @see #addDelegate(int, boolean, AdapterDelegate)
     */
    public AdapterDelegatesManager<T> addDelegate(int viewType,
                                                  @NonNull AdapterDelegate<T> delegate) {
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Adds an {@link AdapterDelegate}.
     *
     * @param viewType               The viewType id
     * @param allowReplacingDelegate if true, you allow to replacing the given delegate any previous
     *                               delegate for the same view type. if false, you disallow and a {@link IllegalArgumentException}
     *                               will be thrown if you try to replace an already registered {@link AdapterDelegate} for the
     *                               same view type.
     * @param delegate               The delegate to add
     * @throws IllegalArgumentException if <b>allowReplacingDelegate</b>  is false and an {@link
     *                                  AdapterDelegate} is already added (registered)
     *                                  with the same ViewType.
     * @throws IllegalArgumentException if viewType is {@link #FALLBACK_DELEGATE_VIEW_TYPE} which is
     *                                  reserved
     * @see #addDelegate(AdapterDelegate)
     * @see #addDelegate(int, AdapterDelegate)
     * @see #setFallbackDelegate(AdapterDelegate)
     */
    public AdapterDelegatesManager<T> addDelegate(int viewType, boolean allowReplacingDelegate,
                                                  @NonNull AdapterDelegate<T> delegate) {

        if (delegate == null) {
            throw new NullPointerException("AdapterDelegate is null!");
        }

        if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
            throw new IllegalArgumentException("The view type = "
                    + FALLBACK_DELEGATE_VIEW_TYPE
                    + " is reserved for fallback adapter delegate (see setFallbackDelegate() ). Please use another view type.");
        }

        if (!allowReplacingDelegate && delegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An AdapterDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered AdapterDelegate is "
                            + delegates.get(viewType));
        }

        delegates.put(viewType, delegate);

        return this;
    }

    /**
     * Removes a previously registered delegate if and only if the passed delegate is registered
     * (checks the reference of the object). This will not remove any other delegate for the same
     * viewType (if there is any).
     *
     * @param delegate The delegate to remove
     * @return self
     */
    public AdapterDelegatesManager<T> removeDelegate(@NonNull AdapterDelegate<T> delegate) {

        if (delegate == null) {
            throw new NullPointerException("AdapterDelegate is null");
        }

        int indexToRemove = delegates.indexOfValue(delegate);

        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }
        return this;
    }

    /**
     * Removes the adapterDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public AdapterDelegatesManager<T> removeDelegate(int viewType) {
        delegates.remove(viewType);
        return this;
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#getItemViewType(int)}. Internally it scans all
     * the registered {@link AdapterDelegate} and picks the right one to return the ViewType integer.
     *
     * @param items    Adapter's data source
     * @param position the position in adapters data source
     * @return the ViewType (integer). Returns {@link #FALLBACK_DELEGATE_VIEW_TYPE} in case that the
     * fallback adapter delegate should be used
     * @throws NullPointerException if no {@link AdapterDelegate} has been found that is
     *                              responsible for the given data element in data set (No {@link AdapterDelegate} for the given
     *                              ViewType)
     * @throws NullPointerException if items is null
     */
    public int getItemViewType(@NonNull T items, int position) {

        if (items == null) {
            throw new NullPointerException("Items datasource is null!");
        }

        int delegatesCount = delegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            AdapterDelegate<T> delegate = delegates.valueAt(i);
            if (delegate.isForViewType(items, position)) {
                if (delegate.getItemType() != -1) {
                    return delegate.getItemType();
                }
                return delegates.keyAt(i);
            }
        }

        if (fallbackDelegate != null) {
            return FALLBACK_DELEGATE_VIEW_TYPE;
        }

        final String errorMessage;

        if (items instanceof List<?>) {
            String itemString = ((List<?>) items).get(position).toString();
            errorMessage = "No AdapterDelegate added that matches item=" + itemString + " at position=" + position + " in data source";
        } else {
            errorMessage = "No AdapterDelegate added for item at position=" + position + ". items=" + items;
        }

        throw new NullPointerException(errorMessage);
    }

    /**
     * This method must be called in {@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)}
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return The new created ViewHolder
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterDelegate<T> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }

        RecyclerView.ViewHolder vh = delegate.onCreateViewHolder(parent);
        if (vh == null) {
            throw new NullPointerException("ViewHolder returned from AdapterDelegate "
                    + delegate
                    + " for ViewType ="
                    + viewType
                    + " is null!");
        }
        return vh;
    }

    /**
     * Must be called from{@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int,
     * List)}
     *
     * @param items    Adapter's data source
     * @param position the position in data source
     * @param holder   the ViewHolder to bind
     * @param payloads A non-null list of merged payloads. Can be empty list if requires full update.
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    public void onBindViewHolder(@NonNull T items, int position,
                                 @NonNull RecyclerView.ViewHolder holder, List payloads) {

        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onBindViewHolder(items, position, holder,
                payloads != null ? payloads : PAYLOADS_EMPTY_LIST);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int,
     * List)}
     *
     * @param items    Adapter's data source
     * @param position the position in data source
     * @param holder   the ViewHolder to bind
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    public void onBindViewHolder(@NonNull T items, int position,
                                 @NonNull RecyclerView.ViewHolder holder) {
        onBindViewHolder(items, position, holder, PAYLOADS_EMPTY_LIST);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)}
     *
     * @param holder The ViewHolder for the view being recycled
     */
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewRecycled(holder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)}
     *
     * @param holder The ViewHolder containing the View that could not be recycled due to its
     *               transient state.
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
     * the View and recycle it regardless. If this method returns <code>false</code>,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        return delegate.onFailedToRecycleView(holder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
     *
     * @param holder Holder of the view being attached
     */
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewAttachedToWindow(holder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
     *
     * @param holder Holder of the view being attached
     */
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        AdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewDetachedFromWindow(holder);
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
    public AdapterDelegatesManager<T> setFallbackDelegate(
            @Nullable AdapterDelegate<T> fallbackDelegate) {
        this.fallbackDelegate = fallbackDelegate;
        return this;
    }

    /**
     * Get the view type integer for the given {@link AdapterDelegate}
     *
     * @param delegate The delegate we want to know the view type for
     * @return -1 if passed delegate is unknown, otherwise the view type integer
     */
    public int getViewType(@NonNull AdapterDelegate<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("Delegate is null");
        }
        if (delegate.getItemType() != -1) {
            return delegate.getItemType();
        }
        int index = delegates.indexOfValue(delegate);
        if (index == -1) {
            return -1;
        }
        return delegates.keyAt(index);
    }

    /**
     * Get the {@link AdapterDelegate} associated with the given view type integer
     *
     * @param viewType The view type integer we want to retrieve the associated
     *                 delegate for.
     * @return The {@link AdapterDelegate} associated with the view type param if it exists,
     * the fallback delegate otherwise if it is set or returns <code>null</code> if no delegate is
     * associated to this viewType (and no fallback has been set).
     */
    @Nullable
    public AdapterDelegate<T> getDelegateForViewType(int viewType) {
        return delegates.get(viewType, fallbackDelegate);
    }

    /**
     * Get the fallback delegate
     *
     * @return The fallback delegate or <code>null</code> if no fallback delegate has been set
     * @see #setFallbackDelegate(AdapterDelegate)
     */
    @Nullable
    public AdapterDelegate<T> getFallbackDelegate() {
        return fallbackDelegate;
    }
}
