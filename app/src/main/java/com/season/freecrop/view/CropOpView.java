package com.season.freecrop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.season.freecrop.util.Util;
import com.season.freecrop.util.ToolBitmapCache;


/**
* 操作框，控制缩放的位置比例和角度
* @author season
* created at 2018/3/31 7:47
*/
public class CropOpView {

    private int padding = 2;
    private Paint bitmapPaint = new Paint();
    private Paint paint  = new Paint() ;

    private Bitmap zoom, scaleX, scaleY, close;
    private Context context;

    private Rect rect;
    public float[] srcPoints, desPoints, fixPoints;
    private int width, height;
    private float minWidth = 108;
    private float[] minScale;
    public boolean isRight = true;
    public CropOpView(Context context)
    {
        this.context = context;
        desPoints = new float[8];
        fixPoints = new float[8];
        minScale = new float[2];
        padding = (int) (padding * context.getResources().getDisplayMetrics().density);
        minWidth = 158;

        bitmapPaint.setAntiAlias(true);

        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(0xffeeeeee);
        paint.setStrokeWidth(2 * context.getResources().getDisplayMetrics().density);

        zoom = ToolBitmapCache.getDefault().getZoom(context);
        scaleX = ToolBitmapCache.getDefault().getScaleX(context);
        close = ToolBitmapCache.getDefault().getClose(context);
    }

    public void bindRect(int width, int height, Rect rect, Matrix matrix){
        bindRect(width, height, rect, matrix, false);
    }

    public float[] bindRect(int width, int height, Rect rect, Matrix matrix, boolean force){
        if (srcPoints == null || force){
            this.width = width;
            this.height = height;
            srcPoints = new float[]{rect.left - padding, rect.top - padding,
                    rect.right + padding, rect.top - padding,
                    rect.right + padding, rect.bottom + padding,
                    rect.left - padding, rect.bottom + padding};
            rect = new Rect(rect.left - padding, rect.top - padding, rect.right + padding, rect.bottom + padding);
            minScale[0] = minWidth/ (rect.right - rect.left);
            minScale[1] = minWidth/ (rect.bottom - rect.top);
        }
        if (srcPoints == null){
            return null;
        }
        matrix.mapPoints(desPoints, srcPoints);

        float ox = center[0], oy = center[1];
        center = new float[]{desPoints[0] + (desPoints[4] - desPoints[0])/2, desPoints[1] + (desPoints[5] - desPoints[1])/2};
        degree = Util.getRotationBetweenLines(desPoints[6], desPoints[7], desPoints[0], desPoints[1]);

        double oriX = (srcPoints[2] - srcPoints[0]) * (srcPoints[2] - srcPoints[0]) + (srcPoints[3] - srcPoints[1]) * (srcPoints[3] - srcPoints[1]);
        oriX = Math.sqrt(oriX);
        double finX = (desPoints[2] - desPoints[0]) * (desPoints[2] - desPoints[0]) + (desPoints[3] - desPoints[1]) * (desPoints[3] - desPoints[1]);
        finX = Math.sqrt(finX);

        double oriY = (srcPoints[6] - srcPoints[0]) * (srcPoints[6] - srcPoints[0]) + (srcPoints[7] - srcPoints[1]) * (srcPoints[7] - srcPoints[1]);
        oriY = Math.sqrt(oriY);
        double finY = (desPoints[6] - desPoints[0]) * (desPoints[6] - desPoints[0]) + (desPoints[7] - desPoints[1]) * (desPoints[7] - desPoints[1]);
        finY = Math.sqrt(finY);
        scale = new float[]{(isRight?1:-1)* (float) (finX/oriX), (float) (finY/oriY)};
       // Logger.d("bindRect... x= "+ center[0] +"  ,y= "+ center[1] +"  ,sx= "+ scale[0] +"  ,sy= "+ scale[1] +" ,degree= "+ degree);

        if (Math.abs(scale[0]) >= minScale[0] && scale[1] >= minScale[1]){
            System.arraycopy(desPoints, 0, fixPoints, 0, 8);
        }else{
            Matrix matrixFix = new Matrix();
            float sx = (isRight?1:-1)* Math.max(minScale[0], Math.abs(scale[0]));
            float sy = Math.max(minScale[1], scale[1]);
            matrixFix.postTranslate(center[0] - (rect.right - rect.left)/2,center[1] - (rect.bottom - rect.top)/2);
            matrixFix.postScale(sx, sy, center[0], center[1]);
            matrixFix.postRotate(degree, center[0], center[1]);
            matrixFix.mapPoints(fixPoints, srcPoints);
        }


        return new float[]{ox - center[0], oy - center[1]};
    }

    public float[] scale = new float[]{1,1};
    public float[] center = new float[]{0,0};
    public float degree;
    public void draw(Canvas canvas, boolean isScale, boolean isZoom, boolean isClose) {
        if (srcPoints == null){
            return;
        }

//        Path path = new Path();
//        path.moveTo(desPoints[0], desPoints[1]);
//        path.lineTo(desPoints[2], desPoints[3]);
//        path.lineTo(desPoints[4], desPoints[5]);
//        path.lineTo(desPoints[6], desPoints[7]);
//        path.close();
//
//        canvas.drawPath(path, paint);

        canvas.drawLine(fixPoints[0], fixPoints[1], fixPoints[2], fixPoints[3], paint);
        canvas.drawLine(fixPoints[2], fixPoints[3], fixPoints[4], fixPoints[5], paint);
        canvas.drawLine(fixPoints[4], fixPoints[5], fixPoints[6], fixPoints[7], paint);
        canvas.drawLine(fixPoints[6], fixPoints[7], fixPoints[0], fixPoints[1], paint);

        int saveCount = 0;
        if (isScale){
            float rightCenterX = fixPoints[2] + (fixPoints[4] - fixPoints[2])/2;
            float rightCenterY = fixPoints[3] + (fixPoints[5] - fixPoints[3])/2;
            if ((rightCenterX < -scaleX.getWidth() && rightCenterY < -scaleX.getHeight()) ||
                    (rightCenterX > width + scaleX.getWidth() && rightCenterY < height + scaleX.getHeight())){

            }else{
                saveCount = canvas.save();
                canvas.rotate(degree, rightCenterX, rightCenterY);
                canvas.drawBitmap(scaleX, rightCenterX - scaleX.getWidth()/2, rightCenterY - scaleX.getHeight()/2, bitmapPaint);
                canvas.restoreToCount(saveCount);
            }
        }
        if (isZoom){
            if ((fixPoints[4] < -zoom.getWidth() && fixPoints[5] < -zoom.getHeight()) ||
                    (fixPoints[4] > width + zoom.getWidth() && fixPoints[5] < height + zoom.getHeight())){

            }else{
                saveCount = canvas.save();
                canvas.rotate(degree, fixPoints[4], fixPoints[5]);
                canvas.drawBitmap(zoom, fixPoints[4] - zoom.getWidth()/2, fixPoints[5] - zoom.getHeight()/2, bitmapPaint);
                canvas.restoreToCount(saveCount);
            }
        }
        if (isClose){
            if ((fixPoints[0] < -close.getWidth() && fixPoints[1] < -close.getHeight()) ||
                    (fixPoints[0] > width + close.getWidth() && fixPoints[1] < height + close.getHeight())){

            }else{
                canvas.drawBitmap(close, fixPoints[0] - close.getWidth()/2, fixPoints[1] - close.getHeight()/2, bitmapPaint);
            }
        }
    }


    public boolean isDeleteTouched(int x, int y){
        if (fixPoints == null || close == null || close.isRecycled()){
            return false;
        }
        int radius = close.getWidth()/2;
        if (x >= fixPoints[0] - radius && x < fixPoints[0] + radius){
            if (y >= fixPoints[1] - radius && y < fixPoints[1] + radius){
                return true;
            }
        }
        return false;
//        Path path = new Path();
//        path.moveTo(fixPoints[0] - radius, fixPoints[1] - radius);
//        path.lineTo(fixPoints[0] - radius, fixPoints[1] + radius);
//        path.lineTo(fixPoints[0] + radius, fixPoints[1] + radius);
//        path.lineTo(fixPoints[0] + radius, fixPoints[1] - radius);
//        path.close();
//
//        return isTouchPointInPath(path, x, y);
    }

    public boolean isScaleTouched(int x, int y){
        if (fixPoints == null || scaleX == null || scaleX.isRecycled()){
            return false;
        }
        //int radiusX = scaleX.getWidth()/2;
        int radiusX = scaleX.getHeight()/2;
        int radiusY = scaleX.getHeight()/2;

        float centerX = (fixPoints[4] + fixPoints[2])/2 ;
        float centerY = (fixPoints[5] + fixPoints[3])/2 ;
        if (x >= centerX - radiusX && x < centerX + radiusX){
            if (y >= centerY - radiusY && y < centerY + radiusY){
                return true;
            }
        }
        return false;
//        Path path = new Path();
//        path.moveTo(centerX - radiusX, centerY - radiusY);
//        path.lineTo(centerX - radiusX, centerY + radiusY);
//        path.lineTo(centerX + radiusX, centerY + radiusY);
//        path.lineTo(centerX + radiusX, centerY - radiusY);
//        path.close();
//
//        return isTouchPointInPath(path, x, y);
    }

    public boolean isRotateTouched(int x, int y){
        if (fixPoints == null || zoom == null || zoom.isRecycled()){
            return false;
        }
        int radius = zoom.getWidth()/2;
        if (x >= fixPoints[4] - radius && x < fixPoints[4] + radius){
            if (y >= fixPoints[5] - radius && y < fixPoints[5] + radius){
                return true;
            }
        }
        return false;
//        Path path = new Path();
//        path.moveTo(fixPoints[4] - radius, fixPoints[5] - radius);
//        path.lineTo(fixPoints[4] - radius, fixPoints[5] + radius);
//        path.lineTo(fixPoints[4] + radius, fixPoints[5] + radius);
//        path.lineTo(fixPoints[4] + radius, fixPoints[5] - radius);
//        path.close();
//
//        return isTouchPointInPath(path, x, y);
    }

    public boolean isTouched(int x, int y) {
        if (fixPoints == null){
            return false;
        }

        Path path = new Path();
        path.moveTo(fixPoints[0], fixPoints[1]);
        path.lineTo(fixPoints[2], fixPoints[3]);
        path.lineTo(fixPoints[4], fixPoints[5]);
        path.lineTo(fixPoints[6], fixPoints[7]);
        path.close();

        return Util.isTouchPointInPath(path, x, y);
    }

}
