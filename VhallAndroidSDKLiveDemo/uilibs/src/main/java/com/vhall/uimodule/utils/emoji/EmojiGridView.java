package com.vhall.uimodule.utils.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class EmojiGridView extends GridView {
	public EmojiGridView(Context context) {
		super(context);
	}
	
	public EmojiGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
