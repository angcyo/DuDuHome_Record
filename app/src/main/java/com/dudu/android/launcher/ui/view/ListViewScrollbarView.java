package com.dudu.android.launcher.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.LogUtils;

public class ListViewScrollbarView extends View {

    private static final float HEIGHT_BG = 300;

    private static final float WIDTH = 10;

    private int color;

    private int colorBg;

    private Paint paint;

    private Paint paintBg;

    private float height = 300;

    private float startHeight = 0;

    public ListViewScrollbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arrays = context.obtainStyledAttributes(attrs, R.styleable.VehicleLine);
        color = arrays.getColor(R.styleable.VehicleLine_VehicleLineColor, getResources().getColor(R.color.vehicle_list_scroller));
        colorBg = arrays.getColor(R.styleable.VehicleLine_VehicleLineColor, getResources().getColor(R.color.vehicle_list_scroller_bg));
        arrays.recycle();
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        paintBg = new Paint();
        paintBg.setAntiAlias(true);
        paintBg.setColor(colorBg);
        paintBg.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF oval = new RectF(0, 0, WIDTH, HEIGHT_BG);// 设置个新的长方形
        canvas.drawRoundRect(oval, 0, 0, paintBg);//第二个参数是x半径，第三个参数是y半径

        RectF ovalBg = new RectF(0, startHeight, WIDTH, startHeight + height);// 设置个新的长方形
        canvas.drawRoundRect(ovalBg, 0, 0, paint);//第二个参数是x半径，第三个参数是y半径

    }

    public void setHeight(float heightPercent) {
        if (heightPercent > 1) {
            height = HEIGHT_BG;
        } else {
            height = HEIGHT_BG * heightPercent;
        }
        postInvalidate();
    }

    public void setStartHeight(float startHeightPercent) {
        startHeight = HEIGHT_BG * startHeightPercent;
        postInvalidate();
    }
}
