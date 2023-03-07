package com.hannesdorfmann.adapterdelegates4.sample.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * fragment 显示隐藏处理
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/1/31 17:08
 */
public final class FragmentHelper {
    /**
     * 保存的fragment键名
     */
    static final String SAVE_INSTANCE_FRAGMENT = "HELPER_FRAGMENT_";
    /**
     * 保存的fragment个数
     */
    static final String SAVE_INSTANCE_COUNT = "HELPER_SAVE_COUNT";
    /**
     * 保存通过addFragment方法添加的Fragment
     */
    private List<Fragment> fragments;
    /**
     * 保存的Activity状态
     */
    private Bundle savedInstanceState;
    /**
     * 当前显示的fragment在列表中的索引
     */
    private int mCurShowFragmentPosition = -1;
    /**
     * 当前显示的fragment
     */
    private Fragment curShowFragment;
    /**
     * Fragment管理器
     */
    private FragmentManager fragmentManager;
    OnFragmentHelper onFragmentHelper;

    public FragmentHelper(FragmentManager fragmentManager, Bundle bundle, OnFragmentHelper onFragmentHelper) {
        this.fragmentManager = fragmentManager;
        this.savedInstanceState = bundle;
        this.onFragmentHelper = onFragmentHelper;
        fragments = new ArrayList<>();
        if (bundle != null && onFragmentHelper.isSaveInstance()) {
            //恢复fragment
            int size = bundle.getInt(SAVE_INSTANCE_COUNT);
            for (int i = 0; i < size; i++) {
                final Fragment fragment = fragmentManager.getFragment(savedInstanceState, makeFragmentKey(i));
                if (fragment != null) {
                    fragments.add(fragment);
                }
            }
        }
    }

    public String makeFragmentKey(int index) {
        if (onFragmentHelper != null) {
            return onFragmentHelper.makeFragmentKey(index);
        }
        return SAVE_INSTANCE_FRAGMENT + index;
    }

    public void onSaveInstanceState(Bundle outState) {
        this.savedInstanceState = outState;
        if (onFragmentHelper.isSaveInstance()) {
            int index = 0;
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isAdded()) {
                    fragmentManager.putFragment(outState, makeFragmentKey(index), fragment);
                    index++;
                }
            }
            outState.putInt(SAVE_INSTANCE_COUNT, fragments.size());
        }
    }

    public void onDestroy() {
        if (fragments != null) {
            fragments.clear();
        }
        mCurShowFragmentPosition = -1;
        curShowFragment = null;
        onFragmentHelper = null;
        fragmentManager = null;
    }

    public Fragment getFragment(int index) {
        if (index >= 0 && fragments != null && index < fragments.size()) {
            return fragments.get(index);
        }
        return null;
    }

    public Fragment remove(Fragment fragment) {
        if (fragmentManager == null) {
            return fragment;
        }
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commitNowAllowingStateLoss();
        for (int i = 0; i < fragments.size(); i++) {
            final Fragment fragment1 = fragments.get(i);
            final String tag = fragment1.getTag();
            if (tag != null && tag.equals(fragment.getTag())) {
                fragments.remove(i);
                break;
            }
        }
        return fragment;
    }

    public final void removeAll() {
        if (fragmentManager == null) {
            return;
        }
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitNowAllowingStateLoss();
        fragments.clear();
    }

    /**
     * 添加多个fragment到{@link #fragments}列表中
     * 并显示列表中第一个位置的fragment
     *
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    public final void showFragments(Fragment... fragments) {
        addFragments(0, fragments);
    }

    /**
     * 添加多个fragment到{@link #fragments}列表中
     * 并显示指定索引位置的fragment
     *
     * @param showFragmentIndex
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    public final void addFragments(int showFragmentIndex, Fragment... fragments) {
        addFragments(fragments);
        showFragment(showFragmentIndex);
    }

    /**
     * 添加一个个fragment到{@link #fragments}列表中
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    public final void addFragment(Fragment fragment, boolean isRepeatable) {
        //为解决activity重启时fragment重叠问题
        if (fragment == null) {
            return;
        }
        int index = isRepeatable ? -1 : checkManagerExistsByIndex(fragment);
        if (index >= 0) {
            fragments.set(index, fragment);
        } else {
            fragments.add(fragment);
        }
    }

    /**
     * 添加一个个fragment到{@link #fragments}列表中
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    public final void addFragment(Fragment fragment) {
        addFragment(fragment, false);
    }

    /**
     * 添加多个fragment到{@link #fragments}列表中
     *
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    public final void addFragments(Fragment... fragments) {
        //为解决activity重启时fragment重叠问题
        if (fragments == null || fragments.length == 0) {
            return;
        }
        Fragment fragment;
        for (int i = 0; i < fragments.length; i++) {
            fragment = fragments[i];
            final String f1ClassNam = fragment.getClass().getName();
            fragment = checkManagerExists(fragment);
            if (fragment != null && fragment.getClass().getName().equals(f1ClassNam)) {
                fragments[i] = fragment;
            }
        }
        this.fragments = new ArrayList<>(Arrays.asList(fragments));
    }

    /**
     * 添加fragment到{@link #fragments}列表中
     * 如果{@link #fragments}中存在当前fragment实例，则不添加，
     * 否则将当前fragment添加到{@link #fragments}列表
     *
     * @author dingpeihua
     * @date 2019/1/31 17:25
     * @version 1.0
     */
    public final void addFragments(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
        fragment = checkManagerExists(fragment);
        fragment = checkFragmentsExists(fragment);
        if (fragment != null) {
            fragments.add(fragment);
        }
    }

    /**
     * 检查fragmentManger中是否存在当前类型索引。
     * 如果存在，则返回fragmentManager列表中的索引，否则返回-1
     *
     * @param fragment 当前fragment
     * @return 如果存在，则返回fragmentManager列表中的索引，否则返回-1
     * @author dingpeihua
     * @date 2019/1/31 17:22
     * @version 1.0
     */
    int checkManagerExistsByIndex(Fragment fragment) {
        if (fragmentManager == null) {
            return -1;
        }
        List<Fragment> fList = fragments;
        if (fList == null || fList.size() == 0) {
            fList = new ArrayList<>(fragmentManager.getFragments());
            this.fragments = new ArrayList<>();
            for (Fragment fragment1 : fList) {
                    fragments.add(fragment1);
            }
        }
        if (fragments.size() > 0) {
            String fClassName = fragment.getClass().getName();
            for (int i = 0; i < fragments.size(); i++) {
                final Fragment fragment1 = fragments.get(i);
                if (fClassName.equals(fragment1.getClass().getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 检查fragmentManger中是否存在当前fragment实例，
     * 如果存在，则返回fragmentManager列表中的fragment，否则返回当前fragment
     *
     * @param fragment 当前fragment
     * @author dingpeihua
     * @date 2019/1/31 17:22
     * @version 1.0
     */
    Fragment checkManagerExists(Fragment fragment) {
        if (fragmentManager == null) {
            return fragment;
        }
        List<Fragment> fList = fragments;
        if (fList == null || fList.size() == 0) {
            fList = new ArrayList<>(fragmentManager.getFragments());
            this.fragments = new ArrayList<>();
            for (Fragment fragment1 : fList) {
                    fragments.add(fragment1);
            }
        }
        if (fragments.size() > 0) {
            String fClassName = fragment.getClass().getName();
            //为解决activity重启时fragment重叠问题
            for (Fragment fragment1 : fragments) {
                    final String f1ClassName = fragment1.getClass().getName();
                    if (fClassName.equals(f1ClassName)) {
                        fragment = fragment1;
                        break;
                }
            }
        }
        return fragment;
    }

    /**
     * 检查fragment是否存在列表{@link #fragments}中，
     * 如果存在则返回null，不存在则返回当前fragment
     *
     * @param fragment 当前fragment
     * @author dingpeihua
     * @date 2019/1/31 17:23
     * @version 1.0
     */
    Fragment checkFragmentsExists(Fragment fragment) {
        if (fragments != null && fragments.size() > 0) {
            String fClassName = fragment.getClass().getName();
            int fSize = fragments.size();
            for (int i = 0; i < fSize; i++) {
                final Fragment fragment2 = fragments.get(i);
                final String f2ClassName = fragment2.getClass().getName();
                if (f2ClassName.equals(fClassName)) {
                    return null;
                }
            }
        }
        return fragment;
    }

    /**
     * 显示当前索引位置的fragment
     *
     * @author dingpeihua
     * @date 2016/11/17 15:33
     * @version 1.0
     */
    public final void showFragment(int showFragmentIndex) {
        //如果当前fragment已经显示则不再处理
        showFragment(showFragmentIndex, 0, 0);
    }

    /**
     * 显示当前索引位置的fragment,带进出动画
     *
     * @author dingpeihua
     * @date 2016/11/17 15:33
     * @version 1.0
     */
    @SuppressLint("ResourceType")
    public final void showFragment(int showFragmentIndex, int inAnimation, int outAnimation) {
        if (fragmentManager == null) {
            return;
        }
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (showFragmentIndex == mCurShowFragmentPosition) {
            //如果当前fragment已经显示则不再处理
            return;
        }
        if (inAnimation != 0 || outAnimation != 0) {
            fragmentTransaction.setCustomAnimations(inAnimation, outAnimation);
        }
        if (fragments != null) {
            for (int i = 0; i < fragments.size(); i++) {
                final Fragment fragment = fragments.get(i);
                if (fragment != null) {
                    addFragment(fragmentTransaction, fragment, showFragmentIndex == i, i);
                }
            }
            fragmentTransaction.commitNowAllowingStateLoss();
        }
    }

    /**
     * 显示指定的fragment
     *
     * @param f
     * @author dingpeihua
     * @date 2019/1/31 17:54
     * @version 1.0
     */
    public final <T extends Fragment> T showFragment(T f) {
        if (f == null || fragmentManager == null) {
            return null;
        }
        addFragments(f);
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final String className = f.getClass().getName();
        if (fragments != null) {
            for (int i = 0; i < fragments.size(); i++) {
                final Fragment fragment = fragments.get(i);
                if (fragment != null) {
                    final boolean isShow = className.equals(fragment.getClass().getName());
                    addFragment(fragmentTransaction, fragment, isShow, i);
                }
            }
            fragmentTransaction.commitNowAllowingStateLoss();
        }
        return cast(f, curShowFragment);
    }

    public final <T> T cast(T fragment, Object obj) {
        if (fragment != null) {
            Class<T> clazz = (Class<T>) fragment.getClass();
            try {
                return clazz.cast(obj);
            } catch (Throwable e) {
                Log.e("cast", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 添加fragment到fragmentManager
     *
     * @param fragmentTransaction
     * @param fragment
     * @param isShow
     * @param position
     * @author dingpeihua
     * @date 2019/1/31 17:44
     * @version 1.0
     */
    void addFragment(FragmentTransaction fragmentTransaction, Fragment fragment, boolean isShow, int position) {
        if (fragment == null || fragmentManager == null) {
            return;
        }
        if (!fragment.isAdded()) {
            // add方法默认会显示添加进去的Fragment
            int containerViewId = onFragmentHelper.getContainerViewId();
            String fragmentTag = onFragmentHelper.getFragmentTag();
            if (TextUtils.isEmpty(fragmentTag)) {
                fragmentTag = makeFragmentName(containerViewId, position);
            }
            Fragment f = fragmentManager.findFragmentByTag(fragmentTag);
            if (f == null) {
                if (containerViewId != 0) {
                    fragmentTransaction.add(containerViewId, fragment, fragmentTag);
                } else {
                    fragmentTransaction.add(fragment, fragmentTag);
                }
            }
        }
        if (isShow) {
                curShowFragment = fragment;
            mCurShowFragmentPosition = position;
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.hide(fragment);
        }
//        fragmentTransaction.commitNowAllowingStateLoss();
    }

    private static String makeFragmentName(int viewId, long position) {
        return "app:switcher:fragment_" + viewId + ":" + position;
    }

    public Fragment findFragment(String fragmentKey) {
        if (savedInstanceState != null && fragmentManager != null) {
            return fragmentManager.getFragment(savedInstanceState, fragmentKey);
        }
        return null;
    }

    /**
     * 获取fragments 列表
     *
     * @author dingpeihua
     * @date 2019/1/31 17:17
     * @version 1.0
     */
    public List<Fragment> getFragments() {
        return fragments;
    }

    /**
     * 获取当前显示的fragment
     *
     * @author dingpeihua
     * @date 2019/1/31 17:51
     * @version 1.0
     */
    public Fragment getShowFragment() {
        return curShowFragment;
    }

    /**
     * 获取当前显示的fragment的索引
     *
     * @author dingpeihua
     * @date 2019/1/31 17:51
     * @version 1.0
     */
    public int getShowFragmentIndex() {
        return mCurShowFragmentPosition;
    }

    /**
     * 通知fragment按下返回键
     *
     * @author dingpeihua
     * @date 2019/1/31 17:17
     * @version 1.0
     */
    public final boolean onBackPressed() {
        return false;
    }

    public interface OnFragmentHelper {
        /**
         * fragment 容器视图id
         *
         * @return 容器视图id
         * @author dingpeihua
         * @date 2019/1/31 16:43
         * @version 1.0
         */
        @IdRes
        int getContainerViewId();

        /**
         * {@link FragmentManager#findFragmentByTag(String)}
         *
         * @return fragment 标签
         * @author dingpeihua
         * @date 2019/1/31 16:44
         * @version 1.0
         */
        String getFragmentTag();

        /**
         * Fragment 状态保存key
         *
         * @param index
         * @return
         * @author dingpeihua
         * @date 2019/11/25 14:12
         * @version 1.0
         */
        default String makeFragmentKey(int index) {
            return SAVE_INSTANCE_FRAGMENT + index;
        }

        /**
         * 是否保存状态
         *
         * @return true保存状态，否则不保存状态
         */
        default boolean isSaveInstance() {
            return false;
        }
    }
}
