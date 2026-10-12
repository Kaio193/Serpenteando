package com.serpenteando.cobra3d.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

public final class ResourceHelper {

    private ResourceHelper() {}

    public static Drawable getDrawable(Context context, String resName) {
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        if (resId != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return context.getDrawable(resId);
            } else {
                return context.getResources().getDrawable(resId);
            }
        }
        return null;
    }

    public static int getStringResId(Context context, String resName) {
        return context.getResources().getIdentifier(resName, "string", context.getPackageName());
    }

    public static int getColorResId(Context context, String resName) {
        return context.getResources().getIdentifier(resName, "color", context.getPackageName());
    }
}
