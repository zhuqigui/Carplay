package com.third.zoom.common.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import com.third.zoom.common.utils.KeyEventUtils;
import com.third.zoom.common.utils.PreferenceUtils;

public class MediaListView extends ListView {
    public MediaListView(Context context) {
        super(context);
    }

    public MediaListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
//        int lastSelectItem = getSelectedItemPosition();
//        Log.i("zhu_test","MediaListView onFocusChanged lastSelectItem=="+lastSelectItem+",gainFocus=="+gainFocus);
//        if(lastSelectItem==-1){
//            lastSelectItem= PreferenceUtils.getInt("play_list_position",0);
//            //模拟发送方向下键，第一次设置不了选中样式
//            //KeyEventUtils.sendKeyEvent(20);
//            //Log.i("zhu_test","MediaListView KeyEventUtils.sendKeyEvent(20)..");
//        }
//        if (gainFocus) {
//            //setItemChecked(lastSelectItem,true);
//            Log.i("zhu_test","MediaListView setItemChecked lastSelectItem");
//            setSelection(lastSelectItem);
//        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
}
