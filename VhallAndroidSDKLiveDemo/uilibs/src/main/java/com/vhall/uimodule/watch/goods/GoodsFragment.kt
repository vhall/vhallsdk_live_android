package com.vhall.uimodule.watch.goods

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.vhall.business.MessageServer.MsgInfo
import com.vhall.business.data.RequestDataCallbackV2
import com.vhall.business.data.WebinarInfo
import com.vhall.business.module.goods.GoodsMessageCallBack
import com.vhall.business.module.goods.GoodsServer
import com.vhall.uimodule.base.BaseFragment
import com.vhall.uimodule.base.IBase
import com.vhall.uimodule.databinding.FragmentGoodsBinding
import com.vhall.uimodule.watch.WatchLiveActivity
import com.vhall.uimodule.watch.coupon.CouponListDialog
import com.vhall.uimodule.watch.gift.GiftListDialog
import com.vhall.vhss.data.GoodsInfoData
import com.vhall.vhss.data.GoodsInfoData.GoodsInfo
import com.vhall.vhss.data.OrderInfoData

class GoodsFragment : BaseFragment<FragmentGoodsBinding>(FragmentGoodsBinding::inflate) {
    companion object {
        @JvmStatic
        fun newInstance(info: WebinarInfo, type: String) =
            GoodsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(IBase.INFO_KEY, info)
                    putString(TYPE, type)
                }
            }
    }
    private val TYPE = "type"
    private var type: String? = ""
    lateinit var goodsAdapter: GoodsAdapter
    private var goodsServer: GoodsServer? = null

    private var goodsDetailsDialog: GoodsDetailsDialog? = null
    private var goodsOrderDialog: GoodsOrderDialog? = null

    private var curCardGoodsInfo: GoodsInfo? = null

    override fun initView() {
        val activity: WatchLiveActivity = activity as WatchLiveActivity
        arguments?.let {
            webinarInfo = it.getSerializable(IBase.INFO_KEY) as WebinarInfo
            type = it.getString(TYPE)
        }

        goodsAdapter = GoodsAdapter(mContext,webinarInfo,this)
        goodsAdapter.setOnItemClickListener(OnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>?, view: View?, i: Int ->
            showGoodsDetailsDialog(goodsAdapter.data.get(i))
        })
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(mContext)
        mViewBinding.recycleView.adapter = goodsAdapter
        mViewBinding.recycleView.setHasFixedSize(true)
        mViewBinding.refreshLayout.setOnRefreshListener {
            loadData()
        }
        mViewBinding.tvCouponEnter.setOnClickListener {
            val couponListDialog = CouponListDialog(mContext, webinarInfo,"",0)
            couponListDialog.show()
        }

        goodsServer = GoodsServer.Builder()
            .context(context)
            .webinarInfo(webinarInfo)
            .goodsMessageCallBack(object : GoodsMessageCallBack() {
                override fun pushGoodsCard(msgInfo: MsgInfo,push_status : Int) {
                    showGoodsCard(msgInfo.goodsInfo);
                    if(push_status == 1)
                        loadData()
                }

                override fun addGoodsInfo( goodsInfo: GoodsInfoData.GoodsInfo,goods_list_cdn_url:String) {
                    loadData()
                }

                override fun deleteGoods(del_goods_ids: List<String>,goods_list_cdn_url:String) {
                    for (goods_id : String in del_goods_ids){
                        //删除正在推送的商品，关闭卡片
                        if(curCardGoodsInfo!=null && curCardGoodsInfo?.push_status == 1 && curCardGoodsInfo?.goods_id.equals(goods_id)){
                            curCardGoodsInfo?.push_status = 0;
                            showGoodsCard(curCardGoodsInfo!!);
                        }
                        //删除正在显示详情的商品，关闭详情
                        if(goodsDetailsDialog?.isShowing ==true && goodsDetailsDialog?.goodsInfo?.goods_id.equals(goods_id)){
                            goodsDetailsDialog?.dismiss()
                            Toast.makeText(context, "当前商品已下架或删除", Toast.LENGTH_SHORT).show()
                        }

                        //删除正在显示详情的商品，关闭
                        if(goodsOrderDialog?.isShowing ==true && goodsOrderDialog?.goodsInfo?.goods_id.equals(goods_id)){
                            goodsOrderDialog?.dismiss()
                            Toast.makeText(context, "当前商品已下架或删除", Toast.LENGTH_SHORT).show()
                        }

                    }
                    loadData()
                }

                override fun updateGoodsInfo(goodsInfo: GoodsInfoData.GoodsInfo,goods_list_cdn_url:String) {
                    //商品信息更新，更新正在推屏的商品卡片
                    if(curCardGoodsInfo!=null && curCardGoodsInfo?.push_status == 1 && curCardGoodsInfo?.goods_id.equals(goodsInfo.goods_id)){
                        showGoodsCard(goodsInfo);
                    }
                    //商品信息更新，更新正在推屏的商品卡片
                    if( goodsDetailsDialog?.isShowing ==true && goodsDetailsDialog?.goodsInfo?.goods_id.equals(goodsInfo.goods_id)){
                        showGoodsDetailsDialog(goodsInfo);
                        Toast.makeText(context, "当前商品信息发生变化", Toast.LENGTH_SHORT).show()
                    }

                    //商品信息更新，更新正在推屏的商品卡片
                    if( goodsOrderDialog?.isShowing ==true && goodsOrderDialog?.goodsInfo?.goods_id.equals(goodsInfo.goods_id)){
                        showGoodsOrderDialog(goodsInfo);
                        Toast.makeText(context, "当前商品信息发生变化", Toast.LENGTH_SHORT).show()
                    }
                    loadData()
                }
                override fun updateGoodsList(goods_list_cdn_url: String) {
                    loadData()
                }
                override fun orderStatusChange(orderInfo: OrderInfoData){

                }
            })
            .build()

        loadData(true)
    }

    private fun loadData(){
        loadData(false)
    }

    private fun loadData(load: Boolean){
        mViewBinding.refreshLayout.isRefreshing = true;
        goodsServer?.getGoodsList(object : RequestDataCallbackV2<GoodsInfoData?> {
                override fun onSuccess(data: GoodsInfoData?) {
                    mViewBinding.refreshLayout.isRefreshing = false
                    if (data != null) {
                        goodsAdapter.setList(data.list)
                        (activity as WatchLiveActivity).call( IBase.SHOW_GOODS_TAB, if ( data.list.count()>0 ) "show_goods_tab" else "hide_goods_tab" ,null)
                        if(load){
                            //加载商品页弹出推屏商品
                            for(goodsInfo in data.list){
                                if(goodsInfo.push_status == 1){
                                    showGoodsCard(goodsInfo);
                                    break
                                }
                            }
                        }
                    }
                }
                override fun onError(errorCode: Int, errorMsg: String) {
                    mViewBinding.refreshLayout.isRefreshing = false
                }
            })
    }

    public fun showGoodsCard(goodsInfo:GoodsInfo){
        curCardGoodsInfo = goodsInfo
        (activity as WatchLiveActivity).call(IBase.SHOW_GOODS_CARD, "", goodsInfo)
    }

    public fun showGoodsDetailsDialog(goodsInfo: GoodsInfo?){
        if(goodsInfo == null)
            return

        if(goodsDetailsDialog == null || goodsDetailsDialog?.isShowing == false) {
            goodsDetailsDialog = GoodsDetailsDialog(mContext, webinarInfo, goodsInfo, View.OnClickListener {
                when (goodsInfo.buy_type) {
                    1 -> {
                        showGoodsOrderDialog(goodsDetailsDialog!!.goodsInfo)
                        goodsDetailsDialog?.dismiss()
                    }
                    2 -> {
                        //外链购买
                        if (!TextUtils.isEmpty(goodsInfo.url) && goodsInfo.url.contains("http")) {
                            //通过浏览器打开URL
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(goodsDetailsDialog!!.goodsInfo.url)
                            startActivity(intent)
                            goodsDetailsDialog?.dismiss()
                        } else {
                            Toast.makeText(context, "请填写正确的url地址", Toast.LENGTH_SHORT).show()
                        }
                    }
                    3 -> Toast.makeText(context,"三方商品id: " + goodsDetailsDialog!!.goodsInfo.third_goods_id,Toast.LENGTH_SHORT).show()
                    else -> {
                    }
                }
            })
        }
        else
            goodsDetailsDialog?.updateUI(goodsInfo)
        goodsDetailsDialog?.show()
    }

    fun showGoodsOrderDialog(goodsInfo:GoodsInfo){
        if(goodsOrderDialog == null || goodsOrderDialog?.isShowing == false) {
            goodsOrderDialog = GoodsOrderDialog(mContext, webinarInfo, goodsInfo)
        }
        else
            goodsOrderDialog?.updateUI(goodsInfo)
        goodsOrderDialog?.show()
    }
}