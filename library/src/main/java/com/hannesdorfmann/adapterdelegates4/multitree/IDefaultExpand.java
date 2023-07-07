package com.hannesdorfmann.adapterdelegates4.multitree;


import java.util.List;

/**
 * 控制默认展开指定数量操作
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/11/14 15:59
 */
public interface IDefaultExpand<T> extends IExpandable<T> {
    /**
     * 是否启用默认展开
     *
     * @return
     */
    boolean enabledDefaultExpend();

    void setEnabledDefaultExpand(boolean enabled);

    boolean isDefExpanded();

    void setDefExpanded(boolean expanded);

    /**
     * 设置默认展开数
     * 负数表示全部展开，0默认不展开，大于0者展开指定数量
     *
     * @author dingpeihua
     * @date 2020/4/3 17:05
     * @version 1.0
     */
    int spitCount();

    List<T> subList(int fromIndex, int toIndex);

    int getAllCount();

    /**
     * 用于判断是否含有子节点
     *
     * @return true 该节点下有子节点，否则没有子节点
     * @author dingpeihua
     * @date 2019/11/7 19:36
     * @version 1.0
     */
    boolean hasSubItem();
}
