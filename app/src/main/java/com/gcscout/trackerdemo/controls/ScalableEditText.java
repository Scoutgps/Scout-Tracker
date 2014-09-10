package com.gcscout.trackerdemo.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import com.gcscout.trackerdemo.Fonts;
import com.gcscout.trackerdemo.R;

public class ScalableEditText extends EditText {
    private float textSizePercent = 1f;

    public ScalableEditText(Context context) {
        super(context);
        setTypeface(Fonts.RobotoRegular);
    }

    public ScalableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Fonts.RobotoRegular);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScalableText, 0, 0);
        textSizePercent = a.getFloat(R.styleable.ScalableText_textSizePercent, 1f);
        a.recycle();
    }

    public ScalableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(Fonts.RobotoRegular);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScalableText, defStyle, 0);
        textSizePercent = a.getFloat(R.styleable.ScalableText_textSizePercent, 1f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode != MeasureSpec.UNSPECIFIED)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (textSizePercent * 0.8 * height));

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
