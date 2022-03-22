package com.vhall.uilibs.beautysource;

import com.vhall.beautify.type.VHBeautifyFilterConfig;
import com.vhall.beautify.type.VHBeautifyParamConfig;
import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.uilibs.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜数据构造
 * Created on 2021/3/27
 */
public class FaceBeautySource {


    /**
     * 初始化美颜参数
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyBean> buildShapeParams() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        // 原图
        params.add(new FaceBeautyBean(
                        "reset", R.string.beauty_box_heavy_blur_fine,
                        R.drawable.beauty_skin_buffing_drawable, R.drawable.beauty_skin_buffing_drawable, FaceBeautyBean.ButtonType.CHECK_BUTTON
                )
        ); //磨皮
        params.add(new FaceBeautyBean(
                        VHBeautifyParamConfig.BLUR_INTENSITY, R.string.beauty_box_heavy_blur_fine,
                        R.drawable.beauty_skin_buffing_drawable, R.drawable.beauty_skin_buffing_drawable
                )
        );
        //美白
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.COLOR_INTENSITY, R.string.beauty_box_color_level,
                        R.drawable.beauty_skin_color_drawable, R.drawable.beauty_skin_color_drawable
                )
        );
        //红润
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.RED_INTENSITY, R.string.beauty_box_red_level,
                        R.drawable.beauty_skin_red_drawable, R.drawable.beauty_skin_red_drawable
                )
        );
        //大眼
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_ENLARGING_INTENSITY, R.string.beauty_box_eye_enlarge,
                        R.drawable.beauty_shape_enlarge_eye_drawable, R.drawable.beauty_shape_enlarge_eye_drawable
                )
        );
        //瘦脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_thinning,
                        R.drawable.beauty_shape_face_cheekthin_drawable, R.drawable.beauty_shape_face_cheekthin_drawable
                )
        );
        //锐化
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.SHARPEN_INTENSITY, R.string.beauty_box_sharpen,
                        R.drawable.beauty_skin_sharpen_drawable, R.drawable.beauty_skin_sharpen_drawable
                )
        );
        //白牙
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.TOOTH_WHITEN_INTENSITY, R.string.beauty_box_tooth_whiten,
                        R.drawable.beauty_skin_teeth_drawable, R.drawable.beauty_skin_teeth_drawable
                )
        );
        //亮眼
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_BRIGHT_INTENSITY, R.string.beauty_box_eye_bright,
                        R.drawable.beauty_skin_eyes_bright_drawable, R.drawable.beauty_skin_eyes_bright_drawable
                )
        );
        //额头
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.FOREHEAD_INTENSITY, R.string.beauty_box_intensity_forehead,
                        R.drawable.beauty_shape_forehead_drawable, R.drawable.beauty_shape_forehead_drawable
                )
        );
        //鼻子
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.NOSE_INTENSITY, R.string.beauty_box_intensity_nose,
                        R.drawable.beauty_shape_thin_nose_drawable, R.drawable.beauty_shape_thin_nose_drawable
                )
        );
        //嘴巴
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.MOUTH_INTENSITY, R.string.beauty_box_intensity_mouth,
                        R.drawable.beauty_shape_mouth_drawable, R.drawable.beauty_shape_mouth_drawable
                )
        );
        return params;
    }

    /**
     * 加载脸型子项
     *
     * @return
     */
    public static ArrayList<FaceBeautyBean> buildFaceShapeSubItemParams() {
        return buildSubItemParams(VHBeautifyParamConfig.FACE_SHAPE);
    }

    public static ArrayList<FaceBeautyBean> buildSubItemParams(String key) {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        return params;
    }
    //原始数据
    public static HashMap<String, ModelAttributeData> buildModelAttributeRange() {
        HashMap<String, ModelAttributeData> params = new HashMap<>();
        params.put("reset", new ModelAttributeData(0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.BLUR_INTENSITY, new ModelAttributeData(2.0, 0.0, 0.0, 6.0));
        params.put(VHBeautifyParamConfig.COLOR_INTENSITY, new ModelAttributeData(1.04, 0.0, 0.0, 2.0));
        params.put(VHBeautifyParamConfig.RED_INTENSITY, new ModelAttributeData(1, 0.0, 0.0, 2.0));
        params.put(VHBeautifyParamConfig.EYE_ENLARGING_INTENSITY, new ModelAttributeData(0.25, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_THINNING_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.SHARPEN_INTENSITY, new ModelAttributeData(0.7, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.TOOTH_WHITEN_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_BRIGHT_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.FOREHEAD_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.NOSE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.MOUTH_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        return params;
    }
    /**
     * 初始化滤镜参数
     * VHBeautifyFilterConfig
     *
     * @return ArrayList<FaceBeautyFilterBean>
     */
    public static ArrayList<FaceBeautyFilterBean> buildFilters() {
        ArrayList<FaceBeautyFilterBean> filters = new ArrayList<>();
        //原图
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ORIGIN, R.mipmap.icon_origin_background, R.string.origin, 0.0, FaceBeautyFilterBean.ButtonType.CHECK_BUTTON,R.mipmap.icon_origin_check_background));
        //自然
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ZIRAN, R.mipmap.icon_beauty_filter_natural, R.string.ziran));
        //粉嫩
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.FENNEN, R.mipmap.icon_beauty_filter_fennen, R.string.fennen));
        //白亮
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.BAILIANG, R.mipmap.icon_beauty_filter_bailiang, R.string.bailiang));
        //小清新
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.XIAOQINGXIN, R.mipmap.icon_beauty_filter_xiaoqingxin, R.string.xiaoqingxin));
        //冷色调
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.LENGSEDIAO, R.mipmap.icon_beauty_filter_lengsediao, R.string.lengsediao));
        //暖色调
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.NUANSEDIAO, R.mipmap.icon_beauty_filter_nuansediao, R.string.nuansediao));
        return filters;
    }
}
