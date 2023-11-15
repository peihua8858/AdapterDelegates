package com.hannesdorfmann.adapterdelegates4.multitree;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.hannesdorfmann.adapterdelegates4.loadmore.LoadMoreDelegationAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 多层级嵌套树形适配器
 * 解决多层级嵌套，导致子节点的子节点无法正确折叠的问题
 * 由于remove方法使用{@link Collection#removeAll(Collection)},因此需要重写{@link T}的equals方法，
 * 支持默认部分展开折叠
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/11/7 15:26
 */
public abstract class BaseMultistageTreeAdapter<T> extends LoadMoreDelegationAdapter<T> {
    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    public int collapse(@IntRange(from = 0) int position) {
        return collapse(position, true, true);
    }

    /**
     * Expand an expandable item with animation.
     *
     * @param position position of the item, which includes the header layout count.
     * @return the number of items that have been added.
     */
    public int expand(@IntRange(from = 0) int position) {
        return expand(position, true, true);
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate) {
        return collapse(position, animate, true);
    }

    public int expandAll(int position, boolean animate, boolean notify) {
        List<T> items = this.items;
        if (items == null) {
            return 0;
        }
        position -= headCount();
        T endItem = null;
        if (position + 1 < this.items.size()) {
            endItem = getItem(position + 1);
        }
        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }
        int count = expand(position + headCount(), false, false);
        for (int i = position + 1; i < items.size(); i++) {
            T item = getItem(i);
            if (item != null && item == endItem) {
                break;
            }
            if (isExpandable(item)) {
                count += expand(i + headCount(), false, false);
            }

        }
        if (notify) {
            if (animate) {
                notifyItemRangeInserted(position + headCount() + 1, count);
            } else {
                notifyDataSetChanged();
            }
        }
        return count;
    }

    /**
     * 切换状态
     */
    public final void toggleAction(int position) {
        T item = getItem(position);
        if (isExpanded(item)) {
            collapse(position);
        } else {
            expand(position);
        }
    }

    public int collapse(int position, boolean animate, boolean notify) {
        position -= headCount();
        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        int subItemCount;
        int positionStart = position;
        if (enabledDefaultExpend(position)) {
            subItemCount = recursiveDefaultCollapse(position);
            expandable.setExpanded(false);
            IDefaultExpand defaultExpand = (IDefaultExpand) expandable;
            int spitCount = defaultExpand.spitCount();
            if (defaultExpand.getAllCount() > spitCount) {
                positionStart += spitCount;
            }
        } else {
            subItemCount = recursiveCollapse(position);
        }
        expandable.setExpanded(false);
        int parentPos = position + headCount();
        positionStart += headCount();
        if (notify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeRemoved(positionStart + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    public boolean isExpandable(T item) {
        return item instanceof IExpandable;
    }

    /**
     * 递归获取当前父节点下的所有子节点，包括子节点的子节点
     *
     * @param position
     * @author dingpeihua
     * @date 2019/11/7 15:35
     * @version 1.0
     */
    private int recursiveCollapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (item == null || !isExpandable(item)) {
            return 0;
        }
        IExpandable expandable = (IExpandable) item;
        if (!expandable.isExpanded() && !isDefaultExpanded(item)) {
            return 0;
        }
        List<T> children = recursiveChildrenCount(item);
        removeAll(children);
        return children.size();
    }

    private int recursiveDefaultCollapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (item == null) {
            return 0;
        }
        IDefaultExpand expandable = (IDefaultExpand) item;
        if (!expandable.isExpanded() && !isDefaultExpanded(item)) {
            return 0;
        }
        List<T> children = recursiveDefaultChildrenCount(item);
        removeAll(children);
        return children.size();
    }

    private IDefaultExpand getDefaultExpanded(T item) {
        if (item instanceof IDefaultExpand) {
            return (IDefaultExpand) item;
        }
        return null;
    }

    private boolean isDefaultExpanded(T item) {
        IDefaultExpand iDefaultExpand = getDefaultExpanded(item);
        return iDefaultExpand != null && iDefaultExpand.isDefExpanded();
    }

    private boolean enabledDefaultExpend(int position) {
        return enabledDefaultExpend(getItem(position));
    }

    private boolean enabledDefaultExpend(T item) {
        IDefaultExpand iDefaultExpand = getDefaultExpanded(item);
        return iDefaultExpand != null && iDefaultExpand.enabledDefaultExpend();
    }

    private IExpandable getExpandableItem(int position) {
        T item = getItem(position);
        return getExpandableItem(item);
    }

    private IExpandable getExpandableItem(T item) {
        if (isExpandable(item)) {
            return (IExpandable) item;
        } else {
            return null;
        }
    }

    private boolean isExpanded(T item) {
        return isExpanded(getExpandableItem(item));
    }

    private boolean isExpanded(IExpandable iExpandable) {
        return iExpandable != null && iExpandable.isExpanded();
    }

    public boolean hasSubItems(IExpandable item) {
        if (item == null) {
            return false;
        }
        List<T> list = item.getSubItems();
        return list != null && list.size() > 0;
    }

    /**
     * 刪除列表，如果子节点和父节点不是同一类型，则可重写次方法，按照索引位置移除
     *
     * @param collection
     * @author dingpeihua
     * @date 2019/11/8 10:18
     * @version 1.0
     */
    public void removeAll(Collection<T> collection) {
        if (items != null) {
            items.removeAll(collection);
        }
    }

    public void removeAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 递归获取当前节点及其所有展开的子节点
     *
     * @param item 当前节点
     * @author dingpeihua
     * @date 2019/11/7 16:34
     * @version 1.0
     */
    private List<T> recursiveChildrenCount(T item) {
        List<T> list = new ArrayList<>();
        recursiveChild(0, item, list);
        return list;
    }

    /**
     * 递归获取当前节点下所有展开的子节点
     *
     * @param item 当前节点
     * @author dingpeihua
     * @date 2019/11/7 16:34
     * @version 1.0
     */
    private void recursiveChild(int index, T item, List<T> list) {
        if (index > 0) list.add(item);
        ++index;
        if (item instanceof IExpandable) {
            IExpandable expandable = (IExpandable) item;
            List<T> subItems = ((IExpandable) item).getSubItems();
            if (!expandable.isExpanded() && !isDefaultExpanded(item)) {
                return;
            }
            if (subItems != null) {
                T itemTemp;
                for (int i = 0; i < subItems.size(); i++) {
                    itemTemp = subItems.get(i);
                    recursiveChild(index, itemTemp, list);
                }
            }
        }
    }

    private List<T> recursiveDefaultChildrenCount(T item) {
        List<T> list = new ArrayList<>();
        recursiveDefaultChild(0, item, list);
        return list;
    }

    private void recursiveDefaultChild(int index, T item, List<T> list) {
        if (index > 0) list.add(item);
        ++index;
        IDefaultExpand iDefaultExpand = getDefaultExpanded(item);
        if (iDefaultExpand.enabledDefaultExpend()) {
            List<T> subItems = iDefaultExpand.subList(iDefaultExpand.spitCount(), iDefaultExpand.getAllCount());
            if (!iDefaultExpand.isExpanded() && !iDefaultExpand.isDefExpanded()) {
                return;
            }
            if (subItems != null) {
                T itemTemp;
                for (int i = 0; i < subItems.size(); i++) {
                    itemTemp = subItems.get(i);
                    recursiveChild(index, itemTemp, list);
                }
            }
        }
    }

    public int expand(int position, boolean animate, boolean shouldNotify) {
        List<T> items = this.items;
        if (items == null) {
            return 0;
        }
        position -= headCount();
        T item = getItem(position);
        IExpandable expandable = getExpandableItem(item);
        if (expandable == null) {
            return 0;
        }
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(true);
            notifyItemChanged(position);
            return 0;
        }
        int subItemCount = 0;
        int positionStart = position;
        if (!isExpanded(expandable)) {
            if (enabledDefaultExpend(item)) {
                IDefaultExpand defaultExpand = (IDefaultExpand) expandable;
                int spitCount = defaultExpand.spitCount();
                spitCount = spitCount == -1 ? Integer.MAX_VALUE : spitCount;
                if (isDefaultExpanded(item)) {
                    List list = defaultExpand.subList(defaultExpand.spitCount(), defaultExpand.getAllCount());
                    positionStart += spitCount;
                    items.addAll(positionStart + 1, list);
                    subItemCount += recursiveExpand(positionStart + 1, list);
                    expandable.setExpanded(true);
                } else {
                    List list = defaultExpand.subList(0, spitCount);
                    items.addAll(positionStart + 1, list);
                    subItemCount += recursiveExpand(positionStart + 1, list);
                    expandable.setExpanded(list.size() == defaultExpand.getAllCount());
                    if (defaultExpand.getAllCount() > spitCount) {
                        defaultExpand.setDefExpanded(true);
                    }
                }
            } else {
                List list = expandable.getSubItems();
                items.addAll(positionStart + 1, list);
                subItemCount += recursiveExpand(positionStart + 1, list);
                expandable.setExpanded(true);
            }
        }
        int parentPos = position + headCount();
        positionStart += headCount();
        if (shouldNotify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeInserted(positionStart + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    private int recursiveExpand(int position, @NonNull List<T> list) {
        List<T> items = this.items;
        if (items == null) {
            return 0;
        }
        int count = list.size();
        int pos = position + list.size() - 1;
        for (int i = list.size() - 1; i >= 0; i--, pos--) {
            if (list.get(i) instanceof IExpandable) {
                IExpandable expandable = (IExpandable) list.get(i);
                if (hasSubItems(expandable)) {
                    if (expandable.isExpanded() && hasSubItems(expandable)) {
                        List subList = expandable.getSubItems();
                        items.addAll(pos + 1, subList);
                        int subItemCount = recursiveExpand(pos + 1, subList);
                        count += subItemCount;
                    }
                }
            }
        }
        return count;

    }

    /**
     * expand the item and all its subItems
     *
     * @param position position of the item, which includes the header layout count.
     * @param init     whether you are initializing the recyclerView or not.
     *                 if **true**, it won't notify recyclerView to redraw UI.
     * @return the number of items that have been added to the adapter.
     */
    public int expandAll(int position, boolean init) {
        return expandAll(position, true, !init);
    }

    public void expandAll() {
        List<T> items = this.items;
        if (items == null) {
            return;
        }
        expandAll(items.size());
    }

    /**
     * 展开指定数目
     *
     * @param count
     * @author dingpeihua
     * @date 2019/11/14 21:05
     * @version 1.0
     */
    public void expandAll(int count) {
        for (int i = count - 1 + headCount(); i >= headCount(); i--) {
            setEnabledDefaultExpand(i);
            expandAll(i, false, false);
        }
    }

    private void setEnabledDefaultExpand(int position) {
        T item = getItem(position);
        IDefaultExpand iDefaultExpand = getDefaultExpanded(item);
        if (iDefaultExpand != null && iDefaultExpand.hasSubItem()) {
            iDefaultExpand.setEnabledDefaultExpand(true);
        }
    }

    /**
     * Get the parent item position of the IExpandable item
     *
     * @return return the closest parent item position of the IExpandable.
     * if the IExpandable item's level is 0, return itself position.
     * if the item's level is negative which mean do not implement this, return a negative
     * if the item is not exist in the data list, return a negative.
     */
    public int getParentPosition(T item) {
        int position = getItemPosition(item);
        if (position == -1) {
            return -1;
        }
        // if the item is IExpandable, return a closest IExpandable item position whose level smaller than this.
        // if it is not, return the closest IExpandable item position whose level is not negative
        int level;
        if (item instanceof IExpandable) {
            level = ((IExpandable) item).getLevel();
        } else {
            level = Integer.MAX_VALUE;
        }
        if (level == 0) {
            return position;
        } else if (level == -1) {
            return -1;
        }
        for (int i = position; i >= 0; i--) {
            T temp = getItem(i);
            if (temp instanceof IExpandable) {
                int tempLevel = ((IExpandable<?>) temp).getLevel();
                if (tempLevel > 0 && tempLevel < level) {
                    return i;
                }
            }
        }
        return -1;
    }
}
