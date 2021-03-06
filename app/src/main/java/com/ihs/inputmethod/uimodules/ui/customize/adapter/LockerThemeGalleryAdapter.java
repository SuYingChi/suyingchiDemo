package com.ihs.inputmethod.uimodules.ui.customize.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ihs.feature.common.ViewUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.InCallThemePreviewActivity;
import com.ihs.inputmethod.uimodules.ui.customize.service.ICustomizeService;
import com.ihs.inputmethod.uimodules.ui.customize.service.ServiceListener;
import com.ihs.inputmethod.uimodules.ui.theme.ui.ThemeHomeActivity;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;


/**
 * Created by guonan.lv on 17/9/6.
 */

public class LockerThemeGalleryAdapter extends RecyclerView.Adapter<LockerThemeGalleryAdapter.ViewHolder>
        implements View.OnClickListener, ServiceListener {

    private static final int ITEM_TYPE_THEME_VIEW = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Type> mThemes = new ArrayList<>();

    public LockerThemeGalleryAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        populateData();
    }

    public void populateData() {
        mThemes.clear();
        mThemes = Type.values();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mThemes.size()-1;
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE_THEME_VIEW;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View grid = mInflater.inflate(R.layout.locker_theme_gallery_item, parent, false);
        ThemeViewHolder themeHolder = new ThemeViewHolder(grid);
        themeHolder.itemView.setOnClickListener(this);
        return themeHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int themeIndex = position;
        final ThemeViewHolder themeHolder = (ThemeViewHolder) holder;
        holder.itemView.setTag(themeIndex);
        final Type themeType = mThemes.get(themeIndex);

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.locker_theme_thumbnail_loading)
                .error(R.drawable.locker_theme_thumbnail_failed).diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(mContext).asBitmap().apply(requestOptions)
                .load(themeType.getPreviewImage()).transition(withCrossFade(500))
                .into(themeHolder.themeThumbnail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grid_root:
                int pos = (int) v.getTag();
                Type themeType = mThemes.get(pos);
                if (mContext instanceof ThemeHomeActivity) {
                    Intent intent = new Intent(mContext, InCallThemePreviewActivity.class);
                    intent.putExtra("CallThemeType", themeType);
                    mContext.startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {
        populateData();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ThemeViewHolder extends ViewHolder {
        ImageView themeThumbnail;

        public ThemeViewHolder(View itemView) {
            super(itemView);
            themeThumbnail = ViewUtils.findViewById(itemView, R.id.theme_thumbnail);
        }
    }

    public static class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {

        public GridSpanSizer(LockerThemeGalleryAdapter adapter) {
            super();
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            return 1;
        }
    }
}
