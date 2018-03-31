package com.season.freecrop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.season.freecrop.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
* 图片规则裁剪
* @author season
* created at 2018/3/31 7:48
*/
public class CropRectView extends CropTool {

    public CropOpView mOpView;
    private Bitmap cropBitmap;
    public Matrix mOpMatrix;
    public Bitmap cropLayer;
    private Paint paintCrop = new Paint();
    private Paint paintResult = new Paint();
    private float[] initMatrix;
    CropRectView(Context context, int width, int height, Integer integer){
        super(context, width, height);
        mOpView = new CropOpView(context);
        mOpMatrix = new Matrix();
        cropBitmap = BitmapFactory.decodeResource(context.getResources(), integer);

        float scale = width*2.0f/(3*cropBitmap.getWidth());
        mOpMatrix.setTranslate(getWidth()/2 - cropBitmap.getWidth()/2, getHeight()/2 - cropBitmap.getHeight()/2);
        mOpMatrix.postScale(scale, scale, getWidth()/2, getHeight()/2);
        mOpView.bindRect(getWidth(), getHeight(), new Rect(0, 0, cropBitmap.getWidth(), cropBitmap.getHeight()), mOpMatrix, true);

        initMatrix = new float[9];
        mOpMatrix.getValues(initMatrix);
        paintResult.setAntiAlias(true);
        paintCrop.setAntiAlias(true);
        paintCrop.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        cutOprationUndos = new ArrayList<>();
        cutOprations = new ArrayList<>();
    }

    float[] centerCrop;
    boolean isScale = false, isZoom = false, isOpe = false;
    public boolean isFocusNow = false;


    @Override
    public void release() {
        Util.recycleBitmaps(cropBitmap);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Util.recycleBitmaps(cropLayer);
        cropLayer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasCrop = new Canvas(cropLayer);
        canvasCrop.drawARGB(200, 0 , 0, 0);
        canvasCrop.drawBitmap(cropBitmap, mOpMatrix, paintCrop);

        canvas.drawBitmap(cropLayer, 0, 0, null);

        mOpView.bindRect(getWidth(), getHeight(), new Rect(0, 0, cropBitmap.getWidth(), cropBitmap.getHeight()), mOpMatrix);
        mOpView.draw(canvas, true, true, false);
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
            CutRectOpration opration = cutOprationUndos.remove(cutOprationUndos.size() - 1);
            cutOprations.add(opration);
            reset(opration.matrix);
        }
    }
    @Override
    public void undo(){
        if (canPre()){
            CutRectOpration opration = cutOprations.remove(cutOprations.size() - 1);
            cutOprationUndos.add(opration);
            if (cutOprations.size() > 0){
                reset(cutOprations.get(cutOprations.size() - 1).matrix);
            }else{
                reset(initMatrix);
            }
        }
    }

    List<CutRectOpration> cutOprationUndos;
    List<CutRectOpration> cutOprations;
    class CutRectOpration {
        float[] matrix= new float[9];
    }
    void addEvent(Matrix matrix){
        cutOprationUndos.clear();
        CutRectOpration opration = new CutRectOpration();
        matrix.getValues(opration.matrix);
        cutOprations.add(opration);
    }

    void reset(float[] matrix){
        mOpMatrix.setValues(matrix);
    }

    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void onTouchDown(MotionEvent ev) {
        centerCrop = null;
        mOpView.bindRect(getWidth(), getHeight(), new Rect(0, 0, cropBitmap.getWidth(), cropBitmap.getHeight()), mOpMatrix);
        isScale = false; isZoom = false;
        isFocusNow = mOpView.isTouched((int)ev.getX(), (int)ev.getY());
        isScale = mOpView.isScaleTouched((int)ev.getX(), (int)ev.getY());
        isZoom = mOpView.isRotateTouched((int)ev.getX(), (int)ev.getY());
        isOpe = true;
    }

    @Override
    public void onTouchUp(MotionEvent ev) {
        if (isFocusNow || isScale || isZoom){
            addEvent(mOpMatrix);
        }
        isOpe = false;
        isScale = false;
        isZoom = false;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            fixDegree();
        }
    }

    private boolean fixDegree(){
        if (centerCrop != null && cropBitmap != null){
            if (mOpView.degree >= 355 || mOpView.degree <= 5){
                mOpMatrix.postRotate(-mOpView.degree, centerCrop[0], centerCrop[1]);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOperation() {
        return isScale || isZoom;
    }

    @Override
    public boolean onScale(ScaleDetector detector) {
        if (isFocusNow){
            if (centerCrop == null){
                centerCrop = new float[]{cropBitmap.getWidth() / 2f, cropBitmap.getHeight() / 2f};
                mOpMatrix.mapPoints(centerCrop);
            }

            float preDegree = Util.getRotationBetweenLines(detector.preX2, detector.preY2, detector.preX1, detector.preY1);
            float newDegree = Util.getRotationBetweenLines(detector.currentX2, detector.currentY2, detector.currentX1, detector.currentY1);

            float degree = newDegree - preDegree;
            if (Math.abs(degree) < 18){
                mOpMatrix.postRotate(degree, centerCrop[0], centerCrop[1]);
            }else{
            }

            float scaleFactor = detector.getScaleFactor();

            mOpMatrix.postScale(scaleFactor, scaleFactor, centerCrop[0], centerCrop[1]);

            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY){
        if (isZoom){
            if (centerCrop == null){
                centerCrop = new float[]{cropBitmap.getWidth() / 2f, cropBitmap.getHeight() / 2f};
                mOpMatrix.mapPoints(centerCrop);
            }
            //   mCurrentScale *= scaleFactor;
            float preDegree = Util.getRotationBetweenLines(centerCrop[0], centerCrop[1], currentEvent.getX() + distanceX, currentEvent.getY() + distanceY);
            float newDegree = Util.getRotationBetweenLines(centerCrop[0], centerCrop[1], currentEvent.getX(), currentEvent.getY());

            float degree = newDegree - preDegree;
            mOpMatrix.postRotate(degree, centerCrop[0], centerCrop[1]);

            float preDistance = Util.getDistance(centerCrop[0], centerCrop[1], currentEvent.getX() + distanceX, currentEvent.getY() + distanceY);
            float newDistance = Util.getDistance(centerCrop[0], centerCrop[1], currentEvent.getX(), currentEvent.getY());
            //  float scaleFactor = detector.getScaleFactor();

            float scaleFactor = newDistance/preDistance;
            mOpMatrix.postScale(scaleFactor, scaleFactor, centerCrop[0], centerCrop[1]);
        }else if (isScale){
            if (centerCrop == null){
                centerCrop = new float[]{cropBitmap.getWidth() / 2f, cropBitmap.getHeight() / 2f};
                mOpMatrix.mapPoints(centerCrop);
            }

            float preDistance = Util.getDistance(centerCrop[0], centerCrop[1], currentEvent.getX() + distanceX, currentEvent.getY() + distanceY);
            float newDistance = Util.getDistance(centerCrop[0], centerCrop[1], currentEvent.getX(), currentEvent.getY());
            //  float scaleFactor = detector.getScaleFactor();

            float cad = -1f;
            if (mOpView.isRight){
                if (currentEvent.getX() > centerCrop[0]){
                    cad = 1f;
                }else{
                    cad = -1f;
                    mOpView.isRight = false;
                }
            }else{
                if (currentEvent.getX() > centerCrop[0]){
                    cad = -1f;
                    mOpView.isRight = true;
                }else{
                    cad = 1f;
                }
            }
            mOpMatrix.postRotate(-mOpView.degree, centerCrop[0], centerCrop[1]);
            mOpMatrix.postScale(newDistance/preDistance * cad, 1, centerCrop[0], centerCrop[1]);
            mOpMatrix.postRotate(mOpView.degree, centerCrop[0], centerCrop[1]);
        }else{
            if (isFocusNow){
                mOpMatrix.postTranslate(-distanceX, -distanceY);
            }else{
                return false;
            }
        }
        return true;
    }

    private float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = Float.MIN_VALUE, bottom = Float.MIN_VALUE;
    private void checkPoint(float x, float y){
        left = Math.min(left, x); right = Math.max(right, x);
        top = Math.min(top, y); bottom = Math.max(bottom, y);
    }
    @Override
    public void getCropImage(Bitmap bitmap, Matrix mViewMatrix, String filePath) {
        float[] points = mOpView.desPoints;
        checkPoint(points[0], points[1]);
        checkPoint(points[2], points[3]);
        checkPoint(points[4], points[5]);
        checkPoint(points[6], points[7]);
        left = Math.max(0, left);
        right = Math.min(right, getWidth());
        top = Math.max(0, top);
        bottom = Math.min(bottom, getHeight());

        if (cropBitmap != null){//图片裁剪

//            Bitmap beforeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvasBefore = new Canvas(beforeBitmap);
//            paintResult.setXfermode(null);
//            canvasBefore.drawColor(0xFFFFFFFF);
//            canvasBefore.drawBitmap(bitmap, mViewMatrix, paintResult);
//            paintResult.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//            canvasBefore.drawBitmap(cropBitmap, mOpMatrix, paintResult);
//
//            Util.saveBitmap(new File(filePath), Util.cutBitmap(beforeBitmap,  right - left, bottom - top, left, top));
//            Util.recycleBitmaps(beforeBitmap);

           Bitmap beforeBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvasBefore = new Canvas(beforeBitmap);
            paintResult.setXfermode(null);
            canvasBefore.drawColor(0xFFFFFFFF);
            canvasBefore.drawBitmap(bitmap, mViewMatrix, paintResult);
            paintResult.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvasBefore.drawBitmap(cropBitmap, mOpMatrix, paintResult);

            Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            paintResult.setXfermode(null);
            canvas.drawBitmap(bitmap, mViewMatrix, paintResult);
            paintResult.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvas.drawBitmap(beforeBitmap, 0, 0, paintResult);

            Util.saveBitmap(new File(filePath), Util.cutBitmap(result,  right - left, bottom - top, left, top));
            Util.recycleBitmaps(beforeBitmap, result);
        }
    }
}
