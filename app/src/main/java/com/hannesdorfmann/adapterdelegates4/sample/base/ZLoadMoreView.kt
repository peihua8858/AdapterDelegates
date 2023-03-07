//package com.hannesdorfmann.adapterdelegates4.sample.base
///*
//
//                   _ooOoo_
//                  o8888888o
//                  88" . "88
//                  (| -_- |)
//                  O\  =  /O
//               ____/`---'\____
//             .'  \\|     |//  `.
//            /  \\|||  :  |||//  \
//           /  _||||| -:- |||||-  \
//           |   | \\\  -  /// |   |
//           | \_|  ''\---/''  |   |
//           \  .-\__  `-`  ___/-. /
//         ___`. .'  /--.--\  `. . __
//      ."" '<  `.___\_<|>_/___.'  >'"".
//     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//     \  \ `-.   \_ __\ /__ _/   .-` /  /
//======`-.____`-.___\_____/___.-`____.-'======
//                   `=---='
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//         佛祖保佑       永无BUG
//
//*/import android.view.View
//import android.view.ViewGroup
//import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
//import com.chad.library.adapter.base.viewholder.BaseViewHolder
//import com.fz.common.view.utils.getItemView
//import com.zaful.base.R
//
///**
// * 项目名称: YOSHOP
// * 类描述：
// * 创建人：Created by tanping
// * 创建时间:2018/7/11 10:56
// */
//open class ZLoadMoreView : BaseLoadMoreView() {
//    override fun getLoadComplete(holder: BaseViewHolder): View {
//        return holder.getView(R.id.load_more_load_complete_view)
//    }
//
//    override fun getLoadEndView(holder: BaseViewHolder): View {
//        return holder.getView(R.id.load_more_load_end_view)
//    }
//
//    override fun getLoadFailView(holder: BaseViewHolder): View {
//        return holder.getView(R.id.load_more_load_fail_view)
//    }
//
//    override fun getLoadingView(holder: BaseViewHolder): View {
//        return holder.getView(R.id.load_more_loading_view)
//    }
//
//    override fun getRootView(parent: ViewGroup): View {
//        return parent.getItemView(R.layout.zf_quick_view_load_more)
//    }
//}