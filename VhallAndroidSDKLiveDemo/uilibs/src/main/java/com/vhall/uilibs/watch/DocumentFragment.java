package com.vhall.uilibs.watch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vhall.business.MessageServer;
import com.vhall.uilibs.BasePresenter;
import com.vhall.uilibs.R;
import com.vhall.business.widget.PPTView;
import com.vhall.business.widget.WhiteBoardView;

import java.util.List;

/**
 * 文档页的Fragment
 */
public class DocumentFragment extends Fragment implements WatchContract.DocumentView {
    private PPTView iv_doc;
    private WhiteBoardView board;
    private String url = "";

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iv_doc = (PPTView) getView().findViewById(R.id.iv_doc);
        board = (WhiteBoardView) getView().findViewById(R.id.board);
    }


//    @Override
//    public void showDoc(String docUrl) {
//        if (!url.equals(docUrl))
//            Glide.with(this).load(docUrl).into(iv_doc);
//    }

    @Override
    public void paintBoard(MessageServer.MsgInfo msgInfo) {
        board.setStep(msgInfo);
    }

    @Override
    public void paintBoard(String key, List<MessageServer.MsgInfo> msgInfos) {
        board.setSteps(key, msgInfos);
    }

    @Override
    public void paintPPT(MessageServer.MsgInfo msgInfo) {
        iv_doc.setStep(msgInfo);
    }

    @Override
    public void paintPPT(String key, List<MessageServer.MsgInfo> msgInfos) {
        iv_doc.setSteps(key, msgInfos);
    }

    @Override
    public void setPresenter(BasePresenter presenter) {

    }
}
