package com.season.freecrop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.season.freecrop.util.Util;
import java.io.File;

/**
* 裁剪视图
* @author season
* created at 2018/3/31 7:50
*/
public class CropView extends View {
    private Bitmap bitmap;

    public CropView(Context context) {
        super(context);
        init();
    }

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean setBitmap(final Bitmap bitmap) {
        this.bitmap = bitmap;
        post(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null)
                resetBitmap();
            }
        });
        return bitmap != null;
    }

    public Matrix mViewMatrix;
    Bitmap preViewBitmap;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, mViewMatrix, null);
        if (cropTool != null){
            cropTool.onDraw(canvas);
            if (cropTool.isMove()){
                drawPreviewImage(canvas);
            }
        }
    }

    public boolean canPro(){
        if (cropTool != null){
            return cropTool.canPro();
        }
        return false;
    }
    public boolean canPre(){
        if (cropTool != null){
            return cropTool.canPre();
        }
        return false;
    }
    public void redo(){
        if (cropTool != null){
            cropTool.redo();
            invalidate();
        }
    }
    public void undo(){
        if (cropTool != null){
            cropTool.undo();
            invalidate();
        }
    }

    Bitmap screenBitmap;
    private void drawPreviewImage(Canvas canvas){
        int widthPreview = 320;
        if (screenBitmap == null || screenBitmap.isRecycled()){
            setDrawingCacheEnabled(true);
            screenBitmap = Bitmap.createBitmap(getWidth() + widthPreview, getHeight() + widthPreview, Bitmap.Config.RGB_565);
            Canvas screenCanvas = new Canvas(screenBitmap);
            screenCanvas.drawARGB(255, 0, 0, 0);
            Matrix matrix = new Matrix();
            matrix.postTranslate(widthPreview/2, widthPreview/2);
            screenCanvas.concat(matrix);
            screenCanvas.drawBitmap(bitmap, mViewMatrix, null);
        }
        try {
            Util.recycleBitmaps(preViewBitmap);
            preViewBitmap = Util.cutBitmap(screenBitmap, widthPreview, widthPreview, currentPosition[0], currentPosition[1], false);
            canvas.drawBitmap(preViewBitmap, 0, 0, null);
            canvas.drawLine(0, widthPreview/2, widthPreview, widthPreview/2, whitePaint);
            canvas.drawLine(widthPreview/2, 0, widthPreview/2, widthPreview, whitePaint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Paint whitePaint;
    private void initPaint(){
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.parseColor("#88ffffff"));
        whitePaint.setStrokeWidth(2);
    }


    private void resetBitmap(){
        center = new float[]{getWidth() / 2f, getHeight() / 2f};
        float scale = getWidth() * 1.0f/bitmap.getWidth();
        mViewMatrix.setTranslate(getWidth()/2 - bitmap.getWidth()/2, getHeight()/2 - bitmap.getHeight()/2);
        mViewMatrix.postScale(scale, scale, getWidth()/2, getHeight()/2);
    }

    public void startPathFreeCrop() {
        resetBitmap();
        if (cropTool != null){
            cropTool.release();
            cropTool = null;
        }
        cropTool = new CropPathFreeView(getContext(), getWidth(), getHeight());
        invalidate();
    }

    public void startPathCrop() {
        resetBitmap();
        if (cropTool != null){
            cropTool.release();
            cropTool = null;
        }
        cropTool = new CropPathView(getContext(), getWidth(), getHeight());
        invalidate();
    }

    CropTool cropTool;
    public void startImageCrop(Integer integer) {
        resetBitmap();
        if (cropTool != null){
            cropTool.release();
            cropTool = null;
        }
        cropTool = new CropRectView(getContext(), getWidth(), getHeight(), integer);
        invalidate();
    }

    private ScaleDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        initPaint();
        mViewMatrix = new Matrix();

        ScaleDetector.OnScaleGestureListener scaleListener = new ScaleDetector
                .SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleDetector detector) {
                if (cropTool != null){
                    if (cropTool.onScale(detector)){
                        invalidate();
                        return true;
                    }
                }

                float preDegree = Util.getRotationBetweenLines(detector.preX2, detector.preY2, detector.preX1, detector.preY1);
                float newDegree = Util.getRotationBetweenLines(detector.currentX2, detector.currentY2, detector.currentX1, detector.currentY1);

                float degree = newDegree - preDegree;
                //  mViewMatrix.postRotate(degree, (detector.preX2 + detector.preX1) / 2, (detector.preY2 + detector.preY1) / 2);
                if (Math.abs(degree) < 18){
                    mViewMatrix.postRotate(degree, center[0], center[1]);
                }else{
                }

                float scaleFactor = detector.getScaleFactor();

                // mViewMatrix.postScale(scaleFactor, scaleFactor, mMidX, mMidY);
                mViewMatrix.postScale(scaleFactor, scaleFactor, center[0], center[1]);
                invalidate();

                return true;
            }

            @Override
            public void onScaleEnd(ScaleDetector detector) {
                super.onScaleEnd(detector);
            }
        };
        mScaleDetector = new ScaleDetector(getContext(), scaleListener);

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);

            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (cropTool != null){
                    cropTool.onPathAdd(e);
                    invalidate();
                }
                if (listener != null){
                    listener.onClick(CropView.this);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
                currentPosition[0] = currentEvent.getX();
                currentPosition[1] = currentEvent.getY();

                if (cropTool != null){
                    if (cropTool.onScroll(downEvent, currentEvent, distanceX, distanceY)){
                        invalidate();
                        return true;
                    }
                }
                mViewMatrix.postTranslate(-distanceX, -distanceY);
                invalidate();
                return true;
            }
        };
        mGestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    float[] currentPosition = new float[2];
    float[] center;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            if (ev.getPointerCount() == 1){
                if (cropTool != null) {
                    cropTool.onTouchDown(ev);
                }
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (cropTool != null) {
                cropTool.onTouchUp(ev);
            }
            Util.recycleBitmaps(screenBitmap);
            screenBitmap = null;
            invalidate();
            if (listener != null){
                listener.onClick(this);
            }
        }
        if (cropTool != null && cropTool.isOperation()){
            mGestureDetector.onTouchEvent(ev);
            return true;
        }
        mScaleDetector.onTouchEvent(ev);
        if (!mScaleDetector.isInProgress()) {
            mGestureDetector.onTouchEvent(ev);
        }
        return true;
    }

    public String getCropImage(File cropFile) {
        try {
            if (cropFile == null){
                return null;
            }
            if (cropTool != null){
                cropTool.getCropImage(bitmap, mViewMatrix, cropFile.toString());
                return cropFile.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void release() {
        Util.recycleBitmaps(bitmap, screenBitmap, preViewBitmap);
        if (cropTool != null){
            cropTool.release();
        }
    }

    private OnClickListener listener;
    public void setOnActionListener(OnClickListener listener){
        this.listener = listener;
    }

    public boolean canComplete() {
        if (cropTool != null){
            return cropTool.canCropBitmap();
        }
        return true;
    }

    public boolean canBack() {
        if (cropTool != null){
            return cropTool.canBack();
        }
        return true;
    }

    public void clearTool() {
        if (cropTool != null){
            cropTool.release();
            cropTool = null;
            invalidate();
        }
    }
}
