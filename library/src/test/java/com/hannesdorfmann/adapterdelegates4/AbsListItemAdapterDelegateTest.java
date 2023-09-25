package com.hannesdorfmann.adapterdelegates4;

import android.view.View;
import android.view.ViewGroup;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Hannes Dorfmann
 */
public class AbsListItemAdapterDelegateTest {

    @Test
    public void invokeMethods() {

        List<Animal> items = new ArrayList<>();
        items.add(new Cat());

        CatAbsListItemAdapterDelegate delegate = new CatAbsListItemAdapterDelegate();

        delegate.isForViewType(items.get(0), 0);
        Assert.assertTrue(delegate.isForViewTypeCalled);

        ViewGroup parent = Mockito.mock(ViewGroup.class);
        CatViewHolder vh = (CatViewHolder) delegate.createViewHolder(parent);
        Assert.assertTrue(delegate.onCreateViewHolderCalled);

        delegate.bindViewHolder(items, 0, vh, new ArrayList<Object>());
        Assert.assertTrue(delegate.onBindViewHolderCalled);


    }

    interface Animal {
    }

    class Cat implements Animal {
    }

    class CatViewHolder extends RecyclerView.ViewHolder {
        public CatViewHolder(View itemView) {
            super(itemView);
        }
    }

    class CatAbsListItemAdapterDelegate
            extends AbsListItemAdapterDelegate<Cat, Animal, CatViewHolder> {
        public boolean isForViewTypeCalled = false;
        public boolean onCreateViewHolderCalled = false;
        public boolean onBindViewHolderCalled = false;
        public boolean onViewDetachedFromWindow = false;


        @Override
        public boolean isForViewType(@NonNull Animal item, int position) {
            isForViewTypeCalled = true;
            return false;
        }

        @NonNull
        @Override
        public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
            onCreateViewHolderCalled = true;
            return new CatViewHolder(Mockito.mock(View.class));
        }

        @Override
        protected void onBindViewHolder(@NonNull Cat item, @NonNull CatViewHolder holder, @NonNull List payloads) {
            onBindViewHolderCalled = true;
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            onViewDetachedFromWindow = true;
        }
    }
}
