package com.hannesdorfmann.adapterdelegates4.sample.base

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.fz.common.activity.*
import com.fz.common.utils.isMainThread
import com.fz.common.view.utils.dip2px
import com.google.android.material.appbar.AppBarLayout
import com.hannesdorfmann.adapterdelegates4.sample.R

/**
 * [AppCompatActivity]基础封装
 * @author dingpeihua
 * @date 2021/1/14 10:40
 * @version 1.0
 */
abstract class BaseActivity @JvmOverloads constructor(@LayoutRes contentLayoutId: Int = 0) :
    AppCompatActivity(contentLayoutId),
    FragmentHelper.OnFragmentHelper{

    /**
     * fragment 显示处理
     */
    private var fragmentHelper: FragmentHelper? = null

    /**
     * 弱引用Handler
     */
    protected val mHandler = Handler()
    val windowInsetsControllerCompat: WindowInsetsControllerCompat by lazy {
        val window = window ?: throw NullPointerException("error")
        WindowInsetsControllerCompat(window, window.decorView)
    }
    protected val context: Context by lazy {
        if (isCreated) {
            return@lazy this
        }
        throw IllegalArgumentException("Please get it after onCreate()")
    }
    var appBarLayout: AppBarLayout? = null

    /**
     * Material Design风格Toolbar控件
     */
    private var mToolbar: Toolbar? = null

    /**
     * 自定义标题
     */
    var tvToolbarTitle: TextView? = null

    /**
     * 获取content fragment
     *
     * @return
     */
    open fun getContentFragment(): Fragment? {
        return null
    }

    var isCreated = false
        private set

    /**
     * 记录是否已经调用过onStart方法
     */
    private var isStarted = false

    /**
     * 是否注册eventbus 默认注册,保证切换语言有效
     *
     * @return
     */
    open fun isRegisterEventBus(): Boolean {
        return true
    }

    //
//    /**
//     * 是否启动自动密度适配
//     * 如果开启则布局文件直接使用4dp值，不需要引入@dimen/_4sdp
//     *
//     * @author dingpeihua
//     * @date 2018/11/20 10:43
//     * @version 1.0
//     */
//    protected open fun enableAutoDensity(): Boolean {
//        return true
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCreated = true
        isStarted = true
//        if (enableAutoDensity()) {
//            ScreenMatchUtil.initialize(this)
//            ScreenMatchUtil.matchWithWidth(this, 360f)
//        } else { //不启用，则将所有参数还原
//            ScreenMatchUtil.cancelMatch(this)
//        }
        fragmentHelper = FragmentHelper(supportFragmentManager, savedInstanceState, this)
        //注册eventbus
        setSupportActionBar()
    }


    fun setSupportActionBar() {
        /**
         * 阴影去除
         */
        if (appBarLayout != null) {
            val stateListAnimator = StateListAnimator()
            stateListAnimator.addState(
                IntArray(0),
                ObjectAnimator.ofFloat(
                    appBarLayout,
                    "elevation", dip2px(0.5f).toFloat()
                )
            )
            appBarLayout!!.stateListAnimator = stateListAnimator
        }
    }

    open fun onToolbarNavigationClick() {
        if (onFragmentBackPressed()) {
            return
        }
        finish()
    }

    open fun onFragmentBackPressed(): Boolean {
        return fragmentHelper != null && fragmentHelper!!.onBackPressed() || doBackPressed()
    }

    override fun onResume() {
        super.onResume()
//        if (enableAutoDensity()) {
//            ScreenMatchUtil.matchWithWidth(this, 360f)
//        }
    }


    override fun onStart() {
        super.onStart()
        if (isStarted && isAutoShowFragment()) {
            isStarted = false
            val curShowFragment = getContentFragment()
            //替换添加
            if (curShowFragment != null && fragmentHelper != null) {
                fragmentHelper!!.showFragment(curShowFragment)
            }
        }
    }

    override fun onDestroy() {
        fragmentHelper?.onDestroy()
        super.onDestroy()
        isCreated = false
        mHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 设置重试按钮事件
     *
     * @author dingpeihua
     * @date 2018/12/10 11:13
     * @version 1.0
     */
    fun setTryClickListener(view: View?, callback: () -> Unit) {
//        setTryClickListener(view, R.id.btn_retry, callback)
    }

    fun setTryClickListener(view: View?, @IdRes viewId: Int, callback: () -> Unit) {
        if (view != null) {
            val btnRetry = view.findViewById<View>(viewId)
            btnRetry?.setOnClickListener { callback() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragmentHelper?.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (onFragmentBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun getReferrer(): Uri? {
        return if (Build.VERSION.SDK_INT >= 22) {
            super.getReferrer()
        } else getReferrerCompatible()
    }

    fun getDimensionPixelSize(@DimenRes id: Int): Int {
        return resources.getDimensionPixelSize(id)
    }

    fun getDimension(@DimenRes id: Int): Float {
        return resources.getDimension(id)
    }

    /**
     * Returns the referrer on devices running SDK versions lower than 22.
     */
    private fun getReferrerCompatible(): Uri? {
        val intent = this.intent
        val referrerUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_REFERRER)
        if (referrerUri != null) {
            return referrerUri
        }
        val referrer = intent.getStringExtra("android.intent.extra.REFERRER_NAME")
        return if (referrer != null) {
            // Try parsing the referrer URL; if it's invalid, return null
            try {
                Uri.parse(referrer)
            } catch (e: java.lang.Exception) {
                null
            }
        } else null
    }

    /**
     * 获取颜色值，默认颜色是透明色[Color.TRANSPARENT]
     * @param res 图片id
     * @author dingpeihua
     * @date 2021/2/24 10:59
     * @version 1.0
     */
    @ColorInt
    fun getColorCompat(@ColorRes res: Int): Int {
        return getColorCompat(res, Color.TRANSPARENT)
    }

    /**
     * 获取颜色值
     * @param defaultColor 默认颜色
     * @param res 图片id
     * @author dingpeihua
     * @date 2021/2/24 10:59
     * @version 1.0
     */
    @ColorInt
    fun getColorCompat(@ColorRes res: Int, @ColorInt defaultColor: Int): Int {
        if (res == 0) {
            return defaultColor
        }
        return ContextCompat.getColor(this, res)
    }

    /**
     * 获取图片资源
     * @param res 图片id
     * @author dingpeihua
     * @date 2021/2/24 10:59
     * @version 1.0
     */
    fun getDrawableCompat(@DrawableRes res: Int): Drawable? {
        if (res == 0) {
            return null
        }
        return ContextCompat.getDrawable(this, res)
    }

    /**
     * 发送消息,用于各个组件之间通信
     *
     * @param event 消息事件对象
     */
    fun <EVENT> sendEventMessage(event: EVENT) {
        // 发布事件
    }

    /**
     * 自动显示当前添加的fragment
     *
     * @author dingpeihua
     * @date 2019/11/26 10:15
     * @version 1.0
     */
    open fun isAutoShowFragment(): Boolean {
        return true
    }

    open fun <T> getActionProvider(item: MenuItem?, clazz: Class<T>): T? {
        try {
            if (item != null) {
                return clazz.cast(MenuItemCompat.getActionProvider(item))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 返回退出处理
     *
     * @return true 子类自己处理事件，父类放弃此次事件处理，false则交给父类处理
     * @author dingpeihua
     * @date 2018/11/7 14:59
     * @version 1.0
     */
    protected open fun doBackPressed(): Boolean {
        return false
    }

    /**
     * 获取Bundle对象
     *
     * @author dingpeihua
     * @date 2016/6/29 11:00
     * @version 1.0
     */
    protected val bundle: Bundle
        get() {
            return getBundle("data")
        }

    /**
     * 获取指定key的Bundle对象
     *
     * @param bundleKey
     * @author dingpeihua
     * @date 2016/6/29 11:00
     * @version 1.0
     */
    protected fun getBundle(bundleKey: String?): Bundle {
        return getBundle(intent, bundleKey)
    }

    /**
     * 获取获取指定Intent对象中的Bundle对象
     *
     * @param intent
     * @author dingpeihua
     * @date 2016/6/29 11:00
     * @version 1.0
     */
    protected fun getBundle(intent: Intent?): Bundle {
        return getBundle(intent, "data")
    }

    /**
     * 取获取指定Intent对象中的指定的key的Bundle对象
     *
     * @param intent
     * @param bundleKey
     * @author dingpeihua
     * @date 2016/6/29 11:00
     * @version 1.0
     */
    protected fun getBundle(intent: Intent?, bundleKey: String?): Bundle {
        var bundle: Bundle? = null
        if (intent != null) {
            bundle = intent.getBundleExtra(bundleKey)
            if (bundle == null) {
                bundle = intent.extras
            }
        }
        return bundle ?: Bundle()
    }

    override fun getContainerViewId(): Int {
        return 0
    }

    override fun getFragmentTag(): String? {
        return null
    }

    override fun makeFragmentKey(index: Int): String? {
        return FragmentHelper.SAVE_INSTANCE_FRAGMENT + index
    }

    open fun findFragment(fragmentKey: String?): Fragment? {
        return fragmentHelper?.findFragment(fragmentKey)
    }

    override fun isSaveInstance(): Boolean {
        return true
    }

    /**
     * 是否需要拦截事件，并清除EditText焦点
     *
     * @return true 需要拦截，否则不拦截
     * @author dingpeihua
     * @date 2017/8/19 17:38
     * @version 1.0
     */
    protected open fun isInterceptClearFocus(): Boolean {
        return false
    }

    open fun tryDispatchTouchEventException() {}

    /**
     * 跳转到指定的Activity
     *
     * @param targetActivity 要跳转的目标Activity
     */
    protected fun startActivity(targetActivity: Class<*>) {
        startActivity(Intent(this, targetActivity))
    }

    protected fun startActivityForResult(targetActivity: Class<*>, requestCode: Int) {
        startActivityForResult(Intent(this, targetActivity), requestCode)
    }

    /**
     * 跳转到指定的Activity
     *
     * @param targetActivity 要跳转的目标Activity
     */
    protected fun startActivity(targetActivity: Class<*>, bundle: Bundle?) {
        startActivity(Intent(this, targetActivity).putExtras(bundle!!))
    }

    protected fun startActivityForResult(
        targetActivity: Class<*>,
        requestCode: Int,
        bundle: Bundle?
    ) {
        startActivityForResult(Intent(this, targetActivity).putExtras(bundle!!), requestCode)
    }

    open fun removeAll() {
        fragmentHelper?.removeAll()
    }

    open fun remove(fragment: Fragment?): Fragment? {
        return fragmentHelper?.remove(fragment)
    }

    /**
     * 获取fragments 列表
     *
     * @author dingpeihua
     * @date 2019/1/31 17:05
     * @version 1.0
     */
    open val fragments: MutableList<Fragment>
        get() {
            return fragmentHelper?.fragments ?: arrayListOf()
        }

    /**
     * 获取当前显示的fragment
     *
     * @author dingpeihua
     * @date 2019/1/31 17:51
     * @version 1.0
     */
    open val showFragment: Fragment?
        get() {
            return fragmentHelper?.showFragment
        }

    /**
     * 获取当前显示的fragment的索引
     *
     * @author dingpeihua
     * @date 2019/1/31 17:51
     * @version 1.0
     */
    open val showFragmentIndex: Int
        get() {
            return fragmentHelper?.showFragmentIndex ?: 0
        }

    /**
     * 仅添加一个个fragment到[FragmentHelper.fragments]列表中，不做显示处理
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    open fun addRepeatFragment(fragment: Fragment?) {
        fragmentHelper?.addFragment(fragment, true)
    }

    /**
     * 仅添加一个个fragment到[FragmentHelper.fragments]列表中，不做显示处理
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    open fun addFragment(fragment: Fragment?, isRepeatable: Boolean) {
        fragmentHelper?.addFragment(fragment, isRepeatable)
    }

    /**
     * 仅添加一个个fragment到[FragmentHelper.fragments]列表中，不做显示处理
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:32
     * @version 1.0
     */
    open fun addFragment(fragment: Fragment?) {
        fragmentHelper?.addFragment(fragment)
    }

    /**
     * 仅添加多个fragment 到[FragmentHelper.fragments]列表中，不做显示处理
     *
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 16:59
     * @version 1.0
     */
    fun addFragments(vararg fragments: Fragment?) {
        fragmentHelper?.addFragments(*fragments)
    }

    /**
     * 添加fragment列表，并显示第一个
     *
     * @param showIndex
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 17:00
     * @version 1.0
     */
    fun <T : Fragment?> showFragment(showIndex: Int, vararg fragments: T): T? {
        fragmentHelper?.addFragments(*fragments)
        val fragment = showFragment(showIndex)
        val f = fragments[showIndex]
        return fragmentHelper?.cast(f, fragment)
    }

    /**
     * 添加fragment列表，并显示第一个
     *
     * @param fragments
     * @author dingpeihua
     * @date 2019/1/31 17:00
     * @version 1.0
     */
    fun <T : Fragment?> showFragment(vararg fragments: T): T? {
        return showFragment(0, *fragments)
    }

    /**
     * 显示列表中指定索引位置的fragment
     *
     * @param showFragmentIndex
     * @author dingpeihua
     * @date 2019/1/31 17:02
     * @version 1.0
     */
    fun showFragment(showFragmentIndex: Int): Fragment? {
        fragmentHelper?.showFragment(showFragmentIndex)
        return showFragment
    }

    /**
     * 显示指定的fragment，如果未添加，则会先添加到fragments列表
     * 注意：如果[androidx.fragment.app.FragmentManager]中有保存相同类型的fragment，
     * 则会使用其恢复显示，此时当前返回的fragment即为显示的fragment
     *
     * @param fragment
     * @author dingpeihua
     * @date 2019/1/31 17:03
     * @version 1.0
     */
    fun <T : Fragment?> showFragment(fragment: T): T? {
        return fragmentHelper?.showFragment(fragment)
    }

    /**
     * 将当前显示的fragment转换成目标fragment，并返回
     *
     * @param fragment 目标fragment
     * @author dingpeihua
     * @date 2019/11/26 10:47
     * @version 1.0
     */
    fun <T> cast(fragment: T?): T? {
        return cast(fragment, showFragment)
    }

    /**
     * 将当前显示的fragment转换成目标fragment，并返回
     *
     * @param fragment     目标fragment
     * @param showFragment 当前显示的fragment
     * @author dingpeihua
     * @date 2019/11/26 10:47
     * @version 1.0
     */
    fun <T> cast(fragment: T, showFragment: Fragment?): T? {
        return fragmentHelper?.cast(fragment, showFragment)
    }

    fun sendMessage(msg: Message?): Boolean {
        return mHandler.sendMessage(msg!!)
    }

    fun sendEmptyMessage(what: Int): Boolean {
        return mHandler.sendEmptyMessage(what)
    }

    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        return mHandler.sendEmptyMessageDelayed(what, delayMillis)
    }

    fun sendEmptyMessageAtTime(what: Int, uptimeMillis: Long): Boolean {
        return mHandler.sendEmptyMessageAtTime(what, uptimeMillis)
    }

    fun sendMessageDelayed(msg: Message?, delayMillis: Long): Boolean {
        return mHandler.sendMessageDelayed(msg!!, delayMillis)
    }

    fun sendMessageAtTime(msg: Message?, uptimeMillis: Long): Boolean {
        return mHandler.sendMessageAtTime(msg!!, uptimeMillis)
    }

    open fun post(runnable: Runnable?) {
        mHandler.post(runnable!!)
    }

    open fun postDelayed(runnable: Runnable?, delayMillis: Long) {
        mHandler.postDelayed(runnable!!, delayMillis)
    }

    /**
     * Remove any pending posts of messages with code 'what' that are in the
     * message queue.
     */
    fun removeMessages(what: Int) {
        mHandler.removeMessages(what)
    }

    /**
     * Remove any pending posts of messages with code 'what' and whose obj is
     * 'object' that are in the message queue.  If <var>object</var> is null,
     * all messages will be removed.
     */
    fun removeMessages(what: Int, `object`: Any?) {
        mHandler.removeMessages(what, `object`)
    }

    fun removeCallbacks(r: Runnable) {
        mHandler.removeCallbacks(r)
    }

    /**
     * Remove any pending posts of callbacks and sent messages whose
     * <var>obj</var> is <var>token</var>.  If <var>token</var> is null,
     * all callbacks and messages will be removed.
     */
    fun removeCallbacksAndMessages(token: Any?) {
        mHandler.removeCallbacksAndMessages(token)
    }

    fun obtainMessage(): Message {
        return mHandler.obtainMessage()
    }

    companion object {

        /**
         * loading dialog 显示
         */
        private const val WHAT_SHOW_PROGRESS_DIALOG = 0x01011

        /**
         * loading dialog 关闭
         */
        private const val WHAT_DISMISS_PROGRESS_DIALOG = 0x01012
    }
}