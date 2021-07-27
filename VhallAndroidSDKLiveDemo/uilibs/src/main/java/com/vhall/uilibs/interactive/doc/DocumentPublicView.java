package com.vhall.uilibs.interactive.doc;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.vhall.document.DocumentView;
import com.vhall.document.IDocument;
import com.vhall.ops.VHOPS;
import com.vhall.uilibs.R;
import com.vhall.uilibs.interactive.dialog.OutDialog;
import com.vhall.uilibs.interactive.dialog.OutDialogBuilder;
import com.vhall.uilibs.util.DensityUtils;
import com.vhall.uilibs.util.DocTouchListener;
import com.vhall.uilibs.util.emoji.KeyBoardManager;
import org.json.JSONException;
import org.json.JSONObject;
import static com.vhall.document.DocumentView.DOC_DOCUMENT;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_CREATE;
import static com.vhall.ops.VHOPS.TYPE_DESTROY;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;

/**
 * @author hkl
 * Date: 2019-11-19 15:42
 */
public class DocumentPublicView extends RelativeLayout {
    private static final String TAG = "DocumentPublicView";
    private Context context;
    private VHOPS vhops;
    private IDocViewLister docViewLister;
    private int size = 20;
    private String color = "#3478F6";
    private boolean canEnable = false;
    private DocumentEditPopup editPopup, colorPopup, shapePopup, linePopup;
    private String mainId;
    private String ownerId;
    private String orientation;

    private RelativeLayout rlDocView;
    private DocumentView documentView;
    private RelativeLayout rlRoot;

    private IDocument.DrawType mType = IDocument.DrawType.PATH;
    private IDocument.DrawAction mAction = IDocument.DrawAction.ADD;

    public void setMainId(String mainId) {
        this.mainId = mainId;
        if (!TextUtils.equals(ownerId, mainId)) {
            //观看端
            if (vhops != null) {
                vhops.setEditable(false);
            }
            hintPop();
        } else {
            if (vhops != null) {
                vhops.setEditable(true);
                canEnable = false;
            }
        }
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
        LayoutParams params = (LayoutParams) rlDocView.getLayoutParams();
        if ("1".equals(orientation)) {
            params.width = DensityUtils.getScreenHeight() * 16 / 9;
            params.height = DensityUtils.getScreenHeight();
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
        } else {
            params.width = DensityUtils.getScreenWidth();
            params.height = 9 * DensityUtils.getScreenWidth() / 16;
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        rlDocView.setLayoutParams(params);
    }

    public void setDocViewLister(IDocViewLister docViewLister) {
        this.docViewLister = docViewLister;
    }

    public DocumentPublicView(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public DocumentPublicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public DocumentPublicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }


    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.doc_public_view, this);
        rlDocView = rootView.findViewById(R.id.rl_doc_view);
        rlRoot = rootView.findViewById(R.id.rl_root);
    }


    public void init(String channelId, String roomId, String accessToken, boolean loadLastDoc) {
        vhops = new VHOPS(context, channelId, roomId, accessToken, loadLastDoc);
        vhops.setListener(opsCallback);
        vhops.join();
    }

    public void showDoc(String docId) {
        vhops.setEditable(true);
        vhops.addView(DOC_DOCUMENT, docId, 960, 540);

        if (docViewLister != null) {
            docViewLister.setVisibility(GONE, VISIBLE);
        }
        vhops.switchOn();
    }

    public void hintPop() {
        if (editPopup != null) {
            editPopup.dismiss();
        }
    }

    private OutDialog showClear;

    public void showPop(View view) {
        if (editPopup == null) {
            editPopup = new DocumentEditPopup(context, DocumentEditPopup.POP_TYPE_EDIT);
            editPopup.setOnChildClickListener(new DocumentEditPopup.OnChildClickListener() {
                @Override
                public void onChildViewClickListener(int index, View view) {
                    switch (index) {
                        case 1:
                            if (showClear == null) {
                                showClear = new OutDialogBuilder()
                                        .title("确定要清空文档标记吗？")
                                        .tv1("取消")
                                        .tv2("确定")
                                        .onConfirm(new OutDialog.ClickLister() {
                                                          @Override
                                                          public void click() {
                                                              if (documentView != null) {
                                                                  documentView.clear();
                                                              }
                                                          }
                                                      }
                                        )
                                        .build(context);
                            }
                            showClear.show();
                            if (colorPopup != null) {
                                colorPopup.dismiss();
                            }
                            if (linePopup != null) {
                                linePopup.dismiss();
                            }
                            if (shapePopup != null) {
                                shapePopup.dismiss();
                            }
                            break;
                        case 2:
                            showLine();
                            break;
                        case 3:
                            if (colorPopup != null) {
                                colorPopup.dismiss();
                            }
                            if (linePopup != null) {
                                linePopup.dismiss();
                            }
                            if (shapePopup != null) {
                                shapePopup.dismiss();
                            }
                            mAction = IDocument.DrawAction.DELETE;
                            if (documentView != null) {
                                documentView.setAction(mAction);
                            }
                            break;
                        case 4:
                            showColor();
                            break;
                        case 5:
                            showShape();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void dismiss() {
                    if (colorPopup != null) {
                        colorPopup.dismiss();
                    }
                    if (linePopup != null) {
                        linePopup.dismiss();
                    }
                    if (shapePopup != null) {
                        shapePopup.dismiss();
                    }
                    canEnable = false;
                    if (documentView != null) {
                        onTouchListener = new DocTouchListener();
                        documentView.setOnTouchListener(onTouchListener);
                    }
                }
            });
        }
        if (documentView != null) {
            canEnable = true;
            onTouchListener = null;
            documentView.setOnTouchListener(null);
        }
        int height = DensityUtils.dpToPxInt(60) + KeyBoardManager.getVirtualButtonHeightDoc(context), x = DensityUtils.dpToPxInt(15);
        if (KeyBoardManager.hasVirtualButtonDoc((Activity) context)) {
            if ("1".equals(orientation)) {
                x = DensityUtils.dpToPxInt(20);
            }
        }
        editPopup.showAtLocation(view, Gravity.BOTTOM | Gravity.END, x, height);
        if (documentView != null) {
            documentView.setDrawType(mType);
            documentView.setDrawOption(color, size);
            documentView.setAction(mAction);
        }
    }

    private void showColor() {
        if (linePopup != null) {
            linePopup.dismiss();
        }
        if (shapePopup != null) {
            shapePopup.dismiss();
        }
        if (documentView != null) {
            mAction = IDocument.DrawAction.ADD;
            documentView.setDrawType(mType);
            documentView.setDrawOption(color, size);
            documentView.setAction(mAction);
        }
        if (colorPopup == null) {
            colorPopup = new DocumentEditPopup(context, DocumentEditPopup.POP_TYPE_COLOR);
            colorPopup.setOnChildClickListener(new DocumentEditPopup.OnChildClickListener() {
                @Override
                public void onChildViewClickListener(int index, View view) {
                    switch (index) {
                        case 1:
                            color = "#3478F6";
                            break;
                        case 2:
                            color = "#83D754";
                            break;
                        case 3:
                            color = "#F09A37";
                            break;
                        case 4:
                            color = "#FC5609";
                            break;
                        case 5:
                            color = "#ffffff";
                            break;
                        default:
                            break;
                    }
                    if (documentView != null) {
                        mAction = IDocument.DrawAction.ADD;
                        documentView.setDrawType(mType);
                        documentView.setDrawOption(color, size);
                        documentView.setAction(mAction);
                    }
                }

                @Override
                public void dismiss() {
                }
            });
            colorPopup.setViewBg(1);
        }
        int y = DensityUtils.dpToPxInt(60), x = DensityUtils.dpToPxInt(60);
        if (KeyBoardManager.hasVirtualButtonDoc((Activity) context)) {
            if ("1".equals(orientation)) {
                x = DensityUtils.dpToPxInt(90);
            } else {
                y = DensityUtils.dpToPxInt(60) + KeyBoardManager.getVirtualButtonHeightDoc(context);
            }
        }
        colorPopup.showAtLocation(rlDocView, Gravity.BOTTOM | Gravity.END, x, y);

    }

    private void showLine() {
        if (colorPopup != null) {
            colorPopup.dismiss();
        }
        if (shapePopup != null) {
            shapePopup.dismiss();
        }
        if (documentView != null) {
            mAction = IDocument.DrawAction.ADD;
            documentView.setDrawType(mType);
            documentView.setDrawOption(color, size);
            documentView.setAction(mAction);
        }
        if (linePopup == null) {
            linePopup = new DocumentEditPopup(context, DocumentEditPopup.POP_TYPE_STROKE);
            linePopup.setOnChildClickListener(new DocumentEditPopup.OnChildClickListener() {
                @Override
                public void onChildViewClickListener(int index, View view) {
                    switch (index) {
                        case 1:
                            size = 25;
                            break;
                        case 2:
                            size = 20;
                            break;
                        case 3:
                            size = 15;
                            break;
                        case 4:
                            size = 10;
                            break;
                        case 5:
                            size = 5;
                            break;
                        default:
                            break;
                    }
                    if (documentView != null) {
                        mAction = IDocument.DrawAction.ADD;
                        documentView.setDrawType(mType);
                        documentView.setDrawOption(color, size);
                        documentView.setAction(mAction);
                    }
                }

                @Override
                public void dismiss() {

                }
            });
            linePopup.setViewBg(1);
        }
        int y = DensityUtils.dpToPxInt(60), x = DensityUtils.dpToPxInt(60);
        if (KeyBoardManager.hasVirtualButtonDoc((Activity) context)) {
            if ("1".equals(orientation)) {
                x = DensityUtils.dpToPxInt(90);
            } else {
                y = DensityUtils.dpToPxInt(60) + KeyBoardManager.getVirtualButtonHeightDoc(context);
            }
        }
        linePopup.showAtLocation(rlDocView, Gravity.BOTTOM | Gravity.END, x, y);

    }

    private void showShape() {
        if (linePopup != null) {
            linePopup.dismiss();
        }
        if (colorPopup != null) {
            colorPopup.dismiss();
        }
        if (documentView != null) {
            mAction = IDocument.DrawAction.ADD;
            documentView.setDrawType(mType);
            documentView.setDrawOption(color, size);
            documentView.setAction(mAction);
        }
        if (shapePopup == null) {
            shapePopup = new DocumentEditPopup(context, DocumentEditPopup.POP_TYPE_SHAPE);
            shapePopup.setOnChildClickListener(new DocumentEditPopup.OnChildClickListener() {
                @Override
                public void onChildViewClickListener(int index, View view) {
                    switch (index) {
                        case 1:
                            mType = IDocument.DrawType.DOUBLE_ARROW;
                            break;
                        case 2:
                            mType = IDocument.DrawType.SINGLE_ARROW;
                            break;
                        case 3:
                            mType = IDocument.DrawType.PATH;
                            break;
                        case 4:
                            mType = IDocument.DrawType.CIRCLE;
                            break;
                        case 5:
                            mType = IDocument.DrawType.RECT;
                            break;
                        default:
                            break;
                    }
                    if (documentView != null) {
                        documentView.setDrawType(mType);
                        documentView.setDrawOption(color, size);
                        documentView.setAction(mAction);
                    }
                }

                @Override
                public void dismiss() {

                }
            });
            shapePopup.setViewBg(3);
        }
        int y = DensityUtils.dpToPxInt(60), x = DensityUtils.dpToPxInt(60);
        if (KeyBoardManager.hasVirtualButtonDoc((Activity) context)) {
            if ("1".equals(orientation)) {
                x = DensityUtils.dpToPxInt(90);
            } else {
                y = DensityUtils.dpToPxInt(60) + KeyBoardManager.getVirtualButtonHeightDoc(context);
            }
        }
        shapePopup.showAtLocation(rlDocView, Gravity.BOTTOM | Gravity.END, x, y);

    }

    float downX, downY, upX, upY;

    float lastRawX, lastRawY;//用于记录按钮上一次状态坐标
    int point = 1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (onTouchListener != null && onTouchListener.getScale() > 1) {
            return super.dispatchTouchEvent(event);
        }
        if (!canEnable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    lastRawX = event.getRawX();
                    lastRawY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    point = event.getPointerCount();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("MotionEvent", "upX " + point);
                    if (point == 1) {
                        upX = event.getX();
                        upY = event.getY();
                        if ((upX - downX) < -20) {
                            Log.e("MotionEvent", "下一页");
                            if (documentView != null) {
                                documentView.nextSlide();
                            }
                        } else if ((upX - downX) > 20) {
                            Log.e("MotionEvent", "上一页");
                            if (documentView != null) {
                                documentView.preSlide();
                            }
                        }
                    } else {
                        point = 1;
                    }
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void destroy() {
        if (vhops != null) {
            vhops.switchOff();
            vhops.leave();
        }
    }

    DocumentView.EventListener eventListener = new DocumentView.EventListener() {
        @Override
        public void onEvent(int i, String s) {
            switch (i) {
                case DocumentView.EVENT_PAGE_LOADED://界面加载完毕
                    break;
                case DocumentView.EVENT_DOC_LOADED://文档加载完毕
                    JSONObject optJson = null;
                    try {
                        optJson = new JSONObject(s);
                        Log.i(TAG, "页数：" + optJson.optString("show_page") + "/" + optJson.optString("page"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case DocumentView.EVENT_DOODLE://绘制数据回调
                    Log.i(TAG, "object:" + s);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(s);
                        if (object.has("info")) {
                            JSONObject info = object.optJSONObject("info");
                            Log.i(TAG, "页数：" + (info.optInt("slideIndex") + 1) + "/" + info.optInt("slidesTotal"));
                            Log.i(TAG, "步数：" + (info.optInt("stepIndex") + 1) + "/" + info.optInt("totalSteps"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case DocumentView.EVENT_LOADED_FAILED:
                    break;
                default:
                    break;
            }
        }
    };

    private DocTouchListener onTouchListener;
    private VHOPS.EventListener opsCallback = new VHOPS.EventListener() {
        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_ACTIVE)) {
                    documentView = vhops.getActiveView();
                    if (documentView != null) {
                        if (TextUtils.equals(ownerId, mainId) && vhops.isEditAble()) {
                            documentView.addListener(eventListener);
                            onTouchListener = new DocTouchListener();
                            documentView.setOnTouchListener(onTouchListener);
                        }
                        if (rlDocView != null) {
                            rlDocView.removeAllViews();
                            rlDocView.addView(documentView);
                            rlDocView.setVisibility(VISIBLE);
                            if (docViewLister != null) {
                                docViewLister.setVisibility(GONE, VISIBLE);
                            }
                            //强制刷新父布局
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    if(getParent() != null){
                                        View view = (View) getParent();
                                        view.setVisibility(GONE);
                                        view.setVisibility(VISIBLE);
                                    }
                                }
                            });
                        }
                    }
                } else if (type.equals(TYPE_CREATE)) {
                    //创建文档

                } else if (type.equals(TYPE_DESTROY)) {
                    //删除编号 cid的文档

                } else if (type.equals(TYPE_SWITCHOFF)) {
                    //关闭文档演示
//                    if (rlDocView != null) {
//                        rlDocView.setVisibility(GONE);
//                        if (docViewLister != null) {
//                            docViewLister.setVisibility(VISIBLE, GONE);
//                        }
//                    }
                } else if (type.equals(TYPE_SWITCHON)) {
                    //打开文档演示
                    if (rlDocView != null) {
                        rlDocView.setVisibility(VISIBLE);
                        if (docViewLister != null) {
                            docViewLister.setVisibility(GONE, VISIBLE);
                        }
                    }
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            if (docViewLister != null) {
                docViewLister.onError(errorCode, errorMsg);
            }
        }
    };

}
