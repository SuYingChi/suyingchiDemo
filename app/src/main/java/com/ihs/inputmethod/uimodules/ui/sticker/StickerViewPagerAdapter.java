package com.ihs.inputmethod.uimodules.ui.sticker;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.StickerHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.LockedCardActionUtils;
import com.ihs.inputmethod.utils.DownloadUtils;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils.STICKER_DOWNLOAD_ZIP_SUFFIX;


/**
 * Created by yanxia on 2017/6/8.
 */

public class StickerViewPagerAdapter extends PagerAdapter {
    private final String CLICK_FROM = "keyboard";
    private View firstView;
    private int lastDownloadPosition = -1;
    private List<StickerGroup> needDownloadStickerGroupList = new ArrayList<>();
    private LayoutInflater inflater;
    public StickerViewPagerAdapter(View firstView) {
        inflater = LayoutInflater.from(HSApplication.getContext());
        this.firstView = firstView;
    }

    public void setNeedDownloadStickerGroupList(List<StickerGroup> needDownloadStickerGroupList) {
        if (needDownloadStickerGroupList != null) {
            this.needDownloadStickerGroupList.clear();
            this.needDownloadStickerGroupList.addAll(needDownloadStickerGroupList);
            this.notifyDataSetChanged();
        }
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return needDownloadStickerGroupList.size() + 1;
    }

    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
     * required for a PagerAdapter to function properly.
     *
     * @param view   Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     * @return true if <code>view</code> is associated with the key object <code>object</code>
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Remove a page for the given position.  The adapter is responsible
     * for removing the view from its container, although it only must ensure
     * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by
     *                  {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * Create the page for the given position.  The adapter is responsible
     * for adding the view to the container given here, although it only
     * must ensure this is done by the time it returns from
     * {@link #finishUpdate(ViewGroup)}.
     *
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not
     * need to be a View, but can be some other container of the page.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position != 0 && !needDownloadStickerGroupList.isEmpty()) {
            StickerDownloadView stickerDownloadView = (StickerDownloadView) inflater.inflate(R.layout.common_sticker_panel_need_download_page, null);
            final StickerGroup stickerGroup = needDownloadStickerGroupList.get(position - 1);
            stickerDownloadView.setStickerGroup(stickerGroup);
            final ImageView sticker_download_preview = stickerDownloadView.findViewById(R.id.sticker_download_preview_image);
            final TextView stickerDownloadShowName = stickerDownloadView.findViewById(R.id.sticker_download_show_name);
            stickerDownloadShowName.setText(stickerGroup.getDownloadDisplayName());
            int padding = HSDisplayUtils.dip2px(40);
            sticker_download_preview.setPadding(padding, padding, padding, padding);
            Glide.with(HSApplication.getContext()).load(stickerGroup.getStickerGroupDownloadPreviewImageUri()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    sticker_download_preview.setPadding(0, 0, 0, 0);
                    return false;
                }
            }).into(sticker_download_preview);
            TextView downloadButton = stickerDownloadView.findViewById(R.id.sticker_download_button);
            StickerHomeModel  stickerHomeModel = new StickerHomeModel();
            stickerHomeModel.stickerGroup = stickerGroup;
            if (LockedCardActionUtils.shouldLock(stickerHomeModel)) {
                downloadButton.setText(R.string.theme_card_menu_unlock_for_free);
            }
            downloadButton.setOnClickListener(v -> {
                if (ClickUtils.isFastDoubleClick()) {
                    return;
                }
                if (LockedCardActionUtils.shouldLock(stickerHomeModel)) {
                    LockedCardActionUtils.handleLockAction(v.getContext(),LockedCardActionUtils.LOCKED_CARD_FROM_KEYBOARD_STICKER, stickerHomeModel, null);
                } else {
                    KCAnalytics.logEvent("sticker_download_clicked", "stickerGroupName", stickerGroup.getStickerGroupName(), "form", CLICK_FROM);
                    final String stickerGroupName = stickerGroup.getStickerGroupName();
                    final String stickerGroupDownloadedFilePath = StickerUtils.getStickerFolderPath(stickerGroupName) + STICKER_DOWNLOAD_ZIP_SUFFIX;
                    DownloadUtils.getInstance().startForegroundDownloading(HSApplication.getContext(), stickerGroupName,
                            stickerGroupDownloadedFilePath, stickerGroup.getStickerGroupDownloadUri(),
                            sticker_download_preview.getDrawable(), (success, manually) -> {
                                HSPreferenceHelper.getDefault().putBoolean(KeyboardPanelManager.SHOW_EMOJI_PANEL, true);
                                if (success) {
                                    lastDownloadPosition = position;
                                    KCAnalytics.logEvent("sticker_download_succeed", "stickerGroupName", stickerGroupName, "from", CLICK_FROM);
                                    StickerDownloadManager.getInstance().unzipStickerGroup(stickerGroupDownloadedFilePath, stickerGroup);
                                }
                            }, false);
                }

            });
            stickerDownloadView.setTag(position);
            container.addView(stickerDownloadView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return stickerDownloadView;
        } else {
            firstView.setTag(position);
            container.addView(firstView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return firstView;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if (lastDownloadPosition == 1) {
            return POSITION_NONE;
        } else {
            return POSITION_UNCHANGED;
        }
    }
}
