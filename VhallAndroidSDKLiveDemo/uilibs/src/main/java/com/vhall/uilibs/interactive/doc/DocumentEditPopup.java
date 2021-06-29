package com.vhall.uilibs.interactive.doc;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.vhall.uilibs.R;

import java.util.ArrayList;
import java.util.List;


/**
 * @author hkl
 * Date: 2019-11-20 16:19
 */
public class DocumentEditPopup extends PopupWindow implements View.OnClickListener {

    public static final String POP_TYPE_COLOR = "color";
    public static final String POP_TYPE_EDIT = "edit";
    public static final String POP_TYPE_STROKE = "stroke";
    public static final String POP_TYPE_SHAPE = "shape";

    private Context context;
    private ImageView imageView1, imageView2, imageView3, imageView4, imageView5;
    private String type;
    private OnChildClickListener onChildClickListener;
    private int choose = 0;
    private List<ImageView> imageViews = new ArrayList<>();

    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        this.onChildClickListener = onChildClickListener;
    }

    public DocumentEditPopup(Context context, String type) {
        super(context);
        this.context = context;
        this.type = type;
        setBackgroundDrawable(null);
        setOutsideTouchable(false);
        setFocusable(false);
        View root = View.inflate(context, R.layout.pop_doc_edit, null);
        imageView1 = root.findViewById(R.id.image1);
        imageView2 = root.findViewById(R.id.image2);
        imageView3 = root.findViewById(R.id.image3);
        imageView4 = root.findViewById(R.id.image4);
        imageView5 = root.findViewById(R.id.image5);
        imageViews.add(imageView1);
        imageViews.add(imageView2);
        imageViews.add(imageView3);
        imageViews.add(imageView4);
        imageViews.add(imageView5);

        for (ImageView v:imageViews){
            v.setOnClickListener(this);
        }
        root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setContentView(root);
        initViewBg();
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }

    private void initViewBg() {
        switch (type) {
            case POP_TYPE_COLOR:
                imageView1.setImageResource(R.drawable.shape_doc_color_blue);
                imageView2.setImageResource(R.drawable.shape_doc_color_green);
                imageView3.setImageResource(R.drawable.shape_doc_color_yellow);
                imageView4.setImageResource(R.drawable.shape_doc_color_red);
                imageView5.setImageResource(R.drawable.shape_doc_color_white);
                break;
            case POP_TYPE_SHAPE:
                imageView1.setImageResource(R.drawable.svg_ic_doc_double_arrow);
                imageView2.setImageResource(R.drawable.svg_ic_doc_sigle_arrow);
                imageView3.setImageResource(R.drawable.svg_ic_doc_sigle_line);
                imageView4.setImageResource(R.drawable.svg_ic_doc_cicle);
                imageView5.setImageResource(R.drawable.svg_ic_doc_rectangle);
                break;
            case POP_TYPE_STROKE:
                imageView1.setImageResource(R.drawable.svg_ic_doc_line5);
                imageView2.setImageResource(R.drawable.svg_ic_doc_line4);
                imageView3.setImageResource(R.drawable.svg_ic_doc_line3);
                imageView4.setImageResource(R.drawable.svg_ic_doc_line2);
                imageView5.setImageResource(R.drawable.svg_ic_doc_line1);
                break;
            default:
                break;
        }
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if (onChildClickListener != null) {
            onChildClickListener.dismiss();
        }
    }

    public void show(View v, int x, int y) {
        showAsDropDown(v, x, y);
    }

    public void setViewBg(int position) {
        choose=position;
        switch (position) {
            case 1:
                imageView1.setSelected(true);
                imageView2.setSelected(false);
                imageView3.setSelected(false);
                imageView4.setSelected(false);
                imageView5.setSelected(false);
                break;
            case 2:
                imageView1.setSelected(false);
                imageView2.setSelected(true);
                imageView3.setSelected(false);
                imageView4.setSelected(false);
                imageView5.setSelected(false);
                break;
            case 3:
                imageView1.setSelected(false);
                imageView2.setSelected(false);
                imageView3.setSelected(true);
                imageView4.setSelected(false);
                imageView5.setSelected(false);
                break;
            case 4:
                imageView1.setSelected(false);
                imageView2.setSelected(false);
                imageView3.setSelected(false);
                imageView4.setSelected(true);
                imageView5.setSelected(false);
                break;
            case 5:
                imageView1.setSelected(false);
                imageView2.setSelected(false);
                imageView3.setSelected(false);
                imageView4.setSelected(false);
                imageView5.setSelected(true);
                break;
            default:
                imageView1.setSelected(false);
                imageView2.setSelected(false);
                imageView3.setSelected(false);
                imageView4.setSelected(false);
                imageView5.setSelected(false);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        if(v == imageView1){
            if (choose != 1) {
                choose = 1;
                if (onChildClickListener != null) {
                    onChildClickListener.onChildViewClickListener(1, v);
                }
            }
        }else if(v == imageView2){
            if (choose != 2) {
                choose = 2;
                if (onChildClickListener != null) {
                    onChildClickListener.onChildViewClickListener(2, v);
                }
            }
        }else if(v == imageView3){
            if (choose != 3) {
                choose = 3;
                if (onChildClickListener != null) {
                    onChildClickListener.onChildViewClickListener(3, v);
                }
            }
        }else if(v == imageView4){
            if (choose != 4) {
                choose = 4;
                if (onChildClickListener != null) {
                    onChildClickListener.onChildViewClickListener(4, v);
                }
            }
        }else if(v == imageView5){
            if (choose != 5) {
                choose = 5;
                if (onChildClickListener != null) {
                    onChildClickListener.onChildViewClickListener(5, v);
                }
            }
        }
        setViewBg(choose);
    }

    public interface OnChildClickListener {
        void onChildViewClickListener(int index, View view);

        void dismiss();
    }


}

