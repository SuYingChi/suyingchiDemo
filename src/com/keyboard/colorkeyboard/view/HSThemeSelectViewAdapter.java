package com.keyboard.colorkeyboard.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.extended.api.HSKeyboard;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsEvent;
import com.ihs.inputmethod.extended.eventrecorder.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.extended.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.keyboard.KeyboardTheme;
import com.keyboard.colorkeyboard.R;

public class HSThemeSelectViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private String[] mThemes;
    private HSThemeSelectView mParentView;
    private Drawable mItemDefaultBackground;
    private int mCurrentThemeId;
    private Animator mAnimator;

    public HSThemeSelectViewAdapter(final Context context, final View parentView) {
        mInflater = LayoutInflater.from(context);
        mParentView = (HSThemeSelectView) parentView;
        // Set data
        mThemes = HSKeyboardThemeManager.getThemeNames();
        mItemDefaultBackground = mParentView.getItemDefaultBackground();
        mCurrentThemeId = HSKeyboardThemeManager.getCurrentTheme().mThemeId;
    }

    @Override
    public int getCount() {
        if (mThemes.length > 0) {
            return (int) (Math.ceil(mThemes.length / 2.0f));
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.theme_select_listview_item, null);

            holder = new ViewHolder();

            holder.themeRegionLeft = (FrameLayout) convertView.findViewById(R.id.rl_theme_left);
            holder.themeRegionRight = (FrameLayout) convertView.findViewById(R.id.rl_theme_right);
            holder.themePreviewLeft = (ImageView) convertView.findViewById(R.id.iv_theme_preview_left);
            holder.themePreviewRight = (ImageView) convertView.findViewById(R.id.iv_theme_preview_right);
            holder.themePreviewLeftPick = (ImageView) convertView.findViewById(R.id.iv_theme_preview_left_selected);
            holder.themePreviewRightPick = (ImageView) convertView.findViewById(R.id.iv_theme_preview_right_selected);
            holder.themeRegionLeft.setBackgroundDrawable(mItemDefaultBackground);
            holder.themeRegionRight.setBackgroundDrawable(mItemDefaultBackground);
            convertView.setBackgroundColor(mParentView.getItemDividerColor());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // left font
        final int fontLeftIndex = position * 2;
        holder.themePreviewLeft.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(fontLeftIndex));
        holder.themeRegionLeft.setOnClickListener(new FrameLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTheme(fontLeftIndex, holder.themePreviewLeftPick);
            }
        });


        // right font
        final int fontRightIndex = fontLeftIndex + 1;
        if (fontRightIndex < mThemes.length) {
            holder.themePreviewRight.setImageDrawable(HSKeyboardThemeManager.getThemePreviewDrawable(fontRightIndex));
            holder.themeRegionRight.setOnClickListener(new FrameLayout.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTheme(fontRightIndex, holder.themePreviewRightPick);
                }
            });
        } else {
            holder.themePreviewRight.setImageDrawable(null);
            holder.themePreviewRightPick.setVisibility(View.GONE);
            holder.themeRegionRight.setOnClickListener(null);
        }


        updateViews(fontLeftIndex, holder.themePreviewLeftPick);
        updateViews(fontRightIndex, holder.themePreviewRightPick);
        return convertView;
    }

    private void onClickTheme(final int index, final View view) {
        if (mAnimator != null) {
            return;
        }
        mCurrentThemeId = index;
        view.setVisibility(View.VISIBLE);
        mAnimator = createAnimator(view);
        mAnimator.start();
        KeyboardTheme.saveKeyboardThemeId(String.valueOf(index), PreferenceManager.getDefaultSharedPreferences(HSApplication.getContext()));
        HSGoogleAnalyticsUtils.sendEvent(HSGoogleAnalyticsEvent.GA_EVENT_THEME_CHOSED, KeyboardTheme.getKeyboardThemeName(index));
        notifyDataSetChanged();
    }

    private Animator createAnimator(final View view) {
        final Animator animator;

        animator = createDelayedAnimator();

        animator.setTarget(view);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                HSKeyboard.getInstance().updateKeyboardTheme();
                view.clearAnimation();
                mAnimator = null;
            }
        });

        return animator;
    }

    private Animator createDelayedAnimator() {
        ValueAnimator localValueAnimator = ValueAnimator.ofFloat(1.0f, 1.0f);
        localValueAnimator.setDuration(10L);
        return localValueAnimator;
    }

    private void updateViews(final int index, final View selectedBg) {
        if (index >= mThemes.length) {
            return;
        }
        final String currentTheme = HSKeyboardThemeManager.getCurrentThemeName();
        if (!mThemes[index].equals(currentTheme)) {
            selectedBg.setVisibility(View.GONE);
        } else {
            selectedBg.setVisibility(View.VISIBLE);
        }
    }

    /* 存放控件 */
    final class ViewHolder {
        public FrameLayout themeRegionLeft;
        public ImageView themePreviewLeft;
        public ImageView themePreviewLeftPick;
        public FrameLayout themeRegionRight;
        public ImageView themePreviewRight;
        public ImageView themePreviewRightPick;
    }

    public void cancelAnimation() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }
}
