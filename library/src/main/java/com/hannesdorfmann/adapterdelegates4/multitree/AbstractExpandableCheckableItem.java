package com.hannesdorfmann.adapterdelegates4.multitree;

import android.os.Parcel;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.List;

/**
 * 多级树形结构
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/11/7 19:32
 */
public abstract class AbstractExpandableCheckableItem<T extends Checkable> extends AbstractExpandableItem<T> implements Checkable {
    /**
     * 是否选择
     */
    private boolean checked;

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    @Override
    public int spitCount() {
        return 6;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AbstractExpandableCheckableItem<?> that = (AbstractExpandableCheckableItem<?>) o;

        return checked == that.checked;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (checked ? 1 : 0);
        return result;
    }

    @Override
    public void toggle() {

    }

    /**
     * 获取选中的item
     *
     * @author dingpeihua
     * @date 2019/11/14 10:47
     * @version 1.0
     */
    @Override
    public List<T> getCheckedSubItems() {
        List<T> checkedItem = new ArrayList<>();
        if (hasSubItem()) {
            List<T> subItem = getSubItems();
            if (subItem != null && subItem.size() > 0) {
                for (T child : subItem) {
                    if (child.isChecked()) {
                        checkedItem.add(child);
                    }
                }
            }
        }
        return checkedItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.checked ? (byte) 1 : (byte) 0);
    }

    public AbstractExpandableCheckableItem() {
    }

    protected AbstractExpandableCheckableItem(Parcel in) {
        super(in);
        this.checked = in.readByte() != 0;
    }
}
