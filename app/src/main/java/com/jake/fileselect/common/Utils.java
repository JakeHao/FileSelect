package com.jake.fileselect.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.text.DecimalFormat;

/**
 * Created by yinhao on 16/7/26.
 */
public class Utils
{
    /**
     * 转换文件大小
     *
     * @param fileLength
     * @return
     */
    public static String formatFileSize(long fileLength)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileLength == 0)
        {
            return wrongSize;
        }
        if (fileLength < 1024)
        {
            fileSizeString = df.format((double)fileLength) + "B";
        }
        else if (fileLength < 1048576)
        {
            fileSizeString = df.format((double)fileLength / 1024) + "KB";
        }
        else if (fileLength < 1073741824)
        {
            fileSizeString = df.format((double)fileLength / 1048576) + "MB";
        }
        else
        {
            fileSizeString = df.format((double)fileLength / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 将dp值转换为像素值
     *
     * @param mContext Context对象
     * @param size     dp值
     * @return 像素值
     */
    public static float dpToPx(Context mContext, float size)
    {
        Resources r;
        if (mContext == null) { r = Resources.getSystem(); }
        else { r = mContext.getResources(); }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, r.getDisplayMetrics());
    }

    public static DisplayMetrics getScreenPix(Activity activity)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * open android settings screen for your app.
     */
    public static void openSettingsScreen(@NonNull Context context)
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + context.getPackageName());
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
