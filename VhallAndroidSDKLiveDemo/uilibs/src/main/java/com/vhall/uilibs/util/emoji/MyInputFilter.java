package com.vhall.uilibs.util.emoji;

import android.text.InputFilter;
import android.text.Spanned;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

/**
 * Created by huanan on 2016/6/3.
 */
public class MyInputFilter implements InputFilter {

    // 观看界面 直播70个汉字140字符，回放140个汉字280个字符 聊天界面140个汉字 280个字符

    int limitNum = 200;

    public MyInputFilter(int limitNum) {
        this.limitNum = limitNum;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int destLen = dest.toString().getBytes("GB18030").length;
            int sourceLen = source.toString().getBytes("GB18030").length;
            if (destLen + sourceLen > limitNum) {//TODO 复制情况
                return "";
            }
            boolean containsAt = (source.toString().contains("@"));
            if(!containsAt){
                Pattern p = Pattern.compile(
                        "[a-zA-Z0-9\u4e00-\u9fa5，。？！：；”“（）【】｛｝《》‘’～＃、－——＿＠…,.?!:;.“”()\\]\\[}{<>‘’~#-_@ ]+");
                if (!p.matcher(source.toString()).matches()) {
                    return "";
                }
            }
            //如果按回退键
            if (source.length() < 1 && (dend - dstart >= 1)) {
                return dest.subSequence(dstart, dend - 1);
            }
            return source;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return source;
        }
    }
}
