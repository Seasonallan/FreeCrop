package com.season.freecrop.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.season.freecrop.R;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Administrator on 2017/11/6.
 */

public class ToolBitmapCache {

    private static ToolBitmapCache defaultInstance;
    public static ToolBitmapCache getDefault() {
        if (defaultInstance == null) {
            synchronized (ToolBitmapCache.class) {
                if (defaultInstance == null) {
                    defaultInstance = new ToolBitmapCache();
                }
            }
        }
        return defaultInstance;
    }

    ToolBitmapCache(){
        bitmapHashMap = new HashMap<>();
    }

    public void release(){
        Set<String> keySet = bitmapHashMap.keySet();
        for (String key: keySet) {
            Bitmap bitmap = bitmapHashMap.get(key);
            Util.recycleBitmaps(bitmap);
        }
        bitmapHashMap.clear();
    }

    private HashMap<String, Bitmap> bitmapHashMap;
    public Bitmap getBitmapFromFile(String imagePath){
        if (bitmapHashMap.containsKey(imagePath)){
            Bitmap bitmap = bitmapHashMap.get(imagePath);
            if (bitmap != null && !bitmap.isRecycled()){
                return bitmap;
            }
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        bitmapHashMap.put(imagePath, bitmap);
        return bitmap;
    }

    private Bitmap zoom, scaleX, scaleY, close;
    public Bitmap getZoom(Context context){
        if (zoom == null || zoom.isRecycled()){
            zoom = BitmapFactory.decodeResource(context.getResources(), R.mipmap.toolview_zoom);
            zoom = Util.scaleBitmap(zoom, 50, 50);
        }
        return zoom;
    }
    public Bitmap getScaleX(Context context){
        if (scaleX == null || scaleX.isRecycled()){
            scaleX = BitmapFactory.decodeResource(context.getResources(), R.mipmap.zuoyoulashen);
            scaleX = Util.scaleBitmap(scaleX, 25, 60);
        }
        return scaleX;
    }
    public Bitmap getClose(Context context){
        if (close == null || close.isRecycled()){
            close = BitmapFactory.decodeResource(context.getResources(), R.mipmap.cha);
            close = Util.scaleBitmap(close,50, 50);
        }
        return close;
    }
}
