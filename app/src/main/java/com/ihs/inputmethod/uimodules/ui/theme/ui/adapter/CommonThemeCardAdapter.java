package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.theme.HSThemeNewTipController;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSToastUtils;
import com.ihs.inputmethod.theme.download.ApkUtils;
import com.ihs.inputmethod.theme.download.ThemeDownloadManager;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.constants.KeyboardActivationProcessor;
import com.ihs.inputmethod.uimodules.ui.common.adapter.AdapterDelegatesManager;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeDetailActivity;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.BlankViewAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate.ThemeCardAdapterDelegate;
import com.ihs.inputmethod.uimodules.ui.theme.ui.model.ThemeHomeModel;
import com.ihs.inputmethod.uimodules.ui.theme.utils.ThemeMenuUtils;
import com.ihs.inputmethod.uimodules.widget.TrialKeyboardDialog;
import com.keyboard.core.themes.custom.KCCustomThemeManager;

import java.io.File;
import java.util.List;


/**
 * Created by wenbinduan on 2016/12/22.
 */

public class CommonThemeCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

	public interface ThemeCardItemClickListener{
		void onCardClick(HSKeyboardTheme keyboardTheme);
		void onMenuApplyClick(HSKeyboardTheme keyboardTheme);
		void onMenuShareClick(HSKeyboardTheme keyboardTheme);
		void onMenuDownloadClick(HSKeyboardTheme keyboardTheme);
		void onMenuDeleteClick(HSKeyboardTheme keyboardTheme);
		void onMenuAppliedClick(HSKeyboardTheme keyboardTheme);
	}


	private Activity activity;
	private ThemeCardItemClickListener themeCardItemClickListener;
	protected AdapterDelegatesManager<List<ThemeHomeModel>> delegatesManager;
	private List<ThemeHomeModel> items;

	public CommonThemeCardAdapter(Activity activity, ThemeCardItemClickListener themeCardItemClickListener, boolean themeAnalyticsEnabled) {
		this.activity = activity;
		this.themeCardItemClickListener = themeCardItemClickListener;
		delegatesManager=new AdapterDelegatesManager<>();
		delegatesManager.addDelegate(new ThemeCardAdapterDelegate(themeAnalyticsEnabled,this))
				.addDelegate(new BlankViewAdapterDelegate(HSDisplayUtils.dip2px(88)));

	}

	public void setItems(List<ThemeHomeModel> items) {
		this.items = items;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return delegatesManager.onCreateViewHolder(parent,viewType);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		delegatesManager.onBindViewHolder(items,position,holder);
	}

	@Override
	public int getItemViewType(int position) {
		return delegatesManager.getItemViewType(items,position);
	}

	@Override
	public int getItemCount() {
		return items==null ? 0 : items.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewAttachedToWindow(holder);
	}

	@Override
	public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewDetachedFromWindow(holder);
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		delegatesManager.onViewRecycled(holder);
	}

	@Override
	public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
		return delegatesManager.onFailedToRecycleView(holder);
	}

	public int getSpanSize(int position){
		return delegatesManager.getSpanSize(items,position);
	}

	@Override
	public void onClick(final View v) {
		final ThemeHomeModel model= (ThemeHomeModel) v.getTag();
		final HSKeyboardTheme keyboardTheme= model.keyboardTheme;
		final int key= (int) v.getTag(R.id.theme_card_view_tag_key_action);
		final int position= (int) v.getTag(R.id.theme_card_view_tag_key_position);

		switch (key){
			case ThemeCardAdapterDelegate.TAG_DELETE:
				KCCustomThemeManager.getInstance().removeCustomTheme(keyboardTheme.getThemeId());
//				HSKeyboardThemeManager.removeCustomTheme(keyboardTheme);
				break;
			case ThemeCardAdapterDelegate.TAG_MENU:
				v.setSelected(true);
				PopupMenu popMenu = ThemeMenuUtils.createPopMenu(v, keyboardTheme);
				final KeyboardActivationProcessor procceser =
						new KeyboardActivationProcessor(activity.getClass(), new KeyboardActivationProcessor.OnKeyboardActivationChangedListener() {
							@Override
							public void activeDialogShowing() {

							}

							@Override
							public void keyboardSelected(int requestCode) {
								if (requestCode == ThemeMenuUtils.keyboardActivationFromAdapter) {
									if (!HSKeyboardThemeManager.setKeyboardTheme(keyboardTheme.mThemeName)) {
										String failedString = HSApplication.getContext().getResources().getString(R.string.theme_apply_failed);
										HSToastUtils.toastCenterLong(String.format(failedString, keyboardTheme.getThemeShowName()));
										return;
									}

									TrialKeyboardDialog.sendShowTrialKeyboardDialogNotification(activity.getClass().getSimpleName(), requestCode);
									if (themeCardItemClickListener != null) {
										themeCardItemClickListener.onMenuApplyClick(keyboardTheme);
									}
								}

							}

							@Override
							public void activeDialogCanceled() {

							}

							@Override
							public void activeDialogDismissed() {

							}
						});

				popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						setThemeNotNew(keyboardTheme);
						notifyItemChanged(position);

						final CharSequence title = item.getTitle();
						if (TextUtils.isEmpty(title)) {
							return false;
						}
						if (HSApplication.getContext().getString(R.string.theme_card_menu_download).equals(title)) {
							if (keyboardTheme.getThemePkName() != null) {
								item.setTitle(R.string.theme_card_menu_downloading);
								item.setEnabled(false);
								boolean shouldDownloadThemeAPK = ThemeDownloadManager.getInstance().downloadTheme(keyboardTheme);
								if(shouldDownloadThemeAPK){
									Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.theme_card_downloading_tip), Toast.LENGTH_SHORT).show();
								}
							}
							if (themeCardItemClickListener != null) {
								themeCardItemClickListener.onMenuDownloadClick(keyboardTheme);
							}
						} else if (HSApplication.getContext().getString(R.string.theme_card_menu_delete).equals(title)) {
							HSKeyboardThemeManager.removeCustomTheme(keyboardTheme);
							if (themeCardItemClickListener != null) {
								themeCardItemClickListener.onMenuDeleteClick(keyboardTheme);
							}
						} else if (HSApplication.getContext().getString(R.string.theme_card_menu_share).equals(title)) {
							ThemeMenuUtils.shareTheme(activity, keyboardTheme);
							if (themeCardItemClickListener != null) {
								themeCardItemClickListener.onMenuShareClick(keyboardTheme);
							}
						} else if (HSApplication.getContext().getString(R.string.theme_card_menu_apply).equals(title)) {
							if(keyboardTheme.getThemeType() == HSKeyboardTheme.ThemeType.DOWNLOADED && !HSInstallationUtils.isAppInstalled(keyboardTheme.getThemePkName())) {
								ApkUtils.startInstall(HSApplication.getContext(), Uri.fromFile(new File(ThemeDownloadManager.getThemeDownloadLocalFile(keyboardTheme.mThemeName))));
							}else {
								procceser.activateKeyboard(activity,true, ThemeMenuUtils.keyboardActivationFromAdapter);
							}

						} else if (HSApplication.getContext().getString(R.string.theme_card_menu_applied).equals(title)) {
							if (themeCardItemClickListener != null) {
								themeCardItemClickListener.onMenuAppliedClick(keyboardTheme);
							}
						}
						v.setSelected(false);
						return true;
					}
				});
				popMenu.show();
				break;
			case ThemeCardAdapterDelegate.TAG_CARD:
				if(model.deleteEnable){
					break;
				}
				Intent intent = new Intent(activity, ThemeDetailActivity.class);
				intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, keyboardTheme.mThemeName);
				activity.startActivity(intent);
				if (themeCardItemClickListener != null) {
					themeCardItemClickListener.onCardClick(keyboardTheme);
				}
				setThemeNotNew(keyboardTheme);
				notifyItemChanged(position);
				break;
			default:
				break;
		}
	}


	private void setThemeNotNew(HSKeyboardTheme keyboardTheme) {
		if(keyboardTheme.isNewTheme()) {
			HSThemeNewTipController.getInstance().setThemeNotNew(keyboardTheme.mThemeName);
		}
	}
}
