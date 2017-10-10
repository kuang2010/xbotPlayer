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
import android.view.View;
import android.view.WindowManager;

/**
 * Created by lisongting on 2017/10/9.
 * MapView:正方形的Rviz地图区域
 */

public class MapView extends View {
    int width,height;
    private static final String TAG = "MapView";
    Matrix matrix = new Matrix();
    private float scaleX = 1.0F;
    private float scaleY = 1.0F;

    private Bitmap bitmap;


    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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
        matrix.reset();

//        Bitmap tmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        for(int i=0;i<width;i++){
//            for(int j=0;j<height;j++){
//                if(i*j%13==0){
//                    tmp.setPixel(i, j, Color.RED);
//                } else if (i * j % 27 == 0) {
//                    tmp.setPixel(i, j, Color.YELLOW);
//                }
//
//            }
//        }
//
//        if(scaleX<0.3){
//            scaleX=0.3F;
//        }else if(scaleX>2){
//            scaleX = 2;
//        }
//        if(scaleY<0.3){
//            scaleY = 0.3F;
//        }else if(scaleY>2){
//            scaleY = 2;
//        }
        if (bitmap == null) {
            canvas.drawColor(Color.DKGRAY);
        } else {
            matrix.setScale(scaleX,scaleY);
            canvas.drawBitmap(bitmap, matrix, null);
            bitmap.recycle();
        }

    }

    public void log(String s) {
        Log.i(TAG, TAG + " -- "+s);
    }

    public void zoomIn(float scale) {
        scaleX +=0.2;
        scaleY +=0.2;
        invalidate();
    }

    public void zoomOut(float scale) {
        scaleX -= 0.2;
        scaleY -= 0.2;
        invalidate();
    }


    public void updateMap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    public Size getSize(){
        return new Size(width, height);
    }
}
