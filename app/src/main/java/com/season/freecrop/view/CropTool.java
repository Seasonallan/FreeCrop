package com.season.freecrop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;


/**
* 裁剪抽象方法
* @author season
* created at 2018/3/31 7:50
*/
public class CropTool {

    protected Context context;
    protected int width, height;
    CropTool(Context context, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context;
    }


    public void onDraw(Canvas canvas) {
    }

    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
        return false;
    }

    public void onTouchDown(MotionEvent ev) {
    }

    public void onTouchUp(MotionEvent ev) {
    }


    public boolean canPro(){
        return false;
    }
    public boolean canPre(){
        return false;
    }
    public void redo(){

    }
    public void undo(){

    }

    public boolean isMove() {
        return false;
    }

    public boolean isOperation() {
        return false;
    }

    private Paint paintResult = new Paint();

    public void getCropImage(Bitmap bitmap, Matrix mViewMatrix, String filePath) {

    }

    public void release() {
    }

    public boolean onScale(ScaleDetector detector) {
        return false;
    }

    public void onPathAdd(MotionEvent e) {
        
    }

    public boolean canCropBitmap() {
        return true;
    }

    public boolean canBack() {
        return true;
    }
}
