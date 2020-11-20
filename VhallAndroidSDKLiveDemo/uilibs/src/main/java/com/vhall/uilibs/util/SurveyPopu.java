package com.vhall.uilibs.util;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.business.data.Survey;
import com.vhall.uilibs.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;


/**
 * Created by huanan on 2017/3/6.
 * 旧版问卷
 */
public class SurveyPopu extends PopupWindow {

    ImageView iv_close;
    TextView tv_title;
    LinearLayout ll_content;
    Context context;
    Survey survey;
    OnSubmitClickListener onSubmitClickListener;

    public void setOnSubmitClickListener(OnSubmitClickListener onSubmitClickListener) {
        this.onSubmitClickListener = onSubmitClickListener;
    }

    public SurveyPopu(Context context) {
        super(context);
        this.context = context;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.survey_layout, null);
        setContentView(root);
        iv_close = (ImageView) root.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tv_title = (TextView) root.findViewById(R.id.tv_title);
        ll_content = (LinearLayout) root.findViewById(R.id.ll_content);
        ll_content.setVisibility(View.VISIBLE);
    }

    public void setSurvey(final Survey survey) {
        ll_content.removeAllViews();
        this.survey = survey;
        tv_title.setText(this.survey.subject);
        initPage();
    }

    private void initPage() {
        if (survey.questions != null && survey.questions.size() > 0) {
            Collections.sort(survey.questions, survey);
            for (int i = 0; i < survey.questions.size(); i++) {
                final Survey.Question question = survey.questions.get(i);

                if (question != null) {
                    TextView textView = new TextView(context);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                    textView.setText((i + 1) + "、" + question.subject);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(20);
                    if (question.must == 1) {
                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vhall_icon_redpoint);
                        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        textView.setCompoundDrawables(null, null, drawable, null);
                        textView.setCompoundDrawablePadding(10);
                    }
                    ll_content.addView(textView);
                    switch (question.type) {
                        case 0:
                            EditText editText = new EditText(context);
                            editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                            editText.setTextColor(Color.BLACK);
                            editText.setTextSize(20);
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if (question.answer.isEmpty()) {
                                        question.answer.add(s.toString());
                                    } else {
                                        question.answer.set(0, s.toString());
                                    }
                                }
                            });
                            ll_content.addView(editText);
                            break;
                        case 1:
                            if (question.options != null && question.options.size() > 0) {
                                RadioGroup radioGroup = new RadioGroup(context);
                                radioGroup.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                                radioGroup.setOrientation(LinearLayout.VERTICAL);
                                for (int j = 0; j < question.options.size(); j++) {
                                    RadioButton radioButton = new RadioButton(context);
                                    radioButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                                    radioButton.setText(question.options.get(j));
                                    radioGroup.addView(radioButton);
                                }
                                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                                        RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                                        if (question.answer.isEmpty()) {
                                            question.answer.add(radioButton.getText().toString());
                                        } else {
                                            question.answer.set(0, radioButton.getText().toString());
                                        }
                                    }
                                });
                                ll_content.addView(radioGroup);
                            }
                            break;
                        case 2:
                            if (question.options != null && question.options.size() > 0) {

                                for (int j = 0; j < question.options.size(); j++) {
                                    final CheckBox checkBox = new CheckBox(context);
                                    checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                                    checkBox.setText(question.options.get(j));
                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            String opt = checkBox.getText().toString();
                                            if (isChecked && !question.answer.contains(opt)) {
                                                question.answer.add(opt);
                                                return;
                                            }
                                            if (!isChecked && question.answer.contains(opt)) {
                                                question.answer.remove(opt);
                                                return;
                                            }
                                        }
                                    });
                                    ll_content.addView(checkBox);
                                }
                            }
                            break;
                        default:
                            break;
                    }

                }
            }

            final TextView errorView = new TextView(context);
            errorView.setTextColor(Color.RED);
            errorView.setVisibility(View.GONE);
            ll_content.addView(errorView);

            Button button = new Button(context);
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            button.setText("提交");
            button.setTextColor(Color.WHITE);
            button.setTextSize(20);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (survey.questions != null && survey.questions.size() > 0) {

                        try {
                            boolean isAvailable = true;
                            boolean isEmpty = true;
                            JSONArray jsonArray = new JSONArray();
//                            String list = "";
                            for (int i = 0; i < survey.questions.size(); i++) {
                                Survey.Question question = survey.questions.get(i);
                                int size = question.answer.size();
                                if (question.must == 1 && size == 0) {
                                    errorView.setText("第" + (i + 1) + "题为必填题，请填写（输入框）后再提交！");
                                    errorView.setVisibility(View.VISIBLE);
                                    isAvailable = false;
                                    break;
                                }
                                JSONObject obj = new JSONObject();
                                obj.put("ques_id", question.ques_id);
                                switch (size) {
                                    case 0:
                                        obj.put("answer", "");
                                        break;
                                    case 1:
                                        isEmpty = false;
                                        obj.put("answer", question.answer.get(0));
                                        break;
                                    default:
                                        isEmpty = false;
                                        String answerStr = "";
                                        for (int j = 0; j < size; j++) {
                                            answerStr = answerStr + question.answer.get(j) + "|";
                                        }
                                        obj.put("answer", answerStr);
                                        break;
                                }
                                jsonArray.put(obj);
                            }
                            if (!isAvailable) {
                                return;
                            }
                            if (isEmpty) {
                                Toast.makeText(context, "提交成功！", Toast.LENGTH_SHORT).show();
                                SurveyPopu.this.dismiss();
                                return;
                            }
                            if (onSubmitClickListener != null && isAvailable) {
                                onSubmitClickListener.onSubmitClick(survey, jsonArray.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ll_content.addView(button);
        }
    }

    public interface OnSubmitClickListener {
        void onSubmitClick(Survey survey, String result);
    }
}
