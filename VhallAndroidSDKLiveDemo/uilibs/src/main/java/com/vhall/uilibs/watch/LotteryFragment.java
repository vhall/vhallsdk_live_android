package com.vhall.uilibs.watch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.LotteryPrizeListInfo;
import com.vhall.business.data.LotteryWinnerData;
import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallback;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * 抽奖的Fragment
 *
 * @author huohuo
 */
public class LotteryFragment extends Fragment implements WatchContract.LotteryView {


    public static LotteryFragment newInstance() {
        LotteryFragment articleFragment = new LotteryFragment();
        return articleFragment;
    }

    private RecyclerView lotteryWinnerRecyclerView, commitRecyclerView;
    private ImageView ivCover, ivEnd, ivClose;
    private TextView tvCommit, tvLotteryIng, tvLotterName, tvEndHint, tvEndBtn, tvJoin;
    private View rootView;
    private LinearLayout llIng, llEnd, llCommit, llResult, llOldCommit;
    private String lotteryId;
    private MessageServer.MsgInfo lotteryData;
    private LotteryWinnerAdapter lotteryWinnerAdapter;
    private LotteryCommitAdapter lotteryCommitAdapter;
    private ImageView ivResult;
    private EditText etName, etPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.lottery_fragment, null);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ivClose = rootView.findViewById(R.id.iv_close);
        llIng = rootView.findViewById(R.id.ll_lottery_ing);
        llEnd = rootView.findViewById(R.id.ll_lottery_end);
        llCommit = rootView.findViewById(R.id.ll_lottery_commit);
        llResult = rootView.findViewById(R.id.ll_lottery_result);
        llOldCommit = rootView.findViewById(R.id.commit_ll);
        etName = rootView.findViewById(R.id.et_name);
        etPhone = rootView.findViewById(R.id.et_tel);
        lotteryWinnerRecyclerView = rootView.findViewById(R.id.recycle_view);
        commitRecyclerView = rootView.findViewById(R.id.commit_recycle_view);
        ivResult = rootView.findViewById(R.id.iv_result);
        ivCover = rootView.findViewById(R.id.iv_cover);
        ivEnd = rootView.findViewById(R.id.iv_end);
        tvCommit = rootView.findViewById(R.id.tv_lottery_commit);
        tvLotteryIng = rootView.findViewById(R.id.tv_lottery_ing);
        tvLotterName = rootView.findViewById(R.id.tv_lottery_name);
        tvEndHint = rootView.findViewById(R.id.tv_end_hint);
        tvEndBtn = rootView.findViewById(R.id.tv_look_winner);
        tvJoin = rootView.findViewById(R.id.tv_join_lottery);

        lotteryWinnerAdapter = new LotteryWinnerAdapter(lotteryWinnerRecyclerView);
        lotteryWinnerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lotteryWinnerRecyclerView.setAdapter(lotteryWinnerAdapter);

        commitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lotteryCommitAdapter = new LotteryCommitAdapter(commitRecyclerView);
        commitRecyclerView.setAdapter(lotteryCommitAdapter);

        tvEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llEnd.setVisibility(View.GONE);
                if (endBtnGoLook) {
                    if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.award != null && !TextUtils.isEmpty(lotteryData.lotteryInfo.award.image_url)) {
                        RequestOptions requestOptions = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.icon_default_avatar);
                        Glide.with(getActivity()).load(lotteryData.lotteryInfo.award.image_url).apply(requestOptions).into(ivResult);
                    }
                    /**
                     * 去查看获奖名单 if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.is_new == 1) 这个时候列表通过接口获取
                     */
                    if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.is_new == 1) {
                        VhallSDK.getLotteryWinner(lotteryData.lotteryInfo.room_id, lotteryData.lotteryInfo.lottery_id, new RequestDataCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                LotteryWinnerData result = (LotteryWinnerData) o;
                                lotteryWinnerAdapter.setData(result.list);
                                llEnd.setVisibility(View.GONE);
                                llResult.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.is_new == 1) {
                        commitRecyclerView.setVisibility(View.VISIBLE);
                        llOldCommit.setVisibility(View.GONE);
                        VhallSDK.getPrizeInfo(lotteryData.webinar_id, new RequestDataCallback() {
                            @Override
                            public void onSuccess(Object data) {
                                List<LotteryPrizeListInfo> lotteryPrizeListInfos = (List<LotteryPrizeListInfo>) data;
                                if (lotteryPrizeListInfos != null && lotteryPrizeListInfos.size() > 0) {
                                    lotteryCommitAdapter.setData(lotteryPrizeListInfos);
                                    llCommit.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(getContext(), "返回数据缺少必要参数", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        llCommit.setVisibility(View.VISIBLE);
                        commitRecyclerView.setVisibility(View.GONE);
                        llOldCommit.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        tvCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "", phone = "";
                if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.is_new == 1) {
                    for (int i = 0; i < lotteryCommitAdapter.getDataList().size(); i++) {
                        LotteryPrizeListInfo lotteryPrizeListInfo = lotteryCommitAdapter.getDataList().get(i);
                        if (lotteryPrizeListInfo.is_required == 1) {
                            if (TextUtils.isEmpty(lotteryPrizeListInfo.field_value)) {
                                Toast.makeText(getContext(), "请填写" + lotteryPrizeListInfo.field, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        if ("name".equals(lotteryPrizeListInfo.field_key)) {
                            name = lotteryPrizeListInfo.field_value;
                        }
                        if ("phone".equals(lotteryPrizeListInfo.field_key)) {
                            phone = lotteryPrizeListInfo.field_value;
                        }
                    }
                } else {
                    name = etName.getText().toString().trim();
                    phone = etPhone.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(), "请填写昵称", Toast.LENGTH_SHORT).show();
                        return;

                    }
                    if (TextUtils.isEmpty(phone)) {
                        Toast.makeText(getContext(), "请填写电话", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                VhallSDK.submitLotteryInfo(VhallSDK.getUserId(), lotteryId, name, phone, markToString(lotteryCommitAdapter.getDataList()), new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        llCommit.setVisibility(View.GONE);
                        llEnd.setVisibility(View.VISIBLE);
                        tvEndBtn.setText(getString(R.string.lottery_look_winner));
                        if (lotteryData != null && lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.publish_winner == 0) {
                            tvEndBtn.setVisibility(View.GONE);
                        } else {
                            tvEndBtn.setVisibility(View.VISIBLE);
                        }
                        tvEndHint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_FC5659));
                        endBtnGoLook = true;
                        tvEndHint.setText(getString(R.string.lottery_commit_success));
                        ivEnd.setBackgroundResource(R.drawable.icon_success_commit_prize);
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e("dd", errorMsg);
                        llCommit.setVisibility(View.GONE);
                        llEnd.setVisibility(View.VISIBLE);
                        tvEndBtn.setText(getString(R.string.lottery_look_winner));
                        if (lotteryData != null && lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.publish_winner == 0) {
                            tvEndBtn.setVisibility(View.GONE);
                        } else {
                            tvEndBtn.setVisibility(View.VISIBLE);
                        }
                        tvEndHint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_22));
                        endBtnGoLook = true;
                        tvEndHint.setText(getString(R.string.lottery_commit_failed));
                        ivEnd.setBackgroundResource(R.drawable.icon_error_commit_prize);
                    }
                });
            }
        });
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }

    @Override
    public void setLotteryData(final MessageServer.MsgInfo lotteryData) {
        dealData(lotteryData);
    }

    private void dealData(MessageServer.MsgInfo lotteryData) {
        this.lotteryData = lotteryData;
        switch (lotteryData.event) {
            case MessageServer.EVENT_START_LOTTERY:
                llIng.setVisibility(View.VISIBLE);
                llCommit.setVisibility(View.GONE);
                llEnd.setVisibility(View.GONE);
                llResult.setVisibility(View.GONE);
                String image = "http://t-alistatic01.e.vhall.com/upload/sys/img_url/e0/2b/e02b57d63947b5ec20c57c144686cd7d.gif";
                final MessageServer.LotteryInfo lotteryInfo = lotteryData.lotteryInfo;
                if (lotteryInfo != null) {
                    tvLotteryIng.setText(lotteryInfo.remark);
                }
                if (lotteryInfo != null && lotteryInfo.lottery_type.equals("8")) {
                    image = lotteryInfo.icon;
                    String text = "发送口令\"" + lotteryInfo.command + "\"参与抽奖吧！";
                    SpannableStringBuilder builder = new SpannableStringBuilder(text);
                    tvLotteryIng.setTextColor(Color.parseColor("#222222"));
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5659")), 4, lotteryInfo.command.length() + 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvLotteryIng.setText(builder);
                    tvJoin.setVisibility(View.VISIBLE);
                    tvJoin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VhallSDK.joinCodeLottery(lotteryInfo.room_id, lotteryInfo.lottery_id, lotteryInfo.command, new RequestCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "send success", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else if (lotteryInfo != null) {
                    image = lotteryInfo.icon;
                    tvJoin.setVisibility(View.GONE);
                }
                RequestOptions options = new RequestOptions().placeholder(R.drawable.icon_gift);
                Glide.with(getActivity()).load(image).apply(options).into(ivCover);
                break;
            case MessageServer.EVENT_END_LOTTERY:
                if (lotteryData.lotteries != null && lotteryData.lotteries.size() > 0) {
                    lotteryWinnerAdapter.setData(lotteryData.lotteries);
                    List<MessageServer.Lottery> lottery_winners = lotteryData.lotteries;
                    for (int i = 0; i < lottery_winners.size(); i++) {
                        if (lottery_winners.get(i).isSelf) {
                            /**
                             * 要从两个地方获取确保没问题
                             */
                            lotteryId = lottery_winners.get(i).lottery_id;
                        }
                    }
                }
                String award_name = getString(R.string.lottery);
                if (lotteryData.lotteryInfo != null) {
                    lotteryId = lotteryData.lotteryInfo.lottery_id;
                }
                if (lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.award != null && !TextUtils.isEmpty(lotteryData.lotteryInfo.award.award_name)) {
                    award_name = lotteryData.lotteryInfo.award.award_name;
                }
                tvLotterName.setText(award_name);
                if (lotteryData.winnerLottery) {
                    //自己中奖
                    llIng.setVisibility(View.GONE);
                    llEnd.setVisibility(View.VISIBLE);
                    tvEndBtn.setText(getString(R.string.accept_the_prize));
                    tvEndBtn.setVisibility(View.VISIBLE);
                    endBtnGoLook = false;
                    tvEndHint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_FC5659));
                    tvEndHint.setText(String.format(getString(R.string.lottery_win), award_name));
                    ivEnd.setBackgroundResource(R.drawable.icon_win_prize);
                } else {
                    //自己没中奖
                    llIng.setVisibility(View.GONE);
                    llEnd.setVisibility(View.VISIBLE);
                    tvEndBtn.setText(getString(R.string.lottery_look_winner));
                    tvEndHint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_22));
                    if (lotteryData != null && lotteryData.lotteryInfo != null && lotteryData.lotteryInfo.publish_winner == 0) {
                        tvEndBtn.setVisibility(View.GONE);
                    } else {
                        tvEndBtn.setVisibility(View.VISIBLE);
                    }
                    endBtnGoLook = true;
                    tvEndHint.setText(getString(R.string.lottery_lose));
                    ivEnd.setBackgroundResource(R.drawable.icon_lose_prize);
                }
                break;
        }
    }

    private boolean endBtnGoLook = false;

    private String markToString(List<LotteryPrizeListInfo> data) {
        String mark = "";
        if (data != null && data.size() > 0) {
            JSONArray array = new JSONArray();
            for (LotteryPrizeListInfo datum : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("field", datum.field);
                    object.put("is_required", datum.is_required);
                    object.put("is_system", datum.is_system);
                    object.put("field", datum.field);
                    object.put("rank", datum.rank);
                    object.put("field_value", datum.field_value);
                    object.put("field_key", datum.field_key);
                    object.put("placeholder", datum.placeholder);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(object);
            }
            mark = array.toString();
        }
        return mark;
    }
}
