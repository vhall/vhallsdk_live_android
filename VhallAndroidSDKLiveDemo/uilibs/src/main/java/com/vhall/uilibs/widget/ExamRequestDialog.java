package com.vhall.uilibs.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vhall.business.data.RequestCallback;
import com.vhall.business.data.RequestDataCallbackV2;
import com.vhall.business.data.WebinarInfo;
import com.vhall.business.module.exam.ExamServer;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.base.BaseBottomDialog;
import com.vhall.vhss.data.ExamAnswerPaperHistoryData;
import com.vhall.vhss.data.ExamInfoData;
import com.vhall.vhss.data.ExamListData;
import com.vhall.vhss.data.ExamRankListData;
import com.vhall.vhss.data.ExamScoreInfoData;
import com.vhall.vhss.data.ExamUserFormCheckData;
import com.vhall.vhss.data.ExamUserFormData;

/**
 * @author hkl
 * 礼物列表
 */

public class ExamRequestDialog extends BaseBottomDialog implements View.OnClickListener {

    private ExamServer examServer;
    private EditText et_code;
    private EditText tv_raw;
    private  WebinarInfo webinarInfo;

    public ExamRequestDialog(Context context, WebinarInfo webinarInfo) {
        super(context);
        this.webinarInfo=webinarInfo;
        examServer = new ExamServer.Builder()
                .webinarInfo(webinarInfo)
                .context(context)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_exam_request);
        findViewById(R.id.tv_check).setOnClickListener(this);
        findViewById(R.id.tv_init).setOnClickListener(this);
        findViewById(R.id.tv_send_code).setOnClickListener(this);
        findViewById(R.id.tv_verify_code).setOnClickListener(this);
        findViewById(R.id.tv_save_form).setOnClickListener(this);
        findViewById(R.id.tv_exam_list).setOnClickListener(this);
        findViewById(R.id.tv_exam_info).setOnClickListener(this);
        findViewById(R.id.tv_exam_answer_question).setOnClickListener(this);
        findViewById(R.id.tv_answer_paper_history).setOnClickListener(this);
        findViewById(R.id.tv_exam_submit_paper).setOnClickListener(this);
        findViewById(R.id.tv_exam_rank_list).setOnClickListener(this);
        findViewById(R.id.tv_exam_person_score_info).setOnClickListener(this);
        et_code = findViewById(R.id.et_code);
        et_code.clearFocus();
        et_code.setEnabled(false);
        tv_raw = findViewById(R.id.tv_raw);
    }

    String userDetail;

    private void setResult(String result) {
        tv_raw.setText(result);
    }

    @Override
    public void onClick(View v) {
        String paper_id = "96";
        if (v.getId() == R.id.tv_check) {
            examServer.examUserFormCheck("", "", "", new RequestDataCallbackV2<ExamUserFormCheckData>() {
                @Override
                public void onSuccess(ExamUserFormCheckData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_init) {
            examServer.examGetUserFormInfo(paper_id, new RequestDataCallbackV2<ExamUserFormData>() {
                @Override
                public void onSuccess(ExamUserFormData data) {
                    userDetail = data.form_data;
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_send_code) {
            examServer.examSendVerifyCode("手机号", "", new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_verify_code) {
            examServer.examVerifyCode(paper_id, "手机号", et_code.getText().toString(), "", new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_save_form) {
            examServer.examSaveUserForm(userDetail, paper_id,"", new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_exam_list) {
            examServer.examGetPushedPaperList(new RequestDataCallbackV2<ExamListData>() {
                @Override
                public void onSuccess(ExamListData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
            new ExamListDialog(mContext,webinarInfo).show();

        } else if (v.getId() == R.id.tv_exam_info) {
            examServer.examGetPaperInfoForWatch(paper_id, new RequestDataCallbackV2<ExamInfoData>() {
                @Override
                public void onSuccess(ExamInfoData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_exam_answer_question) {
            examServer.examAnswerQuestion(paper_id, "35942ww6", "554471", new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_answer_paper_history) {
            examServer.examGetUserAnswerPaperHistory(paper_id, new RequestDataCallbackV2<ExamAnswerPaperHistoryData>() {
                @Override
                public void onSuccess(ExamAnswerPaperHistoryData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_exam_submit_paper) {
            examServer.examInitiativeSubmitPaper(paper_id, new RequestCallback() {
                @Override
                public void onSuccess() {
                    showToast("onSuccess");
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else if (v.getId() == R.id.tv_exam_rank_list) {
            examServer.examGetSimpleRankList(paper_id,"0", new RequestDataCallbackV2<ExamRankListData>() {
                @Override
                public void onSuccess(ExamRankListData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }else if (v.getId() == R.id.tv_exam_person_score_info) {
            examServer.examPersonScoreInfo(paper_id, new RequestDataCallbackV2<ExamScoreInfoData>() {
                @Override
                public void onSuccess(ExamScoreInfoData data) {
                    setResult(data.raw);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }
    }

}