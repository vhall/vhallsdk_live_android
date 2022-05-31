package com.vhall.uilibs.interactive.broadcast;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseFragment;
import com.vhall.uilibs.interactive.doc.DocumentPublicView;
import com.vhall.uilibs.interactive.doc.IDocViewLister;
import com.vhall.vhss.data.WebinarInfoData;


/**
 * @author hkl
 */
public class DocFragment extends BaseFragment {

    public static DocFragment getInstance(String broadcastType, String orientation) {
        DocFragment fragment = new DocFragment();
        Bundle bundle = new Bundle();
        bundle.putString("broadcastType", broadcastType);
        bundle.putString("orientation", orientation);
        fragment.setArguments(bundle);
        return fragment;
    }

    private View rootView;
    private DocumentPublicView documentPublicView;
    private boolean showEdit = false;
    private String mainId;
    private TextView tvEmpty;
    private WebinarInfoData webinarInfoData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_doc, container, false);
        documentPublicView = rootView.findViewById(R.id.rl_doc_view);
        String orientation = getArguments().getString("orientation");
        documentPublicView.setOrientation(orientation);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        documentPublicView.setDocViewLister(new IDocViewLister() {
            @Override
            public void onError(int error, String msg) {

            }

            @Override
            public void setVisibility(int visibility, int rootVisibility) {
                tvEmpty.setVisibility(visibility);
                documentPublicView.setVisibility(rootVisibility);
            }
        });
        if (webinarInfoData!=null){
            setRoomInfo(webinarInfoData);
        }
        return rootView;
    }

    public void setRoomInfo(WebinarInfoData webinarInfoData) {
        this.webinarInfoData=webinarInfoData;
        if (documentPublicView==null){
            return;
        }
        if (!TextUtils.isEmpty(webinarInfoData.join_info.third_party_user_id)) {
            documentPublicView.setOwnerId(webinarInfoData.join_info.third_party_user_id);
            if ( webinarInfoData.roomToolsStatusData!=null&&!TextUtils.isEmpty(webinarInfoData.roomToolsStatusData.doc_permission)) {
                setMainId(webinarInfoData.roomToolsStatusData.doc_permission, webinarInfoData.join_info.third_party_user_id);
            } else {
                setMainId(webinarInfoData.webinar.userinfo.user_id, webinarInfoData.join_info.third_party_user_id);
            }
            documentPublicView.init(webinarInfoData.interact.channel_id, webinarInfoData.interact.room_id, webinarInfoData.interact.paas_access_token, !TextUtils.equals(webinarInfoData.join_info.third_party_user_id, mainId));
        } else {
            baseShowToast("doc error");
        }
    }

    public void setMainId(String mainId, String ownerId) {
        this.mainId = mainId;
        if (documentPublicView != null) {
            documentPublicView.setMainId(mainId);
        }
        if (TextUtils.equals(ownerId, mainId)) {
            tvEmpty.setText("还没有文档哦，点击右下角添加～");
        } else {
            tvEmpty.setText("还没有文档哦");
        }
    }

    public void setDocId(String docId) {
        documentPublicView.showDoc(docId);
    }

    public void showPop(View view) {
        if (showEdit) {
            documentPublicView.hintPop();
        } else {
            documentPublicView.showPop(view);
        }
        showEdit = !showEdit;
    }

    @Override
    public void onPause() {
        super.onPause();
        hintDoc();
    }

    public void hintDoc() {
        if (documentPublicView != null) {
            documentPublicView.hintPop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (documentPublicView!=null)
        documentPublicView.destroy();
    }

}
