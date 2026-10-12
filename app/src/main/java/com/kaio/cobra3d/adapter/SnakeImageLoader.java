package com.kaio.cobra3d.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public final class SnakeImageLoader {

    private SnakeImageLoader() {
    }

    public static Bitmap load(Context context, String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return null;
        }
        AssetManager am = context.getAssets();
        String[] candidates = {
                "images/" + imageName + ".png",
                "images/" + imageName + ".jpg",
                "images/" + imageName + ".jpeg"
        };
        for (String path : candidates) {
            try (InputStream is = am.open(path)) {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (bmp != null) {
                    return bmp;
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }
}