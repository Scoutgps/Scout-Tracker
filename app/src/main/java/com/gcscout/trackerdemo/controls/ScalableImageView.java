package com.gcscout.trackerdemo.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.gcscout.trackerdemo.R;

public class ScalableImageView extends ImageView {
    private final float imageHeightSizePercent;
    private final float imageWidthSizePercent;

    public ScalableImageView(Context context) {
        this(context, null);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScalableImageView, defStyle, 0);
        imageHeightSizePercent = a.getFloat(R.styleable.ScalableImageView_imageHeightSizePercent, 1f);
        imageWidthSizePercent = a.getFloat(R.styleable.ScalableImageView_imageHeightSizePercent, 1f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable img = getDrawable();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (img == null || img.getIntrinsicHeight() <= 0 || img.getIntrinsicWidth() <= 0 || widthSize <= 0 || heightSize <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int availableWidthSize = (int) (widthSize * imageWidthSizePercent);
        int availableHeightSize = (int) (heightSize * imageHeightSizePercent);
        float aspectRatio = img.getIntrinsicWidth() / (float) img.getIntrinsicHeight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED)
            setMeasuredDimension((int) (availableHeightSize * aspectRatio), availableHeightSize);
        else if (heightMode == MeasureSpec.UNSPECIFIED)
            setMeasuredDimension(availableWidthSize, (int) (availableWidthSize / aspectRatio));
        else if (availableWidthSize < availableHeightSize * aspectRatio) {
            if (heightMode == MeasureSpec.AT_MOST)
                setMeasuredDimension(availableWidthSize, (int) (availableWidthSize / aspectRatio));
            else
                setMeasuredDimension(availableWidthSize, Math.max(heightSize, (int) (availableWidthSize / aspectRatio)));
        } else if (widthMode == MeasureSpec.AT_MOST)
            setMeasuredDimension((int) (availableHeightSize * aspectRatio), availableHeightSize);
        else
            setMeasuredDimension(Math.max(widthSize, (int) (availableHeightSize * aspectRatio)), availableHeightSize);
    }
}
