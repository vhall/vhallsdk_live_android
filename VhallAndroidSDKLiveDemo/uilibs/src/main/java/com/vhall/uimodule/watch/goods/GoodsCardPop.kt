package com.vhall.uimodule.watch.goods

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vhall.business.data.WebinarInfo
import com.vhall.uimodule.R
import com.vhall.uimodule.databinding.ItemGoodsCardBinding
import com.vhall.uimodule.utils.DensityUtils
import com.vhall.uimodule.widget.CornerTransform
import com.vhall.vhss.data.GoodsInfoData.GoodsInfo


class GoodsCardPop @JvmOverloads constructor(private val mContext: Context, var webinarInfo : WebinarInfo, var goodsInfo:  GoodsInfo, l : View.OnClickListener) :
    PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    View.OnTouchListener {
    private var lastX = 0
    private var lastY = 0
    private var binding: ItemGoodsCardBinding
    init {
        val root = View.inflate(mContext, R.layout.item_goods_card, null)
        binding = ItemGoodsCardBinding.bind(root)
        contentView = root
        setBackgroundDrawable(null)
        isOutsideTouchable = false
        isFocusable = false

        binding.ivCancel.setOnClickListener {
            dismiss()
        }
        binding.draggableLayout.setOnClickListener(l)

        // 添加触摸事件监听器
//        binding.draggableLayout.setOnTouchListener(this)

        updateUI(goodsInfo)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        var offsetX: Float = 0.0f;
        var offsetY: Float = 0.0f;
        var lastX: Float = 0.0f;
        var lastY: Float = 0.0f;
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录手指按下时的位置
                lastX = event.getRawX();
                lastY = event.getRawY();
                offsetX = lastX - getContentView().getX();
                offsetY = lastY - getContentView().getY();
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.rawX
                val moveY = event.rawY
                val distanceX = moveX - lastX
                val distanceY = moveY - lastY
                val newX = moveX - offsetX
                val newY = moveY - offsetY

                // 更新 PopupWindow 的位置

                // 更新 PopupWindow 的位置
                update(newX.toInt(), newY.toInt(), -1, -1, true)
                lastX = moveX
                lastY = moveY
            }
        }
        return true
    }

    fun updateUI(goodsInfo: GoodsInfo) {
        this.goodsInfo = goodsInfo
        binding.tvName.setText(goodsInfo.name)
        binding.ivHead.setScaleType(ImageView.ScaleType.CENTER_CROP)
//        val options = RequestOptions().transform(RoundedCorners(DensityUtils.dpToPxInt(4f)))
//        Glide.with(mContext).load(goodsInfo.cover_img).apply(options).into(binding.ivHead)
        val cornerTransform = CornerTransform(mContext,DensityUtils.dpToPx(4f))
        cornerTransform.setNeedCorner(true, true, false, false)
        val options: RequestOptions = RequestOptions().transform(cornerTransform)
        Glide.with(mContext).asBitmap()
            .load(goodsInfo.cover_img)
            .apply(options)
            .into(binding.ivHead)

//        binding.ivHead.setOutlineProvider(
//            CardDialog.RoundViewOutlineProvider(
//                DensityUtils.dpToPxInt(4f).toFloat()
//            )
//        )
//        binding.ivHead.setClipToOutline(true)
        val builder = SpannableStringBuilder()
        var text = if (TextUtils.isEmpty(goodsInfo.discount_price)) goodsInfo.price else goodsInfo.discount_price
        if (TextUtils.isEmpty(text)){
            builder.append("")
            binding.tvL.visibility=View.INVISIBLE
        }
        else{
            builder.append(text)
            binding.tvL.visibility=View.VISIBLE
            val sizeSpan2 = RelativeSizeSpan(1.0f)
            builder.setSpan(sizeSpan2, 1, text.length - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val sizeSpan3 = RelativeSizeSpan(0.8f)
            builder.setSpan(sizeSpan3, text.length - 2, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvPrice.setText(builder)
    }

    fun getCurCardGoodsInfo():GoodsInfo?{
        return this.goodsInfo
    }
}