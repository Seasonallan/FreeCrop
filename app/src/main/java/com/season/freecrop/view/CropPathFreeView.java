package com.season.freecrop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.view.MotionEvent;

import com.season.freecrop.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
* 自由裁剪
* @author season
* created at 2018/3/31 7:50
*/
public class CropPathFreeView extends CropTool{


    private Paint paintPath = new Paint();
    private Paint paintPathCircle = new Paint();

    public Path path;
    private Paint paintCrop = new Paint();

    CropPathFreeView(Context context, int width, int height) {
        super(context, width, height);
        //matrix = new Matrix();
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeWidth(2);
        paintPath.setColor(Color.parseColor("#ffff0000"));
        paintPath.setAntiAlias(true);
        paintPathCircle.setAntiAlias(true);

        pathList = new ArrayList<>();
        path = new Path();
        //path.setFillType(Path.FillType.WINDING);
        paintResult.setAntiAlias(true);
        paintCrop.setAntiAlias(true);
        paintCrop.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        cutOprationUndos = new ArrayList<>();
        cutOprations = new ArrayList<>();
    }

    private float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = Float.MIN_VALUE, bottom = Float.MIN_VALUE;

    private void checkPoint(float x, float y) {
        left = Math.min(left, x);
        right = Math.max(right, x);
        top = Math.min(top, y);
        bottom = Math.max(bottom, y);
    }

    @Override
    public boolean canBack() {
        return pathList.size() == 0;
    }

    @Override
    public boolean canPro(){
        return cutOprationUndos.size() > 0;
    }
    @Override
    public boolean canPre(){
        return cutOprations.size() > 0;
    }
    @Override
    public void redo(){
        if (canPro()){
            CutFreeOpration opration = cutOprationUndos.remove(cutOprationUndos.size() - 1);
            cutOprations.add(opration);
            reset(opration.points);
        }
    }
    @Override
    public void undo(){
        if (canPre()){
            CutFreeOpration opration = cutOprations.remove(cutOprations.size() - 1);
            cutOprationUndos.add(opration);
            if (cutOprations.size() > 0){
                reset(cutOprations.get(cutOprations.size() - 1).points);
            }else{
                reset(new ArrayList<Path>());
            }
        }
    }

    List<CutFreeOpration> cutOprationUndos;
    List<CutFreeOpration> cutOprations;
    class CutFreeOpration {
        List<Path> points = new ArrayList<>();
    }
    void addEvent(List<Path> list){
        cutOprationUndos.clear();
        CutFreeOpration opration = new CutFreeOpration();
        for (Path path:list){
            Path pointDes = new Path(path);
            opration.points.add(pointDes);
        }
        cutOprations.add(opration);
    }

    void reset(List<Path> list){
        pathList = new ArrayList<>();
        path = new Path();
        for (Path point:list){
            Path pointDes = new Path(point);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path.op(pointDes, Path.Op.UNION);
            }else{
                path.addPath(pointDes);
            }
            pathList.add(pointDes);
        }
    }

    public Bitmap cropLayer;
    @Override
    public void onDraw(Canvas canvas) {
        if (itemPath != null){
            canvas.drawPath(itemPath, paintPath);
        }else{
            Util.recycleBitmaps(cropLayer);
            cropLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvasCrop = new Canvas(cropLayer);
            canvasCrop.drawARGB(180, 0, 0, 0);
            canvasCrop.drawPath(path, paintCrop);
            canvas.drawBitmap(cropLayer, 0, 0, null);
        }
        canvas.drawPath(path, paintPath);
    }

    private float preX;
    private float preY;
    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
        float x = currentEvent.getX();
        float y = currentEvent.getY();
        float dx = Math.abs(x - preX);
        float dy = Math.abs(y - preY);
        if (dx >= 5 || dy >= 5) {
            checkPoint(x, y);
            itemPath.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);//用于绘制圆滑曲线，即贝塞尔曲线。
            preX = x;
            preY = y;
        }
        return true;
    }

    List<Path> pathList;
    Path itemPath;
    @Override
    public void onTouchDown(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        preX = x;
        preY = y;
        itemPath = new Path();
        itemPath.moveTo(x, y);
    }

    @Override
    public void onTouchUp(MotionEvent ev) {
        itemPath.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            path.op(itemPath, Path.Op.UNION);
        }else{
            path.addPath(itemPath);
        }
        pathList.add(itemPath);
        addEvent(pathList);
        itemPath = null;
    }


    @Override
    public boolean isMove() {
        return itemPath != null;
    }

    @Override
    public boolean isOperation() {
        return true;
    }

    private Paint paintResult = new Paint();

    @Override
    public void getCropImage(Bitmap bitmap, Matrix mViewMatrix, String filePath) {

        float padding = 32;

        left -= padding;
        top -= padding;
        right += padding;
        bottom += padding;

        left = Math.max(0, left);
        right = Math.min(right, width);
        top = Math.max(0, top);
        bottom = Math.min(bottom, height);

        if (path != null) {//图片裁剪
            Bitmap beforeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvasBefore = new Canvas(beforeBitmap);
            paintResult.setXfermode(null);
            canvasBefore.drawColor(0xFFFFFFFF);
            canvasBefore.drawBitmap(bitmap, mViewMatrix, paintResult);
            paintResult.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvasBefore.drawPath(path, paintResult);

            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            paintResult.setXfermode(null);
            canvas.drawBitmap(bitmap, mViewMatrix, paintResult);
            paintResult.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvas.drawBitmap(beforeBitmap, 0, 0, paintResult);

            Util.saveBitmap(new File(filePath), Util.cutBitmap(result, right - left, bottom - top, left, top));
            Util.recycleBitmaps(beforeBitmap, result);
        }
    }


}
