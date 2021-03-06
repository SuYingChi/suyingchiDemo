package com.ihs.inputmethod.uimodules.ui.sticker;

import android.content.res.AssetManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.utils.GlideURIUtils;
import com.kc.commons.configfile.KCList;
import com.kc.commons.configfile.KCMap;
import com.kc.commons.configfile.KCParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanxia on 2017/6/9.
 */

public class StickerGroup {

    private final String stickerGroupName;
    private boolean showInKeyboard = false;
    private boolean showAnimatedMark = false;
    private String downloadDisplayName;
    private String stickerGroupPreviewImageUri;
    private String stickerGroupDownloadPreviewImageUri;
    private String stickerGroupDownloadUri;
    public boolean downloadLockerToUnlock = false;
    public boolean needNewVersionToUnlock = false;
    public boolean rateToUnlock = false;
    public boolean shareToUnlock = false;
    private boolean isInternalStickerGroup = false;
    private List<Sticker> stickerList = new ArrayList<>();
    public static final String ASSETS_STICKER_FILE_NAME = "Stickers";
    private static final String STICKER_TAB_IMAGE_SUFFIX = "-tab.png";
    private static final String STICKER_DOWNLOAD_IMAGE_SUFFIX = "-preview.png";
    private static final String STICKER_DOWNLOAD_ZIP_SUFFIX = ".zip";
    // --Commented out by Inspection (18/1/11 下午2:41):private static final String STICKER_IMAGE_PNG_SUFFIX = ".png";
    private static final String STICKER_CONFIG_FILE_SUFFIX = "/contents.json";

    public StickerGroup(final String stickerGroupName) {
        this.isInternalStickerGroup = isStickerExistInAssets(stickerGroupName);
        this.stickerGroupName = stickerGroupName;

        StringBuilder stickerPreviewImageUri;
        if (isInternalStickerGroup) {
            stickerPreviewImageUri = new StringBuilder(ASSETS_STICKER_FILE_NAME)
                    .append("/").append(stickerGroupName).append("/").append(stickerGroupName).append(STICKER_TAB_IMAGE_SUFFIX);
            this.stickerGroupPreviewImageUri = GlideURIUtils.transformURI(stickerPreviewImageUri.toString(), GlideURIUtils.Type.Assets);
        } else {
            stickerPreviewImageUri = new StringBuilder(getStickerDownloadBaseUrl())
                    .append(stickerGroupName).append("/").append(stickerGroupName).append(STICKER_TAB_IMAGE_SUFFIX);
            this.stickerGroupPreviewImageUri = stickerPreviewImageUri.toString();
        }

        StringBuilder stickerDownloadPreviewUri = new StringBuilder(getStickerDownloadBaseUrl())
                .append(stickerGroupName).append("/").append(stickerGroupName).append(STICKER_DOWNLOAD_IMAGE_SUFFIX);
        this.stickerGroupDownloadPreviewImageUri = stickerDownloadPreviewUri.toString();

        StringBuilder stickerDownloadUri = new StringBuilder(getStickerDownloadBaseUrl())
                .append(stickerGroupName).append("/").append(stickerGroupName).append(STICKER_DOWNLOAD_ZIP_SUFFIX);
        this.stickerGroupDownloadUri = stickerDownloadUri.toString();

        reloadStickers();
    }

    public void reloadStickers() {
        stickerList.clear();
        KCMap configMap = getStickerConfigMap();
        if (configMap != null) {
            KCList contents = configMap.getList("contents");
            for (int i = 0; i < contents.size(); i++) {
                final Map<String, Object> contentMap = (Map<String, Object>) contents.get(i);
                final String imageName = (String) contentMap.get("imageName");
                String stickerImageUri = "";
                StringBuilder stickerImageFilePath;
                String filePath = "";
                if (isInternalStickerGroup) {
                    stickerImageFilePath = new StringBuilder(ASSETS_STICKER_FILE_NAME)
                            .append("/").append(stickerGroupName).append("/").append(imageName);
                    stickerImageUri = GlideURIUtils.transformURI(stickerImageFilePath.toString(), GlideURIUtils.Type.Assets);
                    filePath = stickerImageUri;
                } else if (isStickerGroupDownloaded()) {
                    stickerImageFilePath = new StringBuilder(getStickerFolderPath(stickerGroupName))
                            .append("/").append(imageName);
                    stickerImageUri = GlideURIUtils.transformURI(stickerImageFilePath.toString(), GlideURIUtils.Type.File);
                    filePath = stickerImageUri;
                }

                Sticker sticker = new Sticker(stickerImageUri);
                sticker.setFilePath(filePath);
                addSticker(sticker);
            }
        }
    }

    private KCMap getStickerConfigMap() {
        KCMap kcMap = null;

        FileInputStream inputStream = null;
        try {
            if (isInternalStickerGroup) { //从assets里读取
                AssetManager assetManager = HSApplication.getContext().getAssets();
                kcMap = KCParser.parseMap(assetManager.open(ASSETS_STICKER_FILE_NAME + "/" + stickerGroupName + STICKER_CONFIG_FILE_SUFFIX));
            } else if (isStickerGroupDownloaded()) {
                inputStream = new FileInputStream(getStickerFolderPath(stickerGroupName) + STICKER_CONFIG_FILE_SUFFIX);
                kcMap = KCParser.parseMap(inputStream);
            }
            if (kcMap != null) {
                return kcMap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private String getStickerRootFolderPath() { // /data/data/com.keyboard.colorkeyboard/files/Stickers
        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_STICKER_FILE_NAME;
    }

    private String getStickerFolderPath(String stickerGroupName) { // /data/data/com.keyboard.colorkeyboard/files/Stickers/emoji_modified
        return getStickerRootFolderPath() + File.separator + stickerGroupName;
    }

    private String getStickerDownloadBaseUrl() {
        return HSConfig.getString("Application", "Server", "StickerDownloadBaseURL") + "/";
    }

    private boolean isStickerExistInAssets(String stickerGroupName) {
        try {
            InputStream open = HSApplication.getContext().getAssets().open(ASSETS_STICKER_FILE_NAME + "/" + stickerGroupName + STICKER_CONFIG_FILE_SUFFIX);
            open.close();
            return true;
        } catch (IOException e) {
            HSLog.e("FileNotFoundException: " + stickerGroupName + STICKER_CONFIG_FILE_SUFFIX);
        }
        return false;
    }

    public String getStickerGroupName() {
        return stickerGroupName;
    }

    public List<Sticker> getStickerList() {
        if (stickerList == null) {
            stickerList = new ArrayList<>();

        }

        return stickerList;
    }

    public Sticker getSticker(String stickerName) {
        for (Sticker sticker : getStickerList()) {
            if (sticker.getStickerName().equals(stickerName)) {
                return sticker;
            }
        }
        return null;
    }

    public void addSticker(final Sticker sticker) {
        stickerList.add(sticker);
    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void addStickerToFirst(final Sticker sticker) {
//        stickerList.add(0, sticker);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void removeLastSticker() {
//        stickerList.remove(stickerList.size() - 1);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void removeSticker(final Sticker sticker) {
//        stickerList.remove(sticker);
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public int size() {
//        return stickerList.size();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void clearSticker() {
//        stickerList.clear();
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public String getDownloadDisplayName() {
        return downloadDisplayName;
    }

    public void setDownloadDisplayName(String downloadDisplayName) {
        this.downloadDisplayName = downloadDisplayName;
    }

    public void setShowInKeyboard(boolean showInKeyboard) {
        this.showInKeyboard = showInKeyboard;
    }

    public void setShowAnimatedMark(boolean showAnimatedMark) {
        this.showAnimatedMark = showAnimatedMark;
    }

    public boolean shouldShowAnimatedMark() {
        return showAnimatedMark;
    }

    public boolean isShowInKeyboard() {
        return showInKeyboard;
    }

    public String getStickerGroupPreviewImageUri() {
        return stickerGroupPreviewImageUri;
    }

    public String getStickerGroupDownloadPreviewImageUri() {
        return stickerGroupDownloadPreviewImageUri;
    }

    public String getStickerGroupDownloadUri() {
        return stickerGroupDownloadUri;
    }

    @Override
    public String toString() {
        return stickerGroupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StickerGroup group = (StickerGroup) o;

        return stickerGroupName != null ? stickerGroupName.equals(group.stickerGroupName) : group.stickerGroupName == null;

    }

    public boolean isStickerGroupDownloaded() {
        if (isInternalStickerGroup) {
            return true;
        } else {
            String stickerGroupFilePath = getStickerFolderPath(stickerGroupName);
            return isFileExist(stickerGroupFilePath);
        }
    }

    private boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    public boolean isInternalStickerGroup() {
        return isInternalStickerGroup;
    }

    @Override
    public int hashCode() {
        return stickerGroupName != null ? stickerGroupName.hashCode() : 0;
    }
}
