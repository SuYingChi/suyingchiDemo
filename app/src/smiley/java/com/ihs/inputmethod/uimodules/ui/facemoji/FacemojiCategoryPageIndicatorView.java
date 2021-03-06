package com.ihs.inputmethod.uimodules.ui.facemoji;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FacemojiCategoryPageIndicatorView extends View {
	private static final float BOTTOM_MARGIN_RATIO = 1.0f;
	private final Paint mPaint = new Paint();
	private int mCategoryPageSize = 0;
	private int mCurrentCategoryPageId = 0;
	private float mOffset = 0.0f;

	public FacemojiCategoryPageIndicatorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FacemojiCategoryPageIndicatorView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void setColors(final int foregroundColor, final int backgroundColor) {
//		mPaint.setColor(foregroundColor);
//		setBackgroundColor(backgroundColor);
//		if(foregroundColor+backgroundColor==0){
//			mPaint.setColor(Color.parseColor("#BBFFFFFF"));
//			setBackgroundColor(Color.parseColor("#33FFFFFF"));
//		}
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)
// --Commented out by Inspection START (18/1/11 下午2:41):
//	public void setCategoryPageId(final int size, final int id, final float offset) {
//		mCategoryPageSize = size;
//		mCurrentCategoryPageId = id;
//		mOffset = offset;
//		invalidate();
//	}
// --Commented out by Inspection STOP (18/1/11 下午2:41)

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mCategoryPageSize <= 1) {
			canvas.drawColor(0);
			return;
		}
		final float height = getHeight();
		final float width = getWidth();
		final float unitWidth = width / mCategoryPageSize;
		final float left = unitWidth * mCurrentCategoryPageId + mOffset * unitWidth;
		final float top = 0.0f;
		final float right = left + unitWidth;
		final float bottom = height * BOTTOM_MARGIN_RATIO;
		canvas.drawRect(left, top, right, bottom, mPaint);
	}
}