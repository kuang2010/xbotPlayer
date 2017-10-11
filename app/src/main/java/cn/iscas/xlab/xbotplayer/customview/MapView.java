package cn.iscas.xlab.xbotplayer.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by lisongting on 2017/10/9.
 * MapView:正方形的Rviz地图区域
 */

public class MapView extends View implements View.OnTouchListener{
    int width,height;
    private static final String TAG = "MapView";
    Matrix matrix = new Matrix();
    private float scaleX = 1.0F;
    private float scaleY = 1.0F;
    private int fingers;
    private Bitmap bitmap;

    //与缩放和旋转相关的变量
    private float gestureCenterX = 0;
    private float gestureCenterY = 0;
    //两手指上次的距离
    private double oldDistance;
    //上次的角度
    private double oldAngle =0;
    private float rotateAngle=0;

    private final int MODE_NONE  =0;
    private final int MODE_SCALE  =1;
    private final int MODE_ROTATE  =2;
    private int mode = MODE_NONE;

    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;
            log("ScreenSize:" + screenWidth + "X" + screenHeight);

            if (widthSize < screenWidth && heightSize < screenWidth) {
                //设置成正方形
                widthSize = widthSize > heightSize ? widthSize : heightSize;
                heightSize = widthSize;
            }else{
                widthSize = screenWidth;
                heightSize = screenWidth;
            }
            width = widthSize;
            height = heightSize;
            log("widthMode:"+widthMode);
            log("width:"+width);
            log("height:"+height);

        }

        setMeasuredDimension(widthSize, heightSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        matrix.reset();

        if (bitmap == null) {
            canvas.drawColor(Color.DKGRAY);
        } else {
            if (mode == MODE_ROTATE) {
                matrix.postRotate(rotateAngle, gestureCenterX, gestureCenterY);
//            matrix.reset();
            } else if (mode == MODE_SCALE) {
                matrix.postScale(scaleX,scaleY,gestureCenterX,gestureCenterY);
//            matrix.reset();
            }
            canvas.drawBitmap(bitmap, matrix, null);
            bitmap.recycle();
        }

    }

    public void log(String s) {
        Log.i(TAG, TAG + " -- "+s);
    }

    public void updateMap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public Size getSize(){
        return new Size(width, height);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
//                log("ACTION_DOWN");
                mode = MODE_NONE;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
//                log("ACTION_POINTER_DOWN");
                mode = MODE_NONE;

                oldDistance = getMoveDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                oldAngle = getAngle(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                gestureCenterX = (event.getX(0) + event.getX(1)) * 0.5F;
                gestureCenterY = (event.getY(0) + event.getY(1)) * 0.5F;
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                if (pointerCount == 2) {
                    double newDistance = getMoveDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    double newAngle = getAngle(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    log("newDistance:" + newDistance + ",oldDistance:" + oldDistance);
                    log("newAngle:" + newAngle + ",oldAngle:" + oldAngle);
                    if (Math.abs(newAngle - oldAngle) > 25 ) {
                        rotateAngle = (float) (newAngle - oldAngle);
                        if (rotateAngle > 0) {
//                            rotateAngle += 1;
                            rotateAngle = 3;
                        } else {
//                            rotateAngle -= 1;
                            rotateAngle = -3;
                        }
                        mode = MODE_ROTATE;
                        log("-------rotate:" + rotateAngle);
                        invalidate();
                    } else if (Math.abs(newDistance - oldDistance) > 100 && oldDistance > 0) {

                        double delta = newDistance - oldDistance;
                        if (delta > 0) {
//                            scaleX += 0.1;
//                            scaleY += 0.1;
                            scaleX = 1.05F;
                            scaleY = 1.05F;
                        } else {
//                            scaleX -= 0.1;
//                            scaleY -= 0.1;
                            scaleX = 0.95F;
                            scaleY = 0.95F;

                        }
                        if (scaleX < 0.5) {
                            scaleX = 0.5F;
                        }
                        if (scaleY < 0.5) {
                            scaleY = 0.5F;
                        }
                        mode = MODE_SCALE;
                        log("-------scale:" + scaleX);
                        invalidate();
                    } else {
                        mode = MODE_NONE;
                    }

                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
//                log("ACTION_POINTER_UP");
                log("PointerCount:" + event.getPointerCount());
                break;
            case MotionEvent.ACTION_UP:
//                log("ACTION_UP");
                getParent().requestDisallowInterceptTouchEvent(false);
                mode = MODE_NONE;
                oldAngle = 0;
                oldDistance = 0;
                rotateAngle = 0;
                scaleX=1.0F;
                scaleY = 1.0F;
                break;
        }
        return true;
    }

    //计算两个手指的移动距离
    public double getMoveDistance(float x1,float y1,float x2,float y2) {
        float x = x1 - x2;
        float y = y1 - y1;
        return Math.sqrt(x * x - y * y);

    }


    public double getAngle(float x1,float y1,float x2,float y2) {

        float lenX = x2 - x1;
        float lenY = y2 - y1;
        float lenXY = (float) Math.sqrt((double) (lenX * lenX+ lenY * lenY));
        //计算弧度,如果触摸点在中心位置以下，则为正弧度，如果在中心位置上面，则为负
        double radian = Math.acos(lenX / lenXY) * (y2 < y1 ? -1 : 1);
        double tmp = Math.round(radian / Math.PI * 180);
        return tmp >= 0 ? tmp : tmp + 360;

    }



}
