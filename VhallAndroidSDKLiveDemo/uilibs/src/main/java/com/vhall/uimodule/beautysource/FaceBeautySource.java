package com.vhall.uimodule.beautysource;

import com.vhall.beautify.type.VHBeautifyFilterConfig;
import com.vhall.beautify.type.VHBeautifyParamConfig;
import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.uimodule.R;

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
        //白牙
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.TOOTH_WHITEN_INTENSITY, R.string.beauty_box_tooth_whiten,
                        R.drawable.beauty_skin_teeth_drawable, R.drawable.beauty_skin_teeth_drawable
                )
        );
        //锐化
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.SHARPEN_INTENSITY, R.string.beauty_box_sharpen,
                        R.drawable.beauty_skin_sharpen_drawable, R.drawable.beauty_skin_sharpen_drawable
                )
        );
        //鼻子
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.NOSE_INTENSITY, R.string.beauty_box_intensity_nose,
                        R.drawable.beauty_shape_thin_nose_drawable, R.drawable.beauty_shape_thin_nose_drawable
                )
        );
        //长鼻子
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.LONG_NOSE_INTENSITY, R.string.beauty_micro_long_nose,
                        R.drawable.beauty_skin_longthnose_drawable, R.drawable.beauty_skin_longthnose_drawable
                )
        );
        //嘴巴
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.MOUTH_INTENSITY, R.string.beauty_box_intensity_mouth,
                        R.drawable.beauty_shape_mouth_drawable, R.drawable.beauty_shape_mouth_drawable
                )
        );
        //厚度
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_LIP_THICK, R.string.beauty_intensity_lip_thick,
                        R.drawable.beauty_skin_llipsthickness_drawable, R.drawable.beauty_skin_llipsthickness_drawable
                )
        );
        //人中
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.PHILTRUM_INTENSITY, R.string.beauty_micro_philtrum,
                        R.drawable.beauty_skin_philtrum_drawable, R.drawable.beauty_skin_philtrum_drawable
                )
        );
        //微笑
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.SMILE_INTENSITY, R.string.beauty_micro_smile,
                        R.drawable.beauty_skin_smile_drawable, R.drawable.beauty_skin_smile_drawable
                )
        );
        //下巴
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHIN_INTENSITY, R.string.beauty_box_intensity_chin,
                        R.drawable.beauty_skin_chin_intensity_drawable, R.drawable.beauty_skin_chin_intensity_drawable
                )
        );
        //清晰
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CLARITY, R.string.beauty_clarity,
                        R.drawable.beauty_skin_clarity_drawable, R.drawable.beauty_skin_clarity_drawable
                )
        );
        //瘦脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_thinning,
                        R.drawable.beauty_shape_face_cheekthin_drawable, R.drawable.beauty_shape_face_cheekthin_drawable
                )
        );
        //V脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_V_INTENSITY, R.string.beauty_box_cheek_v,
                        R.drawable.beauty_skin_v_face_drawable, R.drawable.beauty_skin_v_face_drawable
                )
        );
        //立体
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.FACE_THREED, R.string.beauty_face_threed,
                        R.drawable.beauty_skin_face_threed_drawable, R.drawable.beauty_skin_face_threed_drawable
                )
        );
        //窄脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_NARROW_INTENSITY, R.string.beauty_box_cheek_narrow,
                        R.drawable.beauty_skin_cheek_narrow_drawable, R.drawable.beauty_skin_cheek_narrow_drawable
                )
        );
        //短脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_SHORT_INTENSITY, R.string.beauty_box_cheek_short,
                        R.drawable.beauty_skin_cheek_short_drawable, R.drawable.beauty_skin_cheek_short_drawable
                )
        );
        //小脸
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CHEEK_SMALL_INTENSITY, R.string.beauty_box_cheek_small,
                        R.drawable.beauty_skin_cheek_small_drawable, R.drawable.beauty_skin_cheek_small_drawable
                )
        );
        //额头
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.FOREHEAD_INTENSITY, R.string.beauty_box_intensity_forehead,
                        R.drawable.beauty_shape_forehead_drawable, R.drawable.beauty_shape_forehead_drawable
                )
        );
        //下颚骨
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_LOW_JAW_INTENSITY, R.string.avatar_chin_thin,
                        R.drawable.beauty_skin_low_jaw_drawable, R.drawable.beauty_skin_low_jaw_drawable
                )
        );
        //瘦颧骨
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_CHEEKBONES_INTENSITY, R.string.beauty_box_cheekbones,
                        R.drawable.beauty_skin_cheek_bones_drawable, R.drawable.beauty_skin_cheek_bones_drawable
                )
        );
        //法令纹
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.REMOVE_NASOLABIAL_FOLDS_INTENSITY, R.string.beauty_micro_nasolabial,
                        R.drawable.beauty_skin_nasolabial_drawable, R.drawable.beauty_skin_nasolabial_drawable
                )
        );
        //眉距
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.BROW_SPACE_INTENSITY, R.string.beauty_intensity_brow_space,
                        R.drawable.beauty_skin_brow_space_drawable, R.drawable.beauty_skin_brow_space_drawable
                )
        );
        //粗细
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_BROW_THICK, R.string.beauty_intensity_brow_thick,
                        R.drawable.beauty_skin_brow_thick_drawable, R.drawable.beauty_skin_brow_thick_drawable
                )
        );
        //上下
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.BROW_HEIGHT_INTENSITY, R.string.beauty_intensity_brow_height,
                        R.drawable.beauty_skin_brow_height_drawable, R.drawable.beauty_skin_brow_height_drawable
                )
        );
        //亮眼
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_BRIGHT_INTENSITY, R.string.beauty_box_eye_bright,
                        R.drawable.beauty_skin_eyes_bright_drawable, R.drawable.beauty_skin_eyes_bright_drawable
                )
        );
        //眼睑
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_EYE_LID, R.string.beauty_intensity_eye_lid,
                        R.drawable.beauty_skin_eye_lid_drawable, R.drawable.beauty_skin_eye_lid_drawable
                )
        );
        //位置
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.INTENSITY_EYE_HEIGHT, R.string.beauty_eye_location,
                        R.drawable.beauty_skin_eye_height_drawable, R.drawable.beauty_skin_eye_height_drawable
                )
        );
        //圆眼
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_CIRCLE_INTENSITY, R.string.beauty_box_eye_circle,
                        R.drawable.beauty_skin_eye_circle_drawable, R.drawable.beauty_skin_eye_circle_drawable
                )
        );

        //眼距
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_SPACE_INTENSITY, R.string.beauty_micro_eye_space,
                        R.drawable.beauty_skin_eye_space_drawable, R.drawable.beauty_skin_eye_space_drawable
                )
        );
        //大眼
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_ENLARGING_INTENSITY, R.string.beauty_box_eye_enlarge,
                        R.drawable.beauty_shape_enlarge_eye_drawable, R.drawable.beauty_shape_enlarge_eye_drawable
                )
        );
        //黑眼圈
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.REMOVE_POUCH_INTENSITY, R.string.beauty_micro_pouch,
                        R.drawable.beauty_skin_remove_pouch_drawable, R.drawable.beauty_skin_remove_pouch_drawable
                )
        );
        //开眼角
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.CANTHUS_INTENSITY, R.string.beauty_micro_canthus,
                        R.drawable.beauty_skin_canthus_drawable, R.drawable.beauty_skin_canthus_drawable
                )
        );
        //角度
        params.add(
                new FaceBeautyBean(
                        VHBeautifyParamConfig.EYE_ROTATE_INTENSITY, R.string.beauty_micro_eye_rotate,
                        R.drawable.beauty_skin_eye_rotate_drawable, R.drawable.beauty_skin_eye_rotate_drawable
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
        params.put(VHBeautifyParamConfig.REMOVE_POUCH_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.REMOVE_NASOLABIAL_FOLDS_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CLARITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_LONG_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_V_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_NARROW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_SHORT_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHEEK_SMALL_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_CHEEKBONES_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_LOW_JAW_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CHIN_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.CANTHUS_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_SPACE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_ROTATE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.LONG_NOSE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.PHILTRUM_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.SMILE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.EYE_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.BROW_HEIGHT_INTENSITY,  new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.BROW_SPACE_INTENSITY,  new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_EYE_LID,  new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_EYE_HEIGHT,  new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_BROW_THICK,  new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.INTENSITY_LIP_THICK,  new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        params.put(VHBeautifyParamConfig.FACE_THREED,  new ModelAttributeData(0.5, 0.0, 0.0, 1.0));

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
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ZIRAN, R.mipmap.filter_ziran, R.string.ziran));
        //粉嫩
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.FENNEN, R.mipmap.filter_fennen, R.string.fennen));
        //白亮
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.BAILIANG, R.mipmap.filter_bailiang, R.string.bailiang));
        //小清新
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.XIAOQINGXIN, R.mipmap.filter_xiaoqingxin, R.string.xiaoqingxin));
        //冷色调
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.LENGSEDIAO, R.mipmap.filter_lengsediao, R.string.lengsediao));
        //暖色调
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.NUANSEDIAO, R.mipmap.filter_nuansediao, R.string.nuansediao));
        // 红润
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.HONGRUN, R.mipmap.filter_hongrun, R.string.beauty_box_red_level));
        // 温暖
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.WENNUAN, R.mipmap.filter_wennuan, R.string.filter_wen_nuang));
        // 幸运
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.XINGYUN, R.mipmap.filter_xingyun, R.string.filter_xing_yun));
        // 蜜桃
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.MITAO, R.mipmap.filter_mitao, R.string.filter_mitao));
        // 质感会
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.ZHIGANHUI, R.mipmap.filter_zhiganhui, R.string.filter_zhiganhui));
        // 甜美
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.TIANMEI, R.mipmap.filter_tianmei, R.string.makeup_combination_sweet));
        // 粉红
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.FENHONG, R.mipmap.filter_fenhong, R.string.filter_fen_hong));
        // 森林
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.SENLIN, R.mipmap.filter_senlin, R.string.bg_seg_green_forest));
        // 樱花
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.YINGHUA, R.mipmap.filter_yinghua, R.string.filter_ying_hua));
        // 淡雅
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.DANYA, R.mipmap.filter_danya, R.string.filter_dang_ya));
        // 阳光
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.YANGGUANG, R.mipmap.filter_yangguang, R.string.filter_yang_guang));
        // 新白
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.XINBAI, R.mipmap.filter_xinbai, R.string.filter_xin_bai));
        // 秋色
        filters.add(new FaceBeautyFilterBean(VHBeautifyFilterConfig.QIUSE, R.mipmap.filter_qiuse, R.string.filter_qiu_se));
        return filters;
    }
}
