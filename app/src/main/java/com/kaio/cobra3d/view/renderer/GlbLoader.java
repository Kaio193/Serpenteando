package com.kaio.cobra3d.view.renderer;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.filament.Engine;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.ResourceLoader;
import com.google.android.filament.gltfio.UbershaderProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GlbLoader {
    private static final String TAG = "GlbLoader";

    public static FilamentAsset load(Engine engine, AssetManager androidAssets, String path) {
        byte[] data;
        try {
            InputStream is = androidAssets.open(path);
            data = new byte[is.available()];
            int read = 0;
            while (read < data.length) {
                int n = is.read(data, read, data.length - read);
                if (n < 0) break;
                read += n;
            }
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read asset: " + path, e);
            return null;
        }

        AssetLoader loader;
        FilamentAsset asset;
        try {
            UbershaderProvider matProvider = new UbershaderProvider(engine);
            loader = new AssetLoader(engine, matProvider, com.google.android.filament.EntityManager.get());
            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length)
                .order(ByteOrder.nativeOrder());
            buffer.put(data);
            buffer.flip();

            asset = loader.createAsset(buffer);
        } catch (Exception e) {
            Log.e(TAG, "AssetLoader failed for " + path, e);
            return null;
        }

        if (asset == null) {
            Log.e(TAG, "createAsset returned null for " + path);
            return null;
        }

        try {
            ResourceLoader resLoader = new ResourceLoader(engine, true);
            resLoader.loadResources(asset);
        } catch (Exception e) {
            Log.w(TAG, "ResourceLoader failed, asset may be incomplete", e);
        }

        Log.d(TAG, "Loaded " + path + " with " + asset.getEntities().length + " entities");
        return asset;
    }
}
