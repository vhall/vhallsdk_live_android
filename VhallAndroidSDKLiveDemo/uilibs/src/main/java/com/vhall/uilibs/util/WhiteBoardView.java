package com.vhall.uilibs.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.vhall.business.MessageServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by huanan on 2017/3/8.
 * 白板 用于绘制直播端画笔数据
 */
public class WhiteBoardView extends View {

    private static final String TAG = "WhiteBoardView";

    int remoteWidth = 1024;
    int remoteHeight = 768;

    int boardWidth = 0;
    int boardHeight = 0;

    Paint mPaint = new Paint();

    List<MessageServer.MsgInfo> msgInfos = new ArrayList<MessageServer.MsgInfo>();//纪录所有步骤

    public WhiteBoardView(Context context) {
        super(context);
    }

    public WhiteBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WhiteBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boardWidth = this.getMeasuredWidth();
        boardHeight = this.getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw");
        canvas.drawColor(Color.WHITE);
        if (msgInfos.size() > 0) {
            for (int i = 0; i < msgInfos.size(); i++) {
                MessageServer.MsgInfo msgInfo = msgInfos.get(i);
                switch (msgInfo.event) {
                    case MessageServer.EVENT_PAINTBOARD:
                        Log.e(TAG, "EVENT:EVENT_PAINTBOARD");
                        paint(msgInfo, canvas);
                        break;
                }
            }
        }
    }

    private void paint(MessageServer.MsgInfo msgInfo, Canvas canvas) {//1 表示白板画笔 4 表示白板文字 7表示白板锚点 20表示圆形 22表示矩形

        if (msgInfo == null || msgInfo.step == null)
            return;
        MessageServer.Step step = msgInfo.step;
        try {
            mPaint.setColor(Color.parseColor("#" + msgInfo.step.color));
        } catch (Exception e) {
            mPaint.setColor(Color.BLACK);
        }
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        switch (step.type) {
            case 1:
                mPaint.setStrokeWidth(msgInfo.step.lineSize);
                Path path = new Path();
                ArrayList<MessageServer.Position> points = msgInfo.step.points;
                if (points != null && points.size() > 0) {
                    for (int j = 0; j < points.size(); j++) {
                        if (j == 0)
                            path.moveTo((float) points.get(j).x, (float) points.get(j).y);
                        else
                            path.lineTo((float) points.get(j).x, (float) points.get(j).y);
                    }
                }
                canvas.drawPath(path, mPaint);
                break;
            case 4:
                mPaint.setTextSize(msgInfo.step.fs);
                canvas.drawText(msgInfo.step.ft, (float) msgInfo.step.sPoint.x, (float) msgInfo.step.sPoint.y, mPaint);
                break;
        }

    }


    public void setSteps(List<MessageServer.MsgInfo> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                MessageServer.MsgInfo msgInfo = list.get(i);
                setSteps(msgInfo, false);
            }
            invalidate();
            Log.e(TAG, "msgInfo.size:" + msgInfos.size());
        }
    }

    public void setSteps(MessageServer.MsgInfo msgInfo) {
        setSteps(msgInfo, true);
    }

    private void setSteps(MessageServer.MsgInfo msgInfo, boolean invalidate) {
        switch (msgInfo.event) {
            case MessageServer.EVENT_SHOWBOARD:
                if (msgInfo.showType == 1)
                    this.setVisibility(VISIBLE);
                else
                    this.setVisibility(GONE);
                break;
            case MessageServer.EVENT_PAINTBOARD:
                msgInfos.add(msgInfo);
                if (invalidate)
                    invalidate();
                break;
            case MessageServer.EVENT_DELETEBOARD:
                Iterator<MessageServer.MsgInfo> msgIter = msgInfos.iterator();
                while (msgIter.hasNext()) {
                    MessageServer.MsgInfo info = msgIter.next();
                    if (msgInfo.step == null || msgInfo.step.id == -1)
                        continue;
                    if (info.step == null || info.step.id == -1)
                        continue;
                    if (info.step.id == msgInfo.step.id)
                        msgIter.remove();
                }
                if (invalidate)
                    invalidate();
                break;
            case MessageServer.EVENT_CLEARBOARD:
                msgInfos.clear();
                if (invalidate)
                    invalidate();
                break;
        }

    }
}
