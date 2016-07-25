package com.dudu.aios.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dudu.aios.ui.utils.customFontUtils.DINLightFontTextView;
import com.dudu.aios.ui.utils.customFontUtils.FZLFontTextView;
import com.dudu.android.launcher.R;

/**
 * Created by robi on 2016-04-15 14:38.
 */
public class DuduChooseButton extends LinearLayout implements View.OnClickListener {

    boolean isChoose = false;//按钮状态切换标识符
    OnChooseListener chooseListener;
    private Drawable buttonImage;
    private Drawable buttonChooseImage;//选中后的图片
    private String fzlText;
    private String dinlText;
    private float fzlTextSize = 7f;
    private float dinlTextSize = 5f;
    private float lineSpace1 = 3f;//第一行的空隙
    private float lineSpace2 = 2f;//第二空的空隙
    private ImageView imageView;
    private FZLFontTextView fzlTextView;
    private DINLightFontTextView dinlTextView;
    private int chooseColor;
    private int defaultColor;

    public DuduChooseButton(Context context) {
        this(context, null);
    }

    public DuduChooseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        fzlTextSize = toSp(getResources(), fzlTextSize);
        dinlTextSize = toSp(getResources(), dinlTextSize);
        lineSpace1 = toDp(getResources(), lineSpace1);
        lineSpace2 = toDp(getResources(), lineSpace2);

        defaultColor = getResources().getColor(R.color.unchecked_textColor);
        chooseColor = getResources().getColor(R.color.checked_textColor);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DuduChooseButton);
        buttonImage = typedArray.getDrawable(R.styleable.DuduChooseButton_buttonImage);
        buttonChooseImage = typedArray.getDrawable(R.styleable.DuduChooseButton_buttonChooseImage);
        dinlText = typedArray.getString(R.styleable.DuduChooseButton_dinlFontText);
        fzlText = typedArray.getString(R.styleable.DuduChooseButton_fzlFontText);
        fzlTextSize = typedArray.getDimension(R.styleable.DuduChooseButton_fzlFontTextSize, fzlTextSize);
        dinlTextSize = typedArray.getDimension(R.styleable.DuduChooseButton_dinlFontTextSize, dinlTextSize);
        isChoose = typedArray.getBoolean(R.styleable.DuduChooseButton_isChoose, isChoose);
        typedArray.recycle();
        init();
    }

    public static float toDp(Resources resources, float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, resources.getDisplayMetrics());
    }

    public static float toSp(Resources resources, float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, resources.getDisplayMetrics());
    }


    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOnClickListener(this);

        imageView = new ImageView(getContext());
        imageView.setClickable(false);

        fzlTextView = new FZLFontTextView(getContext());
        fzlTextView.setText(fzlText);
        fzlTextView.setTextSize(fzlTextSize);

        dinlTextView = new DINLightFontTextView(getContext());
        dinlTextView.setText(dinlText);
        dinlTextView.setTextSize(dinlTextSize);

        addView(imageView, generateLayoutParams(0));
        addView(fzlTextView, generateLayoutParams((int) lineSpace1));
        addView(dinlTextView, generateLayoutParams((int) lineSpace2));

        updateState();
    }

    private LayoutParams generateLayoutParams(int topOffset) {
        LayoutParams params = new LayoutParams(-2, -2);
        params.setMargins(0, topOffset, 0, 0);
        return params;
    }


    public void setChoose(boolean choose) {
        isChoose = choose;
        updateState();
    }


    public void setChooseListener(OnChooseListener listener) {
        this.chooseListener = listener;
    }

    private void updateState() {
        if (isChoose) {
            imageView.setImageDrawable(buttonChooseImage);
            fzlTextView.setTextColor(chooseColor);
            dinlTextView.setTextColor(chooseColor);
        } else {
            imageView.setImageDrawable(buttonImage);
            fzlTextView.setTextColor(defaultColor);
            dinlTextView.setTextColor(defaultColor);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("choose", isChoose);
        bundle.putParcelable("state", super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        isChoose = bundle.getBoolean("choose");
        super.onRestoreInstanceState(bundle.getParcelable("state"));

        updateState();
    }

    @Override
    public void onClick(View v) {
        isChoose = !isChoose;
        if (chooseListener != null) {
            chooseListener.onChoose(this, isChoose);
        }
        updateState();
    }

    public interface OnChooseListener {
        void onChoose(View view, boolean choose);
    }
}
