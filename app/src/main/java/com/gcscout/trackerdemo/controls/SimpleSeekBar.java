package com.gcscout.trackerdemo.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.gcscout.trackerdemo.R;

public class SimpleSeekBar extends SeekBar {
    private int thumbWidth;
    private Drawable thumbDrawable;
    private Drawable bgDrawable;
    private Drawable progressDrawable;

    private double getProgressPercent() {
        return (double) getProgress() / getMax();
    }

    public SimpleSeekBar(Context context) {
        this(context, null);
    }

    public SimpleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        thumbDrawable = getResources().getDrawable(R.drawable.settings_track_thumb);
        bgDrawable = getResources().getDrawable(R.drawable.settings_track_bg);
        progressDrawable = getResources().getDrawable(R.drawable.settings_track_progress);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            width = Math.max(Math.max(thumbDrawable.getIntrinsicWidth(), bgDrawable.getIntrinsicWidth()),
                    progressDrawable.getIntrinsicWidth());
            if (widthMode == MeasureSpec.AT_MOST)
                width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height = Math.max(Math.max(thumbDrawable.getIntrinsicHeight(), bgDrawable.getIntrinsicHeight()),
                    progressDrawable.getIntrinsicHeight());
            if (heightMode == MeasureSpec.AT_MOST)
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }

        thumbWidth = (int) (thumbDrawable.getIntrinsicWidth() * (height / (double) thumbDrawable
                .getIntrinsicHeight()));

        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        int thumbPosition = (int) ((getMeasuredWidth() - thumbWidth) * getProgressPercent());

        progressDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        bgDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        thumbDrawable.setBounds(thumbPosition, 0, thumbPosition + thumbWidth, getMeasuredHeight());

        bgDrawable.draw(canvas);

        canvas.save();
        canvas.clipRect(0, 0, thumbPosition + thumbWidth / 2, getMeasuredHeight());
        progressDrawable.draw(canvas);
        canvas.restore();

        thumbDrawable.draw(canvas);
    }
}