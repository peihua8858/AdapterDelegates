package com.hannesdorfmann.adapterdelegates4;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A simplified {@link AdapterDelegate} when the underlying adapter's dataset is a  {@linkplain
 * List}. This class helps to reduce writing boilerplate code like casting list item and casting
 * ViewHolder.
 *
 * <p>
 * For instance if you have a list of animals (different kind of animals in classes like Cat, Dog
 * etc. assuming all have a common super class Animal) you want to display in your adapter and
 * you want to create a CatAdapterDelegate then this class would look like this:
 * {@code
 * class CatAdapterDelegate extends AbsListItemAdapterDelegate<Cat, Animal, CatViewHolder>{
 *
 * @param <I>  The type of the item that is managed by this AdapterDelegate. Must be a subtype of T
 * @param <T>  The generic type of the list, in other words: {@code List<T>}
 * @param <VH> The type of the ViewHolder
 * @author Hannes Dorfmann
 * @Override protected boolean isForViewType(Animal item, List<Animal> items, position){
 * return item instanceof Cat;
 * }
 * @Override public CatViewHolder onCreateViewHolder(ViewGroup parent){
 * return new CatViewHolder(inflater.inflate(R.layout.item_cat, parent, false));
 * }
 * @Override protected void onBindViewHolder(Cat item, CatViewHolder holder);
 * holder.setName(item.getName());
 * ...
 * }
 * }
 * <p>
 * }
 * </p>
 * @since 1.2
 */
public abstract class AbsItemAdapterDelegate<T, VH extends RecyclerView.ViewHolder> extends AbsAdapterDelegate<T> {

    @Override
    public void onBindViewHolder(@NonNull T item, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        onBindViewHolder(item, (VH) holder,position, payloads);
    }

    /**
     * Called to determine whether this AdapterDelegate is the responsible for the given item in the
     * list or not
     * element
     *
     * @param item     The item from the list at the given position
     * @param items    The items from adapters dataset
     * @param position The items position in the dataset (list)
     * @return true if this AdapterDelegate is responsible for that, otherwise false
     */
    protected abstract boolean isForViewType(@NonNull T item, @NonNull List<T> items, int position);

    /**
     * Creates the  {@link RecyclerView.ViewHolder} for the given data source item
     *
     * @param parent The ViewGroup parent of the given datasource
     * @return ViewHolder
     */
    @NonNull
    @Override
    protected abstract VH onCreateViewHolder(@NonNull ViewGroup parent);

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the dataset
     *
     * @param item     The data item
     * @param holder   The ViewHolder
     * @param position   The ViewHolder
     * @param payloads The payloads
     */
    protected abstract void onBindViewHolder(@NonNull T item, @NonNull VH holder, int position,
                                    @NonNull List<Object> payloads);
}
