package com.vhall.live.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vhall.live.R;
import com.vhall.live.data.Param;


/**
 * 主界面的Fragment
 */
public class SettingFragment extends Fragment implements MainContract.SetView, View.OnClickListener {

    private MainContract.Presenter mPresenter;
    private EditText mId, mToken, mVideoBitrate, mFrameRate, mBufferTime, mK ,mRecord;
    private RadioGroup mType;
    private RadioButton mType_hdpi, mType_xhdpi;
    private Button btn_close;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        mPresenter.start();
    }

    private void initView() {
        mId = (EditText) getView().findViewById(R.id.et_id);
        mToken = (EditText) getView().findViewById(R.id.et_token);
        mVideoBitrate = (EditText) getView().findViewById(R.id.et_bitrate);
        mFrameRate = (EditText) getView().findViewById(R.id.et_frame_rate);
        mBufferTime = (EditText) getView().findViewById(R.id.et_buffertime);
        mK = (EditText) getView().findViewById(R.id.et_k);
        mType = (RadioGroup) getView().findViewById(R.id.rg_type);
        mType_hdpi = (RadioButton) getView().findViewById(R.id.rb_hdpi);
        mType_xhdpi = (RadioButton) getView().findViewById(R.id.rb_xhdpi);
        btn_close = (Button) getView().findViewById(R.id.btn_close);
        mRecord = (EditText) getView().findViewById(R.id.recoid_id);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.showSetting(false);
                View view = getActivity().getWindow().peekDecorView();//隐藏软键盘
                if (view != null) {
                    InputMethodManager inputmanger = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public String getID() {
        return mId.getText().toString();
    }

    @Override
    public String getToken() {
        return mToken.getText().toString();
    }

    @Override
    public int getvideoBitrate() {
        String codeRate = mVideoBitrate.getText().toString();
        int code = 0;
        try {
            code = Integer.parseInt(codeRate);
            code = code * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return code;
    }

    @Override
    public int getFrameRate() {
        String frameRate = mFrameRate.getText().toString();
        int frame = 0;
        try {
            frame = Integer.parseInt(frameRate);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return frame;
    }

    @Override
    public int getBufferSecond() {
        String bufferSecond = mBufferTime.getText().toString();
        int buffer = 0;
        try {
            buffer = Integer.parseInt(bufferSecond);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    @Override
    public String getK() {
        return mK.getText().toString();
    }

    @Override
    public int getdpi() {
        if (mType_hdpi.isChecked()) {
            return Param.HDPI;
        } else if (mType_xhdpi.isChecked()) {
            return Param.XHDPI;
        } else {
            return Param.HDPI;
        }
    }

    @Override
    public String getRecord() {
        return mRecord.getText().toString();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
            this.mPresenter = presenter;
    }
}
