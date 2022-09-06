package com.vhall.uilibs.watch.minimalist;

import static com.vhall.uilibs.interactive.RtcInternal.REQUEST_PUSH;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vhall.business.ChatServer;
import com.vhall.business.MessageServer;
import com.vhall.business.VhallSDK;
import com.vhall.business.data.RequestCallback;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.RtcInternal;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.util.BaseUtil;
import com.vhall.uilibs.util.ListUtils;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;
import com.vhall.uilibs.widget.GiftListDialog;
import com.vhall.uilibs.widget.pushView.PushViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 极简模式观看
 */
public class MinimalistWatchActivity extends MinimalistBaseWatchActivity {
    @Override
    public void initInputView() {
        if (inputView == null) {
            inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
            inputView.add2Window(this);
            inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
                @Override
                public void onSendClick(String msg, InputUser user) {
                    if (watchMiniFragment != null) {
                        watchMiniFragment.sendMgs(msg);
                    }
                }
            });
            inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
                @Override
                public void onHeightReceived(int screenOri, int height) {
                    if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        KeyBoardManager.setKeyboardHeight(MinimalistWatchActivity.this, height);
                    } else {
                        KeyBoardManager.setKeyboardHeightLandspace(MinimalistWatchActivity.this, height);
                    }
                }
            });
        }
    }

    @Override
    public void initGiftPush() {
        push_view = findViewById(R.id.fl_notice_push_view);
        iv_push = findViewById(R.id.iv_push);
        tv_push = findViewById(R.id.tv_push);
        pushView = new PushViewUtils(this);
        pushView.setDismissCallBack(new PushViewUtils.DismissCallBack() {
            @Override
            public void viewDismiss() {
                push_view.setVisibility(View.GONE);
            }
        });
        pushView.setShowCallBack(new PushViewUtils.ShowCallBack() {
            @Override
            public void viewShow(View view, Object showData) {
                MessageServer.GiftInfoData giftInfoData = (MessageServer.GiftInfoData) showData;
                Glide.with(MinimalistWatchActivity.this).load(giftInfoData.gift_image_url).apply(new RequestOptions()).into(iv_push);
                StringBuilder stringBuilder = new StringBuilder().append(BaseUtil.getLimitString(giftInfoData.gift_user_nickname, 8)).append(" 送出").append(giftInfoData.gift_name);
                tv_push.setText(stringBuilder);
                push_view.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void initLike() {
        iv_like = findViewById(R.id.iv_like);
        iv_like.setOnClickListener(this);
        pressLikeView = findViewById(R.id.press_like);
        imageUrls.add(R.drawable.icon_live_heart1);
        imageUrls.add(R.drawable.icon_live_heart6);
        imageUrls.add(R.drawable.icon_live_heart2);
        imageUrls.add(R.drawable.icon_live_heart7);
        imageUrls.add(R.drawable.icon_live_heart3);
        imageUrls.add(R.drawable.icon_live_heart4);
        imageUrls.add(R.drawable.icon_live_heart5);
        imageUrls.add(R.drawable.icon_live_heart8);
        pressLikeView.setImageResources(imageUrls);
    }

    @Override
    protected void initChatHistory() {
        watchMiniFragment.getChatHistory(new ChatServer.ChatRecordCallback() {
            @Override
            public void onDataLoaded(List<ChatServer.ChatInfo> list) {
                List<ChatMessageData> messageDataList = new ArrayList<>();
                if (!ListUtils.isEmpty(list)) {
                    for (ChatServer.ChatInfo chatInfo : list) {
                        ChatMessageData chatMessageData = new ChatMessageData();
                        chatMessageData.chatInfo = chatInfo;
                        messageDataList.add(chatMessageData);
                    }
                }
                watchLive = watchMiniFragment.getWatchLive();
                Collections.reverse(messageDataList);
                chatAdapter.addData(messageDataList);
                if (chatAdapter.getItemCount() > 0)
                    recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onFailed(int errorcode, String messaage) {

            }
        });
    }

    @Override
    public void pushLikeNum(int num) {
        VhallSDK.userLike(webinarInfo.vss_room_id, String.valueOf(num), new RequestCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void startMicCount() {
        iv_mic.setVisibility(View.GONE);
        tv_mic.setVisibility(View.VISIBLE);
        if (micCount == null) {
            micCount = new GetCodeCount(30 * 1000, 1000);
        }
        micCount.start();
    }

    @Override
    public void showInvited() {
        //被邀请上麦  1接受，2拒绝，3超时失败
        if (showInvited == null) {
            showInvited = new OutDialogBuilder()
                    .title("邀请您上麦，是否同意？")
                    .tv1("拒绝")
                    .tv2("同意")
                    .dismiss2(false)
                    .onCancel(new OutDialog.ClickLister() {
                        @Override
                        public void click() {
                            watchLive.replyInvitation(webinarInfo.webinar_id, 2, new RequestCallback() {
                                @Override
                                public void onSuccess() {
                                    showToast("拒绝上麦");
                                }

                                @Override
                                public void onError(int errorCode, String errorMsg) {
                                    showToast(errorMsg);
                                }
                            });
                        }
                    })
                    .onConfirm(new OutDialog.ClickLister() {
                        @Override
                        public void click() {
                            //有权限才可以上麦
                            if (RtcInternal.isGrantedPermissionRtc(MinimalistWatchActivity.this, REQUEST_PUSH)) {
                                watchLive.replyInvitation(webinarInfo.webinar_id, 1, new RequestCallback() {
                                    @Override
                                    public void onSuccess() {
                                        showInvited.dismiss();
                                        enterInteractive();
                                    }

                                    @Override
                                    public void onError(int errorCode, String errorMsg) {
                                        showInvited.dismiss();
                                        showToast(errorMsg);
                                    }
                                });
                            }
                        }
                    })
                    .build(MinimalistWatchActivity.this);
        }
        showInvited.show();
    }

    @Override
    public void enterInteractive() {
        isMic = true;
        //进入互动需要暂停直播
        watchMiniFragment.getWatchLive().stop();
        watchMiniFragment.setRootViewVisibility(View.GONE);
        if (rtcMiniFragment == null) {
            rtcMiniFragment = RtcMiniFragment.newInstance();
            rtcMiniFragment.init(webinarInfo, bigShowUserId);
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                rtcMiniFragment, R.id.contentVideo);
        if (micCount != null) {
            micCount.cancel();
        }
        tv_mic.setVisibility(View.GONE);
        iv_mic.setVisibility(View.VISIBLE);
        iv_mic.setBackgroundResource(R.mipmap.icon_mic_up);
        group_public.setVisibility(View.VISIBLE);

    }

    @Override
    public void leaveInteractive() {
        isMic = false;
        watchMiniFragment.setRootViewVisibility(View.VISIBLE);
        watchMiniFragment.getWatchLive().start();
        if (rtcMiniFragment != null) {
            ActivityUtils.remove(getSupportFragmentManager(),
                    rtcMiniFragment);
            rtcMiniFragment = null;
        }
        mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_open));
        hasVideoOpen = false;
        mSwitchAudio.setImageDrawable(getResources().getDrawable(R.drawable.icon_audio_open));
        hasAudioOpen = false;
        iv_mic.setBackgroundResource(R.mipmap.icon_mic_down);
        group_public.setVisibility(View.GONE);
        if ("0".equals(hands_up)) {
            iv_mic.setVisibility(View.GONE);
        }
    }

    @Override
    public void switchDevice(int device, int type) {
        if (rtcMiniFragment != null) {
            rtcMiniFragment.getInteractive().switchDevice(device, type, new RequestCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }
    }

    @Override
    public void updateVideoFrame(int status) {
        if (status == 1) {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_open));
            hasVideoOpen = false;
        } else {
            mSwitchVideo.setImageDrawable(getResources().getDrawable(R.drawable.icon_video_close));
            hasVideoOpen = true;
        }

    }

    @Override
    public void updateAudioFrame(int status) {
        if (status == 1) {
            mSwitchAudio.setImageDrawable(getResources().getDrawable(R.drawable.icon_audio_open));
            hasAudioOpen = false;
        } else {
            mSwitchAudio.setImageDrawable(getResources().getDrawable(R.drawable.icon_audio_close));
            hasAudioOpen = true;
        }
    }

    public void switchVideoFrame(int status) {
        if (rtcMiniFragment != null && rtcMiniFragment.getInteractive() != null)
            if (status == CAMERA_DEVICE_OPEN) { //1打开
                rtcMiniFragment.getInteractive().getLocalStream().unmuteVideo(null);
            } else { // 0禁止
                rtcMiniFragment.getInteractive().getLocalStream().muteVideo(null);
            }
    }

    public void switchAudioFrame(int status) {
        if (rtcMiniFragment != null && rtcMiniFragment.getInteractive() != null)
            if (status == CAMERA_DEVICE_OPEN) {
                rtcMiniFragment.getInteractive().getLocalStream().unmuteAudio(null);
            } else {
                rtcMiniFragment.getInteractive().getLocalStream().muteAudio(null);
            }
    }

    @Override
    public void onClick(View v) {
        if (v == iv_like) {
            clickLikeNumber++;
            likeNum++;
            pressLikeView.show(Math.max(2, random.nextInt(5)));
            likeNumTimer.cancel();
            likeNumTimer.start();
            tv_like_num.setText(String.valueOf(likeNum));
        } else if (v == tv_chat) {
            if (inputView != null) {
                inputView.show(false, null);
            }
        } else if (v == iv_gift) {
            if (webinarInfo != null) {
                GiftListDialog giftListDialog = new GiftListDialog(this, webinarInfo.vss_room_id);
                giftListDialog.show();
            }
        } else if (v == iv_mic) {
            if (isMic)
                rtcMiniFragment.getInteractive().unpublish(new RequestCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast(errorMsg);
                    }
                });
            else
                watchMiniFragment.getWatchLive().onRaiseHand(webinarInfo.webinar_id, 1, new RequestCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("举手成功");
                        startMicCount();
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        showToast(errorMsg);
                    }
                });
        } else if (v == tv_mic) {
            watchMiniFragment.getWatchLive().onRaiseHand(webinarInfo.webinar_id, 0, new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("取消举手成功");
                    if (micCount != null) {
                        micCount.cancel();
                    }
                    tv_mic.setVisibility(View.GONE);
                    if ("0".equals(hands_up)) {
                        iv_mic.setVisibility(View.GONE);
                    } else {
                        iv_mic.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v == mSwitchVideo) {
            switchDevice(CAMERA_VIDEO, hasVideoOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE);
        } else if (v == mSwitchAudio) {
            switchDevice(CAMERA_AUDIO, hasAudioOpen ? CAMERA_DEVICE_OPEN : CAMERA_DEVICE_CLOSE);
        } else if (v == mSwitchCamera) {
            if (rtcMiniFragment != null) {
                rtcMiniFragment.getInteractive().switchCamera();
            }
        } else if (v == iv_close) {
            finish();
        }
    }
}