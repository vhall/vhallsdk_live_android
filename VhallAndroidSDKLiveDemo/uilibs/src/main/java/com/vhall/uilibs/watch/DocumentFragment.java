package com.vhall.uilibs.watch;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.vhall.business.MessageServer;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;
import com.vhall.business.widget.PPTView;
import com.vhall.business.widget.WhiteBoardView;
import com.vhall.uilibs.util.DocTouchListener;

import java.util.List;

/**
 * 文档页的Fragment
 */
public class DocumentFragment extends Fragment implements WatchContract.DocumentView {
    private PPTView iv_doc;
    private WhiteBoardView board;
    private int showType = 0;
    private String url = "";
    private FrameLayout h5FrameLayout;
    private ImageView doc_type_full, doc_ori_land, doc_action_resume;
    private WatchContract.WatchPresenter mPresenter;

    public static DocumentFragment newInstance() {
        DocumentFragment articleFragment = new DocumentFragment();
        return articleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document_fragment, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iv_doc = (PPTView) getView().findViewById(R.id.iv_doc);
        board = (WhiteBoardView) getView().findViewById(R.id.board);
        h5FrameLayout = (FrameLayout) getView().findViewById(R.id.fl_h5_doc);
        doc_type_full = getView().findViewById(R.id.doc_type_full);
        doc_ori_land = getView().findViewById(R.id.doc_ori_land);
        doc_action_resume = getView().findViewById(R.id.doc_action_resume);
    }

    @Override
    public void onResume() {
        super.onResume();
        showType(showType);
    }


    @Override
    public void paintBoard(MessageServer.MsgInfo msgInfo) {
        if (board != null) {
            board.setStep(msgInfo);
        }
    }

    @Override
    public void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos) {
        if (board != null) {
            board.setSteps(key, msgInfos);
        }
    }

    @Override
    public void paintPPT(MessageServer.MsgInfo msgInfo) {
        if (board != null) {
            iv_doc.setStep(msgInfo);
        }
    }

    @Override
    public void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos) {
        if (iv_doc != null) {
            iv_doc.setSteps(key, msgInfos);
        }
    }


    @Override
    public void showType(int type) {
        if (iv_doc == null) {
            showType = type;
            return;
        }
        switch (type) {
            case 0://文档
                iv_doc.setVisibility(View.VISIBLE);
                board.setVisibility(View.GONE);
                board.setShowDoc(true);
                doc_type_full.setVisibility(View.GONE);
                doc_ori_land.setVisibility(View.GONE);
                doc_action_resume.setVisibility(View.GONE);
                break;
            case 1://白板
                iv_doc.setVisibility(View.VISIBLE);
                board.setVisibility(View.VISIBLE);
                board.setShowDoc(true);
                doc_type_full.setVisibility(View.GONE);
                doc_ori_land.setVisibility(View.GONE);
                doc_action_resume.setVisibility(View.GONE);
                break;
            case 2://关闭文档
                iv_doc.setVisibility(View.GONE);
                board.setVisibility(View.GONE);
                board.setShowDoc(false);
                h5FrameLayout.setVisibility(View.GONE);
                doc_type_full.setVisibility(View.GONE);
                doc_ori_land.setVisibility(View.GONE);
                doc_action_resume.setVisibility(View.GONE);
                break;
            case 3://打开h5文档
                iv_doc.setVisibility(View.GONE);
                board.setVisibility(View.GONE);
                h5FrameLayout.setVisibility(View.VISIBLE);
                doc_type_full.setVisibility(View.VISIBLE);
                doc_ori_land.setVisibility(View.GONE);
                doc_action_resume.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private DocTouchListener mDocTouchListener;
    @Override
    public void paintH5DocView(View docView) {
        mDocTouchListener = new DocTouchListener();
        docView.setOnTouchListener(mDocTouchListener);
        h5FrameLayout.removeAllViews();
        FrameLayout.LayoutParams params =  new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        h5FrameLayout.addView(docView, params);
        doc_type_full.setOnClickListener(v ->
                toggleFullScreenState(doc_type_full, DocFragState.toggleIfFullState(getIntegerViewTag(doc_type_full)))
        );
        doc_ori_land.setOnClickListener(v ->
                toggleOrientationState(doc_ori_land, DocFragState.toggleOriState(getIntegerViewTag(doc_ori_land)))
        );
        doc_action_resume.setOnClickListener(v -> {
            if (null != mDocTouchListener && null != docView) {
                mDocTouchListener.setScale(docView, 1);
            }
        });
    }

    @Override
    public void triggerDocOrientation() {
        toggleOrientationState(doc_ori_land, DocFragState.toggleOriState(getIntegerViewTag(doc_ori_land)));
    }

    @Override
    public void clickDocFullBack() {
        if (DocFragState.isStateHorizontal(getIntegerViewTag(doc_ori_land))) {
            toggleOrientationState(doc_ori_land, DocFragState.toggleOriState(getIntegerViewTag(doc_ori_land)));
        }
        toggleFullScreenState(doc_type_full, DocFragState.STATE_VER_NONFULL);
    }

    private void toggleFullScreenState(View tagView, int newState){
        refreshLandBtn(newState);
        tagView.setTag(newState);
        if (null != mPresenter) {
            mPresenter.showDocFullScreen(newState);
        }
    }

    private void toggleOrientationState(View tagView, int newState){
        tagView.setTag(newState);
        if (null != mPresenter) {
            mPresenter.changeDocOrientation();
        }
    }

    private void refreshLandBtn(int newState) {
        doc_ori_land.setVisibility((DocFragState.STATE_VER_NONFULL == newState) ? View.GONE : View.VISIBLE);
    }

    private int getIntegerViewTag(View target) {
        Object tagObj = target.getTag();
        if (null != tagObj) {
            int tag = 0;
            try {
                tag = (int) tagObj;
            } catch (Exception e) {
                tag = DocFragState.STATE_NONE;
                e.printStackTrace();
            }
            return tag;
        } else {
            return DocFragState.STATE_NONE;
        }
    }

    @Override
    public void setPresenter(BasePresenter presenter) {
        if (null != presenter && presenter instanceof WatchContract.WatchPresenter) {
            mPresenter = (WatchContract.WatchPresenter) presenter;
        }
    }
}
