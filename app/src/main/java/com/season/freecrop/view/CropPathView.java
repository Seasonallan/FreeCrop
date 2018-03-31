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
import android.view.MotionEvent;

import com.season.freecrop.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
* 点对点多点裁剪
* @author season
* created at 2018/3/31 7:50
*/
public class CropPathView extends CropTool{

    private Paint paintPath = new Paint();
    private Paint paintPathCircle = new Paint();

    public boolean isClose = false;
    public Path path;
    public List<Point> points;
    public float radius;
    private Paint paintCrop = new Paint();

    CropPathView(Context context, int width, int height){
        super(context, width, height);
        //matrix = new Matrix();
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeWidth(4);
        paintPath.setColor(Color.parseColor("#ff00ff00"));
        paintPath.setAntiAlias(true);
        paintPathCircle.setAntiAlias(true);

        isClose = false;
        radius = 5 * context.getResources().getDisplayMetrics().density;
        path = new Path();
        points = new ArrayList<>();

        paintResult.setAntiAlias(true);
        paintCrop.setAntiAlias(true);
        paintCrop.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        cutOprationUndos = new ArrayList<>();
        cutOprations = new ArrayList<>();
    }

    private float left = Float.MAX_VALUE, top = Float.MAX_VALUE, right = Float.MIN_VALUE, bottom = Float.MIN_VALUE;
    private void checkPoint(float x, float y){
        left = Math.min(left, x); right = Math.max(right, x);
        top = Math.min(top, y); bottom = Math.max(bottom, y);
    }

    @Override
    public boolean canCropBitmap() {
        return isClose;
    }

    public boolean canBack() {
        return points.size() == 0;
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
            CutOpration opration = cutOprationUndos.remove(cutOprationUndos.size() - 1);
            cutOprations.add(opration);
            reset(opration.points, opration.isClose);
        }
    }
    @Override
    public void undo(){
        if (canPre()){
            CutOpration opration = cutOprations.remove(cutOprations.size() - 1);
            cutOprationUndos.add(opration);
            if (cutOprations.size() > 0){
                reset(cutOprations.get(cutOprations.size() - 1).points, cutOprations.get(cutOprations.size() - 1).isClose);
            }else{
                reset(new ArrayList<Point>(), false);
            }
        }
    }

    @Override
    public void onPathAdd(MotionEvent e) {
        if(isClose){
            return;
        }
        float x = e.getX();
        float y = e.getY();

        if (points.size() == 0){//至少两个点才形成闭环
            points.add(new Point(x, y).position(0));
        }else{
            Point point = points.get(0);
            Point pointLast = points.get(points.size() - 1);
            if (x >= point.x - radius*2 && x <= point.x + radius *2 && y >= point.y - radius *2 && y <= point.y + radius *2){
                //形成闭环
                isClose = true;
                x = point.x;
                y = point.y;
                points.add(new Point(x - (x - pointLast.x)/2, y - (y - pointLast.y)/2).isArc().position(points.size()));
                points.add(new Point(x, y).position(points.size()));
            }else{
                points.add(new Point(x - (x - pointLast.x)/2, y - (y - pointLast.y)/2).isArc().position(points.size()));
                points.add(new Point(x, y).position(points.size()));
            }
        }
        addEvent(points, isClose);
        buildPath();
    }
    /**
     * 重要参数，两点之间分为几段描画，数字愈大分段越多，描画的曲线就越精细.
     */
    private static final int STEPS = 12;
    private void buildPath(){
        left = Float.MAX_VALUE;
        top = Float.MAX_VALUE;
        right = Float.MIN_VALUE;
        bottom = Float.MIN_VALUE;
        path = new Path();
        //path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        if (points.size() < 3){
            return;
        }
        if (true){
            int position = 0;
            while (position < points.size()){
                Point pointCurrent = points.get(position);
                checkPoint(pointCurrent.x, pointCurrent.y);
                if (position == 0){
                    path.moveTo(pointCurrent.x, pointCurrent.y);
                }
                if (pointCurrent.arc && position > 0 && position < points.size() - 1){
                    Point pointStart = points.get(position - 1);
                    Point pointEnd = points.get(position + 1);
                    if (pointCurrent.active){
                        List<Float> points_x = new ArrayList<>();
                        points_x.add(pointStart.x);
                        points_x.add(pointCurrent.x);
                        points_x.add(pointEnd.x);

                        List<Float> points_y = new ArrayList<>();
                        points_y.add(pointStart.y);
                        points_y.add(pointCurrent.y);
                        points_y.add(pointEnd.y);

                        List<Cubic> calculate_x = calculate(points_x);
                        List<Cubic> calculate_y = calculate(points_y);

                        for (int i = 0; i < calculate_x.size(); i++) {
                            for (int j = 1; j <= STEPS; j++) {
                                float u = j / (float) STEPS;
                                path.lineTo(calculate_x.get(i).eval(u), calculate_y.get(i)
                                        .eval(u));
                            }
                        }
                    }else{
                        float centerX = (pointStart.x + pointEnd.x)/2;
                        float centerY = (pointStart.y + pointEnd.y)/2;
                        pointCurrent.resetPosition(centerX, centerY);
                        path.lineTo(pointEnd.x, pointEnd.y);
                    }
                }
                position++;
            }

            if (isClose){
                path.close();
            }
            return;
        }

        if (true){
            int i = 0;
            while (i < points.size()){
                Point pointCurrent = points.get(i);
                checkPoint(pointCurrent.x, pointCurrent.y);
                if (i == 0){
                    path.moveTo(pointCurrent.x, pointCurrent.y);
                }
                if (pointCurrent.arc && i > 0 && i < points.size() - 1){
                    Point pointStart = points.get(i - 1);
                    Point pointEnd = points.get(i + 1);

                    float centerX = (pointStart.x + pointEnd.x)/2;
                    float centerY = (pointStart.y + pointEnd.y)/2;

                    float[] resXY = {pointStart.x, pointStart.y, pointEnd.x, pointEnd.y};
                    float[] resXYDes =  new float[4];
                    Matrix matrixBack = new Matrix();
                    matrixBack.setTranslate(pointCurrent.x - centerX, pointCurrent.y - centerY);
                    matrixBack.mapPoints(resXYDes, resXY);

                    //贝塞尔曲线需要三个点（起始点 控制点 结束点）来确定
                    path.quadTo(resXYDes[0] , resXYDes[1], pointCurrent.x, pointCurrent.y);
                    path.quadTo(resXYDes[2] , resXYDes[3], pointEnd.x, pointEnd.y);
                }
                i++;
            }

        }else{
            int i = 0;
            while (i < points.size()){
                Point pointStart = points.get(i);
                checkPoint(pointStart.x, pointStart.y);
                if (i == 0){
                    path.moveTo(pointStart.x, pointStart.y);
                }

                if (!pointStart.arc && i + 2 < points.size()){
                    path.lineTo(pointStart.x, pointStart.y);
                    Point pointArc = points.get(i + 1);
                    Point pointEnd = points.get(i + 2);
                    //贝塞尔曲线需要三个点（起始点 控制点 结束点）来确定
                    path.quadTo(pointArc.x, pointArc.y, pointEnd.x, pointEnd.y);
                }
                i++;
            }
        }
        if (isClose){
            path.close();
        }
    }

    public class Cubic {

        float a,b,c,d;         /* a + b*u + c*u^2 +d*u^3 */

        public Cubic(float a, float b, float c, float d){
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }


        /** evaluate cubic */
        public float eval(float u) {
            return (((d*u) + c)*u + b)*u + a;
        }
    }
    /**
     * 计算曲线.
     *
     * @param x
     * @return
     */
    private List<Cubic> calculate(List<Float> x) {
        int n = x.size() - 1;
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /*
         * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
         * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
         * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])]
         *
         * by using row operations to convert the matrix to upper triangular and
         * then back sustitution. The D[i] are the derivatives at the knots.
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x.get(1) - x.get(0)) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x.get(i + 1) - x.get(i - 1)) - delta[i - 1])
                    * gamma[i];
        }
        delta[n] = (3 * (x.get(n) - x.get(n - 1)) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /* now compute the coefficients of the cubics */
        List<Cubic> cubics = new LinkedList<Cubic>();
        for (i = 0; i < n; i++) {
            Cubic c = new Cubic(x.get(i), D[i], 3 * (x.get(i + 1) - x.get(i))
                    - 2 * D[i] - D[i + 1], 2 * (x.get(i) - x.get(i + 1)) + D[i]
                    + D[i + 1]);
            cubics.add(c);
        }
        return cubics;
    }

    List<CutOpration> cutOprationUndos;
    List<CutOpration> cutOprations;
    class CutOpration{
        List<Point> points = new ArrayList<>();
        boolean isClose = false;
    }
    void addEvent(List<Point> list, boolean isClose){
        cutOprationUndos.clear();
        CutOpration opration = new CutOpration();
        for (Point point:list){
            Point pointDes = new Point(point.x, point.y);
            pointDes.arc = point.arc;
            pointDes.active = point.active;
            pointDes.position(point.position);
            opration.points.add(pointDes);
        }
        opration.isClose = isClose;
        cutOprations.add(opration);
    }

    void reset(List<Point> list, boolean isClose){
        points = new ArrayList<>();
        for (Point point:list){
            Point pointDes = new Point(point.x, point.y);
            pointDes.arc = point.arc;
            pointDes.position(point.position);
            pointDes.active = point.active;
            points.add(pointDes);
        }
        this.isClose = isClose;
        buildPath();
    }



    public Bitmap cropLayer;
    @Override
    public void onDraw(Canvas canvas) {
        if (isClose){
            Util.recycleBitmaps(cropLayer);
            cropLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvasCrop = new Canvas(cropLayer);
            canvasCrop.drawARGB(180, 0 , 0, 0);
            canvasCrop.drawPath(path, paintCrop);
            canvas.drawBitmap(cropLayer, 0, 0, null);

        }
        canvas.drawPath(path, paintPath);
        for (Point point: points){
            if (point.arc){
                paintPathCircle.setColor(Color.parseColor("#ffffff"));
                canvas.drawCircle(point.x, point.y, radius, paintPathCircle);
            }else{
                paintPathCircle.setColor(Color.parseColor("#bbffffff"));
                canvas.drawCircle(point.x, point.y, radius * 2, paintPathCircle);
                if (point.position == 0 && !isClose){
                    paintPathCircle.setColor(Color.parseColor("#ff00ff00"));
                }else{
                    paintPathCircle.setColor(Color.parseColor("#bb414141"));
                }
                canvas.drawCircle(point.x, point.y, radius, paintPathCircle);
            }
        }
    }

    boolean isFocus = false;
    int clickPosition = -1;
    @Override
    public void onTouchDown(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        for (Point point : points){
            if (x >= point.x - radius*2 && x <= point.x + radius *2 && y >= point.y - radius *2 && y <= point.y + radius *2){
                clickPosition = point.position;
                point.active = true;
                return;
            }
        }
        if (!isClose){
            return;
        }
        isFocus = Util.isTouchPointInPath(path, (int)x, (int)y);
    }

    @Override
    public void onTouchUp(MotionEvent ev) {
        if (isOperation()){
            addEvent(points, isClose);
        }
        isFocus = false;
        clickPosition = -1;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent currentEvent, float distanceX, float distanceY) {
        if (clickPosition >= 0){
            if (clickPosition == 0 && isClose){
                points.get(points.size() - 1).resetPosition(currentEvent.getX(), currentEvent.getY());
            }
            points.get(clickPosition).resetPosition(currentEvent.getX(), currentEvent.getY());
            buildPath();
            return true;
        }
        if (isFocus){
            for (Point point : points){
                point.movePosition(-distanceX, -distanceY);
            }
            buildPath();
           // matrix.postTranslate(-distanceX, -distanceY);
            return true;
        }
        return !isClose;
    }

    @Override
    public boolean isOperation() {
        return isFocus || clickPosition>=0;
    }
    @Override
    public boolean isMove() {
        return clickPosition>=0;
    }

    class Point{
        public boolean active = false;
        public int position = 0;
        public float x, y;
        Point(float x, float y){
            this.x = x;
            this.y = y;
        }
        public Point isArc(){
            arc = true;
            return this;
        }
        public Point position(int position){
            this.position = position;
            return this;
        }
        public void resetPosition(float x, float y){
            this.x = x;
            this.y = y;
        }
        public boolean arc = false;

        public void movePosition(float x, float y) {
            this.x += x;
            this.y += y;
        }
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

        if (path != null){//图片裁剪
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

            Util.saveBitmap(new File(filePath), Util.cutBitmap(result,  right - left, bottom - top, left, top));
            Util.recycleBitmaps(beforeBitmap, result);
        }
    }

}
