package com.hannesdorfmann.adapterdelegates4.sample.loadmore;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * 首页 trends 的数据
 *
 * @author yeshunda
 * @version 1.1
 * @date 2016/9/23
 * @since 1.0
 */
public class AdapterBean<T> {
    @IntDef({
            RowType.FIRST_ROW,
            RowType.MIDDLE_ROW,
            RowType.LAST_ROW
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RowType {
        //0表示第一行 1表示中间行，2表示最后一行
        /**
         * 第一行
         */
        int FIRST_ROW = 1 << 1;
        /**
         * 中间行
         */
        int MIDDLE_ROW = 2 << 1;
        /**
         * 最后一行
         */
        int LAST_ROW = 3 << 1;
    }

    public int type;
    public T value;
    /**
     * {@link RowType#FIRST_ROW}表示第一行
     * {@link RowType#MIDDLE_ROW}表示中间行
     * {@link RowType#LAST_ROW}表示最后一行
     */
    @RowType
    public int rowType;

    public AdapterBean() {
    }

    public AdapterBean(int type) {
        this.type = type;
    }

    public AdapterBean(T value) {
        this.type = 0;
        this.value = value;
    }

    public AdapterBean(int type, T value) {
        this.type = type;
        this.value = value;
    }

    public AdapterBean(int type, @RowType int rowType, T value) {
        this.type = type;
        this.value = value;
        this.rowType = rowType;
    }

    public int getItemType() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterBean<?> that = (AdapterBean<?>) o;
        return type == that.type && rowType == that.rowType && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, rowType);
    }
}