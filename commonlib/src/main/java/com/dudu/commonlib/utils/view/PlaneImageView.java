package com.dudu.commonlib.utils.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by dengjun on 2016/5/3.
 * Description :
 */
public class PlaneImageView extends ImageView {

    public PlaneImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleY(-1);
    }
}
