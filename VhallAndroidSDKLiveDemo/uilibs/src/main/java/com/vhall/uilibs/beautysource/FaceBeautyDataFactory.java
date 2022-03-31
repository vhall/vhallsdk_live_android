package com.vhall.uilibs.beautysource;


import android.content.Context;
import android.util.Log;

import com.faceunity.core.controller.facebeauty.FaceBeautyParam;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.vhall.beautify.DefaultFaceBeautyDataFactory;
import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautify.type.VHBeautifyFilterConfig;
import com.vhall.beautify.type.VHBeautifyParamConfig;
import com.vhall.beautifykit.entity.FaceBeautyBean;
import com.vhall.beautifykit.entity.FaceBeautyFilterBean;
import com.vhall.beautifykit.entity.FaceBeautyStyleBean;
import com.vhall.beautifykit.entity.ModelAttributeData;
import com.vhall.beautifykit.infe.AbstractFaceBeautyDataFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜业务工厂
 * Created on 2021/3/1
 */
public class FaceBeautyDataFactory extends AbstractFaceBeautyDataFactory {

    private final String TAG = "FaceBeautyDataFactory";

    /*推荐风格标识*/
    private static int currentStyleIndex = -1;

    private Context context;


    public FaceBeautyDataFactory(Context context) {
        this.context = context;
    }

    /**
     * 获取美肤参数列表
     *
     * @return
     */

    @Override
    public ArrayList<FaceBeautyBean> getSkinBeauty() {
        return new ArrayList<>();
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */

    @Override
    public ArrayList<FaceBeautyBean> getShapeBeauty() {
        return FaceBeautySource.buildShapeParams();
    }

    /**
     * 获取美型参数列表
     *
     * @return
     */

    @Override
    public ArrayList<FaceBeautyBean> getShapeBeautySubItem() {
        return FaceBeautySource.buildFaceShapeSubItemParams();
    }


    /**
     * 获取美肤、美型扩展参数
     *
     * @return
     */

    @Override
    public HashMap<String, ModelAttributeData> getModelAttributeRange() {
        return FaceBeautySource.buildModelAttributeRange();
    }

    /**
     * 获取滤镜参数列表
     *
     * @return
     */

    @Override
    public ArrayList<FaceBeautyFilterBean> getBeautyFilters() {
        ArrayList<FaceBeautyFilterBean> filterBeans = FaceBeautySource.buildFilters();
        for (int i = 0; i < filterBeans.size(); i++) {
            if (filterBeans.get(i).getKey().equals(currentFaceBeauty().getFilterName())) {
                filterBeans.get(i).setIntensity(currentFaceBeauty().getFilterIntensity());
            }
        }
        return filterBeans;
    }

    /**
     * 获取当前滤镜下标
     *
     * @return
     */
    @Override
    public int getCurrentFilterIndex() {
        return BeautyManager.getFilterSelectFromCatch(context);
    }

    /**
     * 设置当前滤镜下标
     *
     * @param currentFilterIndex
     */
    @Override
    public void setCurrentFilterIndex(int currentFilterIndex) {
        BeautyManager.setFilterSelect(currentFilterIndex, context);
    }

    /**
     * 获取推荐风格列表
     *
     * @return
     */

    @Override
    public ArrayList<FaceBeautyStyleBean> getBeautyStyles() {
        return new ArrayList<>();
    }


    /**
     * 获取当前风格推荐标识
     *
     * @return
     */
    @Override
    public int getCurrentStyleIndex() {
        return currentStyleIndex;
    }

    /**
     * 设置风格推荐标识
     *
     * @param styleIndex
     */
    @Override
    public void setCurrentStyleIndex(int styleIndex) {
        currentStyleIndex = styleIndex;
    }

    /**
     * 美颜滤镜总开关设置
     *
     * @param enable
     */
    @Override
    public void enableFaceBeauty(boolean enable) {
    }

    /**
     * 获取模型参数
     *
     * @param key 名称标识
     * @return 属性值
     */
    @Override
    public double getParamIntensity(String key) {
        return BeautyManager.getBeautyItemNumFromCatch(context, key, 0.4);
    }

    @Override
    public double getParamIntensity(String key, double origin) {

        double beautyItemNumFromCatch = BeautyManager.getBeautyItemNumFromCatch(context, key, origin);
        return beautyItemNumFromCatch;
    }

    /**
     * 设置模型参数
     *
     * @param key   名称标识
     * @param value 属性值
     */
    @Override
    public void updateParamIntensity(String key, double value, boolean catchData) {
        VHBeautifyKit.getInstance().updateParamIntensity(key, value);
        if (catchData)
            BeautyManager.setBeautyItemNum(key, String.valueOf(value), context);
    }

    @Override
    public String getCurrentOneHotFaceShape() {
        return CurrentFaceShapeUIValue.currentFaceShape == null ? FaceBeautyParam.CHEEK_V_INTENSITY : CurrentFaceShapeUIValue.currentFaceShape;
    }

    @Override
    public void setCurrentOneHotFaceShape(String faceShape) {
        CurrentFaceShapeUIValue.currentFaceShape = faceShape;
    }

    /**
     * 获取当前脸型的UI值
     */
    public HashMap<String, Double> getCurrentFaceShapeUIValue() {
        return CurrentFaceShapeUIValue.currentFaceShapeValue;
    }

    private void setD(HashMap<String, Double> currentFaceShapeValue, String key, double origin) {
        currentFaceShapeValue.put(key, BeautyManager.getBeautyItemNumFromCatch(context, key, origin));
    }

    /**
     * 切换滤镜
     *
     * @param name      滤镜名称标识
     * @param intensity 滤镜强度
     * @param position  第几个
     */
    @Override
    public void onFilterSelected(String name, double intensity, int position) {
        VHBeautifyKit.getInstance().setFilter(name, intensity);
        BeautyManager.setBeautyItemNum(name, String.valueOf(intensity), context);
        BeautyManager.setFilterSelect(position, context);
    }

    /**
     * 更换滤镜强度
     *
     * @param intensity 滤镜强度
     */
    @Override
    public void updateFilterIntensity(String name, double intensity) {
        VHBeautifyKit.getInstance().setFilter(name, intensity);
        BeautyManager.setBeautyItemNum(name, String.valueOf(intensity), context);
    }

    /**
     * 设置推荐风格
     *
     * @param name
     */
    @Override
    public void onStyleSelected(String name) {
    }

    // 只更改缓存数据
    @Override
    public void updateCatchIntensity(@NotNull String key, double value) {
        BeautyManager.setBeautyItemNum(key, String.valueOf(value), context);
    }

    /**
     * 设置美颜的单独开关
     *
     * @param open
     */
    @Override
    public void setOpenBeauty(boolean open) {
        BeautyManager.setBeautySwitch(open, context);
    }

    @Override
    public boolean getOpenBeauty() {
        return BeautyManager.getBeautySwitchFromCatch(context);
    }

    private FaceBeauty currentFaceBeauty() {
        return DefaultFaceBeautyDataFactory.currentBeauty;
    }

    /**
     * 用于记录当前脸型的UI值 -> 用于用户下次点入的时候恢复
     */
    static class CurrentFaceShapeUIValue {
        /* 当前生效的脸型 */
        public static String currentFaceShape = FaceBeautyParam.CHEEK_V_INTENSITY;
        /* 当前脸型的UI值 */
        public static HashMap<String, Double> currentFaceShapeValue = new HashMap<>();

    }
}
