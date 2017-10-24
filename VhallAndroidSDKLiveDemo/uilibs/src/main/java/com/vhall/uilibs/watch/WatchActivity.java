package com.vhall.uilibs.watch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.vhall.business.MessageServer;
import com.vhall.business.data.Survey;
import com.vhall.business_support.dlna.DeviceDisplay;
import com.vhall.uilibs.util.ActivityUtils;
import com.vhall.uilibs.Param;
import com.vhall.uilibs.R;
import com.vhall.uilibs.util.DevicePopu;
import com.vhall.uilibs.util.ShowLotteryDialog;
import com.vhall.uilibs.util.SignInDialog;
import com.vhall.uilibs.util.SurveyPopu;
import com.vhall.uilibs.util.VhallUtil;
import com.vhall.uilibs.chat.ChatFragment;
import com.vhall.uilibs.util.ExtendTextView;
import com.vhall.uilibs.util.emoji.InputUser;
import com.vhall.uilibs.util.emoji.InputView;
import com.vhall.uilibs.util.emoji.KeyBoardManager;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * 观看页的Activity
 */
public class WatchActivity extends FragmentActivity implements WatchContract.WatchView {

    private FrameLayout contentDoc, contentDetail, contentChat, contentQuestion;
    private RadioGroup radio_tabs;
    private RadioButton questionBtn, chatBtn;
    private LinearLayout ll_detail;
    ExtendTextView tv_notice;
    private Param param;
    private int type;

    public WatchPlaybackFragment playbackFragment;
    public WatchLiveFragment liveFragment;
    public ChatFragment chatFragment;
    public ChatFragment questionFragment;


    InputView inputView;
    public int chatEvent = ChatFragment.CHAT_EVENT_CHAT;
    private SurveyPopu popu;
    private SignInDialog signInDialog;
    private ShowLotteryDialog lotteryDialog;
    WatchContract.WatchPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.watch_activity);
        param = (Param) getIntent().getSerializableExtra("param");
        type = getIntent().getIntExtra("type", VhallUtil.WATCH_LIVE);
        liveFragment = (WatchLiveFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        playbackFragment = (WatchPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentChat);
        DocumentFragment docFragment = (DocumentFragment) getSupportFragmentManager().findFragmentById(R.id.contentDoc);
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentDetail);
        questionFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentQuestion);
        initView();
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance(type, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.contentChat);
        }
        if (docFragment == null) {
            docFragment = DocumentFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    docFragment, R.id.contentDoc);
        }

        if (questionFragment == null && type == VhallUtil.WATCH_LIVE) {
            questionFragment = ChatFragment.newInstance(type, true);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    questionFragment, R.id.contentQuestion);
        }

        if (detailFragment == null) {
            detailFragment = DetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    detailFragment, R.id.contentDetail);
        }

        if (liveFragment == null && type == VhallUtil.WATCH_LIVE) {
            liveFragment = WatchLiveFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    liveFragment, R.id.contentVideo);
            new WatchLivePresenter(liveFragment, docFragment, chatFragment, questionFragment, this, param);
        }

        if (playbackFragment == null && type == VhallUtil.WATCH_PLAYBACK) {
            playbackFragment = WatchPlaybackFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    playbackFragment, R.id.contentVideo);
            new WatchPlaybackPresenter(playbackFragment, docFragment, chatFragment, this, param);
        }

        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new FixedAndroidLogHandler()
        );
        this.bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && inputView.getContentView().getVisibility() == View.VISIBLE) {
            boolean isDismiss = isShouldHideInput(inputView.getContentView(), ev);
            if (isDismiss) {
                inputView.dismiss();
                return false;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isShouldHideInput(View view, MotionEvent event) {
        if (view.getVisibility() == View.GONE)
            return false;
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        inputView.getContentView().getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + inputView.getContentView().getHeight();
        int right = left + inputView.getContentView().getWidth();
        return !(event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom);
    }

    private void initView() {
        inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
        inputView.add2Window(this);
        inputView.setClickCallback(new InputView.ClickCallback() {
            @Override
            public void onEmojiClick() {

            }
        });
        inputView.setOnSendClickListener(new InputView.SendMsgClickListener() {
            @Override
            public void onSendClick(String msg, InputUser user) {
                if (chatFragment != null && chatEvent == ChatFragment.CHAT_EVENT_CHAT) {
                    chatFragment.performSend(msg, chatEvent);
                } else if (questionFragment != null && chatEvent == ChatFragment.CHAT_EVENT_QUESTION) {
                    questionFragment.performSend(msg, chatEvent);
                }
            }
        });
        inputView.setOnHeightReceivedListener(new InputView.KeyboardHeightListener() {
            @Override
            public void onHeightReceived(int screenOri, int height) {
                if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    KeyBoardManager.setKeyboardHeight(WatchActivity.this, height);
                } else {
                    KeyBoardManager.setKeyboardHeightLandspace(WatchActivity.this, height);
                }
            }
        });
        ll_detail = (LinearLayout) this.findViewById(R.id.ll_detail);
        contentDoc = (FrameLayout) findViewById(R.id.contentDoc);
        contentDetail = (FrameLayout) findViewById(R.id.contentDetail);
        contentChat = (FrameLayout) findViewById(R.id.contentChat);
        contentQuestion = (FrameLayout) findViewById(R.id.contentQuestion);

        questionBtn = (RadioButton) this.findViewById(R.id.rb_question);
        chatBtn = (RadioButton) this.findViewById(R.id.rb_chat);

        tv_notice = (ExtendTextView) this.findViewById(R.id.tv_notice);
        tv_notice.setDrawableClickListener(new ExtendTextView.DrawableClickListener() {
            @Override
            public void onDrawableClick(int position) {
                switch (position) {
                    case ExtendTextView.DRAWABLE_RIGHT:
                        dismissNotice();
                        break;
                }
            }
        });
        if (type == VhallUtil.WATCH_LIVE) {
            questionBtn.setVisibility(View.VISIBLE);
            contentChat.setVisibility(View.VISIBLE);
            chatBtn.setText("聊天");
        }
        if (type == VhallUtil.WATCH_PLAYBACK) {
            chatBtn.setText("评论");
            contentChat.setVisibility(View.VISIBLE);
        }
        radio_tabs = (RadioGroup) findViewById(R.id.radio_tabs);
        radio_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_chat) {
                    chatEvent = ChatFragment.CHAT_EVENT_CHAT;
                    contentChat.setVisibility(View.VISIBLE);
                    contentDoc.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_doc) {
                    contentDoc.setVisibility(View.VISIBLE);
                    contentChat.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_question) {
                    chatEvent = ChatFragment.CHAT_EVENT_QUESTION;
                    contentDoc.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.VISIBLE);
                    contentChat.setVisibility(View.GONE);
                } else if (checkedId == R.id.rb_detail) {
                    contentDoc.setVisibility(View.GONE);
                    contentChat.setVisibility(View.GONE);
                    contentQuestion.setVisibility(View.GONE);
                    contentDetail.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void showChatView(boolean isShowEmoji, InputUser user, int contentLengthLimit) {
        if (contentLengthLimit > 0)
            inputView.setLimitNo(contentLengthLimit);
        inputView.show(isShowEmoji, user);
    }

    @Override
    public void showSignIn(String signId, int startTime) {
        if (signInDialog == null) {
            signInDialog = new SignInDialog(this);
        }
        signInDialog.setSignInId(signId);
        signInDialog.setCountDownTime(startTime);
        signInDialog.setOnSignInClickListener(new SignInDialog.OnSignInClickListener() {
            @Override
            public void signIn(String signId) {
                mPresenter.signIn(signId);
            }
        });
        signInDialog.show();
    }

    @Override
    public void dismissSignIn() {
        if (signInDialog != null)
            signInDialog.dismiss();
    }

    @Override
    public void showSurvey(Survey survey) {
        if (popu == null) {
            popu = new SurveyPopu(this);
            popu.setOnSubmitClickListener(new SurveyPopu.OnSubmitClickListener() {
                @Override
                public void onSubmitClick(Survey survey1, String result) {
                    mPresenter.submitSurvey(survey1, result);
                }
            });
        }
        popu.setSurvey(survey);
        popu.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public void dismissSurvey() {
        if (popu != null)
            popu.dismiss();
    }

    @Override
    public int changeOrientation() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ll_detail.setVisibility(View.GONE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ll_detail.setVisibility(View.VISIBLE);
        }
        return getRequestedOrientation();
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(int toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showLottery(MessageServer.MsgInfo messageInfo) {
        if (lotteryDialog == null) {
            lotteryDialog = new ShowLotteryDialog(this);
        }
        lotteryDialog.setMessageInfo(messageInfo);
        lotteryDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        lotteryDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        lotteryDialog.show();
    }

    @Override
    public void showDevices() {
        if (devicePopu == null) {
            devicePopu = new DevicePopu(this);
            devicePopu.setOnItemClickListener(new OnItemClick());
        }
        devicePopu.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismissDevices() {
        if (devicePopu != null)
            devicePopu.dismiss();
    }

    @Override
    public void showNotice(String content) {
        if (TextUtils.isEmpty(content))
            return;
        tv_notice.setText(content);
        tv_notice.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissNotice() {
        tv_notice.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        inputView.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            changeOrientation();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        if (null != inputView) {
            inputView.dismiss();
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        this.unbindService(serviceConnection);
    }

    @Override
    public void setPresenter(WatchContract.WatchPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //------------------------------------------------------投屏相关--------------------------------------------------
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    private DevicePopu devicePopu;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("Service ", "mUpnpServiceConnection onServiceConnected");
            upnpService = (AndroidUpnpService) service;
            // Clear the list
            if (devicePopu != null)
                devicePopu.clear();
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);
            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search(); // 搜索设备
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
//            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(
//                        BrowserActivity2.this,
//                        "Discovery failed of '" + device.getDisplayString() + "': "
//                            + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
//                        Toast.LENGTH_LONG
//                    ).show();
//                }
//            });
//            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            if (device.getType().getNamespace().equals("schemas-upnp-org") && device.getType().getType().equals("MediaRenderer")) {
                deviceAdded(device);
            }

        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
//            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
//            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(WatchActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceAdded(device);
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (devicePopu == null) {
                        devicePopu = new DevicePopu(WatchActivity.this);
                        devicePopu.setOnItemClickListener(new OnItemClick());
                    }
                    devicePopu.deviceRemoved(device);
                }
            });
        }
    }

    class OnItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);
            mPresenter.dlnaPost(deviceDisplay, upnpService);
            dismissDevices();
        }
    }

    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    /**
     * 获取支持投屏的设备
     *
     * @return  设备列表
     */
    @Nullable
    public Collection<Device> getDmrDevices() {
        if (upnpService == null)
            return null;
        Collection<Device> devices = upnpService.getRegistry().getDevices(DMR_DEVICE_TYPE);
        return devices;
    }


}
