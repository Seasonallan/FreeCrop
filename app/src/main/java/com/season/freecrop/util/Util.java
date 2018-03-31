package com.season.freecrop.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * CreateAt : 7/12/17
 * Describe :
 *
 * @author chendong
 */
public class Util {

    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double d2 = dx * dx + dy * dy;
        return Math.sqrt(d2);
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float d2 = dx * dx + dy * dy;
        return ((float) Math.sqrt(d2));
    }
    /**
     */
    public static float getRotationBetweenLines(float centerX, float centerY, float xInView, float yInView) {
        float rotation = 0;

        float k1 =   (centerY - centerY) / (centerX * 2 - centerX);
        float k2 =   (yInView - centerY) / (xInView - centerX);
        float tmpDegree = (float) (Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180);

        if (xInView > centerX && yInView < centerY) {
            rotation = 90 - tmpDegree;
        } else if (xInView > centerX && yInView > centerY)
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < centerX && yInView > centerY) {
            rotation = 270 - tmpDegree;
        } else if (xInView < centerX && yInView < centerY) {
            rotation = 270 + tmpDegree;
        } else if (xInView == centerX && yInView < centerY) {
            rotation = 0;
        } else if (xInView == centerX && yInView > centerY) {
            rotation = 180;
        }

        return rotation;
    }

    public static boolean isTouchPointInPath(Path path, int x, int y) {
        if (path == null) {
            return false;
        }
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }

    /**
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, float width, float height)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
//        Logger.d("w:"+w+",h:"+h);
        Matrix matrix = new Matrix();
        float scale = width / w;
        float scale2 = height / h;
        scale = scale < scale2 ? scale : scale2;
        matrix.postScale(scale, scale);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return bmp;
    }

    public static Bitmap cutBitmap(Bitmap bitmap, float width, float height, float x, float y)
    {
        return cutBitmap(bitmap, width, height, x, y, true);
    }

    public static String getFileTri(String filePath){
        try{
            String[] token = filePath.split("\\.");
            return token[token.length - 1];
        }catch (Exception e){
            try{
                if (filePath.endsWith("gif")){
                    return "gif";
                }
                if (filePath.endsWith("jpg")){
                    return "jpg";
                }
            }catch (Exception e2){
                e2.printStackTrace();
            }
            e.printStackTrace();
        }
        return "png";
    }

    /**
     * 复制单个文件
     * @return boolean
     */
    public static boolean copyFile(String srcFilePath, String desFilePath) {
        try {
            File srcFile = new File(srcFilePath);
            if (srcFile.exists()) {
                InputStream inStream = new FileInputStream(srcFile);
                FileOutputStream fs = new FileOutputStream(desFilePath);
                byte[] buffer = new byte[1024];
                int in = 0;
                while ( (in = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, in);
                }
                inStream.close();
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //保存图片
    public static String saveBitmap(File output, Bitmap bitmap) {
        try {
            FileOutputStream fos = new FileOutputStream(output);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return output.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap cutBitmap(Bitmap bitmap, float width, float height, float x, float y, boolean recycle)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (x > w){
            x = 0;
        }
        if (y > h){
            y = 0;
        }
        if (width + x > w){
            width = w - x;
        }
        if (height + y > h){
            height = h - y;
        }
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) width, (int) height);
        if (recycle)
            bitmap.recycle();
        return bitmapResult;
    }

    /**
     * Color对象转换成字符串
     *
     * @return 16进制颜色字符串
     */
    private static String toHexFromColor(int red, int green, int blue) {
        String r, g, b;
        StringBuilder su = new StringBuilder();
        r = Integer.toHexString(red);
        g = Integer.toHexString(green);
        b = Integer.toHexString(blue);
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        r = r.toUpperCase();
        g = g.toUpperCase();
        b = b.toUpperCase();
        //  su.append("0xFF");
        su.append(r);
        su.append(g);
        su.append(b);
        //0xFF0000FF
        return su.toString();
    }

    public static boolean isColorEqualWithoutAlpha(int color, int color2){
        try {
            String preColor = getColorStr(color);
            String proColor = getColorStr(color2);
            return preColor.equals(proColor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getColorStr(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return toHexFromColor(red, green, blue);
    }

    public static int getColor(String textcolor, int defaultColor) {
        try {
            if (textcolor.startsWith("#")) {
                return Color.parseColor(textcolor);
            } else {
                try {
                    return Color.parseColor("#" + textcolor);
                } catch (Exception e) {
                     e.printStackTrace();
                    return Integer.parseInt(textcolor);
                }
            }
        } catch (Exception e) {
               e.printStackTrace();
            return defaultColor;
        }
    }

    public static void recycleBitmaps(Bitmap... bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            try {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFileType(String path){
        if (TextUtils.isEmpty(path)){
            return "unknow";
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        String type = options.outMimeType;
        if (TextUtils.isEmpty(type)) {
            type = "unknow";
        } else {
            type = type.substring(6, type.length());
        }
        return type;
    }

    public static boolean isWebp(String type){
        return type.equalsIgnoreCase("webp");
    }

    public static boolean isGif(String type){
        return type.equalsIgnoreCase("gif");
    }


}
