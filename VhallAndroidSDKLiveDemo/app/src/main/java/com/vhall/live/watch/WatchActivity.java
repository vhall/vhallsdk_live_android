package com.vhall.live.watch;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vhall.live.BasePresenter;
import com.vhall.live.R;
import com.vhall.live.chat.ChatFragment;
import com.vhall.live.data.Param;
import com.vhall.live.utils.ActivityUtils;

/**
 * 观看页的Activity
 */
public class WatchActivity extends FragmentActivity implements WatchContract.WatchView {
    private FrameLayout contentDoc, contentDetail, contentChat, contentQuestion;
    private RadioGroup rg_tabs;
    private RadioButton chatRadioButton, questionRadioButton;
    private LinearLayout ll_detail;
    private Param param;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.watch_activity);
        param = (Param) getIntent().getSerializableExtra("param");
        initView();
        WatchLiveFragment liveFragment = (WatchLiveFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        WatchPlaybackFragment playbackFragment = (WatchPlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.contentVideo);
        DocumentFragment docFragment = (DocumentFragment) getSupportFragmentManager().findFragmentById(R.id.contentDoc);
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentDetail);
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentChat);
        ChatFragment questionFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.contentQuestion);

        if (docFragment == null) {
            docFragment = DocumentFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    docFragment, R.id.contentDoc);
        }
        if (chatFragment == null && param.watch_type == Param.WATCH_LIVE) {
            chatFragment = ChatFragment.newInstance(param.watch_type, false);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.contentChat);
        }
        if (questionFragment == null && param.watch_type == Param.WATCH_LIVE) {
            questionFragment = ChatFragment.newInstance(param.watch_type, true);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    questionFragment, R.id.contentQuestion);
        }
        if (detailFragment == null) {
            detailFragment = DetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    detailFragment, R.id.contentDetail);
        }

        if (liveFragment == null && param.watch_type == Param.WATCH_LIVE) {
            liveFragment = WatchLiveFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    liveFragment, R.id.contentVideo);
            new WatchLivePresenter(liveFragment, docFragment, chatFragment, questionFragment, this, param);
        }

        if (playbackFragment == null && param.watch_type == Param.WATCH_PLAYBACK) {
            playbackFragment = WatchPlaybackFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    playbackFragment, R.id.contentVideo);
            new WatchPlaybackPresenter(playbackFragment, docFragment, this, param);
        }

    }

    private void initView() {
        ll_detail = (LinearLayout) this.findViewById(R.id.ll_detail);
        contentDoc = (FrameLayout) findViewById(R.id.contentDoc);
        contentDetail = (FrameLayout) findViewById(R.id.contentDetail);
        contentChat = (FrameLayout) findViewById(R.id.contentChat);
        contentQuestion = (FrameLayout) findViewById(R.id.contentQuestion);
        chatRadioButton = (RadioButton) findViewById(R.id.rb_chat);
        questionRadioButton = (RadioButton) findViewById(R.id.rb_question);

        if (param.watch_type == Param.WATCH_LIVE) {
            chatRadioButton.setVisibility(View.VISIBLE);
            questionRadioButton.setVisibility(View.VISIBLE);
        }
        rg_tabs = (RadioGroup) findViewById(R.id.rg_tabs);
        rg_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_doc:
                        contentDoc.setVisibility(View.VISIBLE);
                        contentChat.setVisibility(View.GONE);
                        contentDetail.setVisibility(View.GONE);
                        contentQuestion.setVisibility(View.GONE);
                        break;
                    case R.id.rb_detail:
                        contentDoc.setVisibility(View.GONE);
                        contentChat.setVisibility(View.GONE);
                        contentQuestion.setVisibility(View.GONE);
                        contentDetail.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_chat:
                        contentDoc.setVisibility(View.GONE);
                        contentDetail.setVisibility(View.GONE);
                        contentQuestion.setVisibility(View.GONE);
                        contentChat.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_question:
                        contentDoc.setVisibility(View.GONE);
                        contentDetail.setVisibility(View.GONE);
                        contentQuestion.setVisibility(View.VISIBLE);
                        contentChat.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });


    }

    @Override
    public void setShowDetail(boolean isShow) {
        if (isShow) {
            ll_detail.setVisibility(View.VISIBLE);
        } else {
            ll_detail.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }
}
