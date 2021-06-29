package com.vhall.uilibs.interactive.doc;

import android.text.TextUtils;

import com.vhall.uilibs.R;

//文档类型
public enum EnumDoc {

    DOC("doc", R.mipmap.icon_word),
    DOCX("docx",R.mipmap.icon_word),
    PPT("ppt",R.mipmap.icon_ppt),
    PPTX("pptx",R.mipmap.icon_ppt),
    XLSX("xlsx",R.mipmap.icon_xlsx),
    XLS("xls",R.mipmap.icon_xlsx),
    PDF("xls",R.mipmap.icon_pdf),
    ;
    public String key;
    public int icon;

    EnumDoc(String key, int icon) {
        this.key = key;
        this.icon = icon;
    }


    public static EnumDoc parseDoc(String type){
        for (EnumDoc doc:EnumDoc.values()){
            if(doc.key.equalsIgnoreCase(type)){
                return doc;
            }
        }
        return null;
    }
}
