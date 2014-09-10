package com.gcscout.trackerdemo.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.gcscout.trackerdemo.Fonts;
import com.gcscout.trackerdemo.R;

public class ScalableTextView extends TextView {
	private final float mTextSizePercent;

	public ScalableTextView(Context context) {
		this(context, null);
	}

	public ScalableTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScalableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(Fonts.RobotoRegular);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScalableText, defStyle, 0);
		mTextSizePercent = a.getFloat(R.styleable.ScalableText_textSizePercent, 1f);
		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		if (heightMode != MeasureSpec.UNSPECIFIED)
			setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (mTextSizePercent * 0.8 * height));

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
