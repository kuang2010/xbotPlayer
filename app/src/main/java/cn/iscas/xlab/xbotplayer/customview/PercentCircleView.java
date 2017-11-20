package cn.iscas.xlab.xbotplayer.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import cn.iscas.xlab.xbotplayer.R;

/**
 * 一个用来显示电量多少的水波圆形控件
 * Created by lisongting on 2017/11/14.
 */

public class PercentCircleView extends View {

    private int percent = 72;
    private int radius;
    private int textSize;
    private int textColor;
    private int strokeSize;
    private int strokeColor;
    private Paint strokePaint;
    private Paint textPaint;

    //该圆形控件的圆心点
    private float centerX;
    private float centerY;

    //电量的三种状态颜色
    private int colorBatteryNormal,colorBatteryLow,colorBatteryVeryLow;
    
    private Path bezierPath;
    private Paint bezierPaint;
    private float bezierStartX , bezierStartY;
    //贝塞尔曲线起始点的偏移量，用于动画效果
    private float bezierStartShift = 0;
    float peakHeight=0;
    float peakWidth =0;
    //用于裁剪的path
    private Path clipPath;

    private ValueAnimator valueAnimator;
    public PercentCircleView(Context context) {
        this(context, null);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PercentCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PercentCircleView, 0, 0);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int index = array.getIndex(i);
            switch (index) {
                case R.styleable.PercentCircleView_radius:
                    radius = array.getDimensionPixelSize(index, 200);
                    break;
                case R.styleable.PercentCircleView_normal_state_color:
                    colorBatteryNormal = array.getColor(index, Color.GREEN);
                    break;
                case R.styleable.PercentCircleView_mid_state_color:
                    colorBatteryLow = array.getColor(index, Color.parseColor("#ff8f00"));
                    break;
                case R.styleable.PercentCircleView_low_state_color:
                    colorBatteryVeryLow = array.getColor(index, Color.RED);
                    break;
                case R.styleable.PercentCircleView_stroke_size:
                    strokeSize = array.getDimensionPixelSize(index, 40);
                    break;
                case R.styleable.PercentCircleView_stroke_color:
                    strokeColor = array.getColor(index, Color.GREEN);
                    break;
                case R.styleable.PercentCircleView_text_size:
                    textSize = array.getDimensionPixelSize(index, 15);
                    break;
                case R.styleable.PercentCircleView_text_color:
                    textColor = array.getColor(index, Color.GREEN);
                    break;
                default:
                    break;
            }
        }

        initializePaint();

        array.recycle();

    }

    private void initializePaint() {

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeSize);
        strokePaint.setAlpha(50);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

        bezierPaint = new Paint();
        bezierPaint.setStyle(Paint.Style.FILL);
        bezierPaint.setStrokeWidth(8);
        bezierPaint.setAntiAlias(true);

        bezierPath = new Path();
        clipPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        if (widthMode == MeasureSpec.AT_MOST) {
//            int wantSize  = (radius + strokeSize) * 2 < widthSize ? (radius + strokeSize) * 2 : widthSize / 2;
//            width = wantSize < widthSize ? wantSize : widthSize;
            width = (radius + strokeSize) * 2;

        } else if (widthMode == MeasureSpec.EXACTLY) {
//            width = (radius + strokeSize) * 2 < widthSize ? (radius + strokeSize) * 2 : widthSize / 2;
            width = (radius + strokeSize) * 2;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
//            int wantSize = (radius + strokeSize) * 2 ;
//            height = wantSize < heightSize ? wantSize : heightSize;
            height = (radius + strokeSize) * 2 ;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            height = (radius + strokeSize) * 2 ;
        }

        //将width 和height 统一设置为更小的
//        height = width < height ? width : height;
        setMeasuredDimension(width, height);
        log("onMeasuredDimension:" + width + "x" + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        canvas.save();
        clipPath.reset();
        canvas.clipPath(clipPath);
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CCW);
        canvas.clipPath(clipPath, Region.Op.REPLACE);

        if (percent <= 10) {
            bezierPaint.setColor(colorBatteryVeryLow);
        } else if (percent <= 30) {
            bezierPaint.setColor(colorBatteryLow);
        }else{
            bezierPaint.setColor(colorBatteryNormal);
        }

        //使用贝塞尔曲线绘制水波图案
        if (percent>= 0 && percent < 50) {
            float distanceToCenter ;
            if (percent == 0) {
                //如果等于0%则按照1%来显示
                distanceToCenter = 2*radius*(50 - 1)/100F  ;
            } else {
                distanceToCenter = 2*radius*(50 - percent)/100F  ;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2);
            peakHeight = (radius-distanceToCenter)/4;
            bezierStartX = centerX - 2 * peakWidth + bezierStartShift;
            bezierStartY = centerY + distanceToCenter;
        } else if (percent > 50 && percent <= 100) {
            float distanceToCenter;
            //如果等于100则按照99来显示
            if (percent == 100) {
                distanceToCenter = 2 * radius * (99 - 50) / 100F;
            } else {
                distanceToCenter = 2 * radius * (percent - 50) / 100F;
            }
            double angle = Math.acos(distanceToCenter / radius);
            peakWidth = (float) (radius * Math.sin(angle) / 2);
            peakHeight = (radius-distanceToCenter)/4;
            bezierStartX = centerX - 2 * peakWidth + bezierStartShift;
            bezierStartY = centerY - distanceToCenter;
        }else{
            //50%
            peakWidth = radius / 2;
            peakHeight = (radius)/4;
            bezierStartX = centerX - 2 * peakWidth + bezierStartShift;
            bezierStartY = centerY ;
        }

        bezierPath.reset();
        bezierPath.moveTo(bezierStartX-4*peakWidth, bezierStartY);
        //画n组曲线，一组曲线为一个波峰和一个波谷
        int n =4;

        for(int i=0;i<n;i++) {
            //i=0时，往起始点左边画一组，为了达到动画衔接
            float pAx = bezierStartX + (i-1) * peakWidth * 2;
            float pAy = bezierStartY;

            float cABx = pAx + peakWidth / 2;
            float cABy = pAy - peakHeight;

            float pBx = pAx + peakWidth;
            float pBy = pAy;

            float cBCx = pBx + peakWidth / 2;
            float cBCy = pBy + peakHeight;

            float pCx = pAx + peakWidth*2;
            float pCy = pAy;

            bezierPath.quadTo(cABx, cABy, pBx, pBy);
            bezierPath.quadTo(cBCx,cBCy,pCx,pCy);
            //如果画到最后一组曲线，则将这个曲线的围成的一个图形区域进行封闭
            if (i == n-1 ) {
                bezierPath.lineTo(pCx+getMeasuredWidth(),pCy);
                bezierPath.lineTo(pCx+getMeasuredWidth(),pCy+(float)getMeasuredHeight());
                bezierPath.lineTo(bezierStartX-getMeasuredWidth() ,bezierStartY+(float)getMeasuredHeight());
                bezierPath.lineTo(bezierStartX-getMeasuredWidth(),bezierStartY);
                bezierPath.close();

            }
        }

        canvas.drawPath(bezierPath,bezierPaint);

        canvas.restore();
        //绘制电量百分比
        String text = percent + "%";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, centerX - textWidth / 2,centerY-(textPaint.ascent()+textPaint.descent())/2, textPaint);

        canvas.drawCircle(centerX, centerY, radius, strokePaint);

    }

    public void startAnim() {
        valueAnimator = ValueAnimator.ofFloat(0, peakWidth*2 );
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bezierStartShift = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(1500);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    public void stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator.pause();
            valueAnimator.cancel();
        }
    }

    public void setPercent(int p) {
        if (p < 0) {
            percent = 0;
        } else if (p > 100) {
            percent = 100;
        } else {
            this.percent = p;
        }

        postInvalidate();
        startAnim();
    }

    public int getPercent() {
        return percent;
    }

    private void log(String s) {
        Log.i("tag","PercentCircleView -- "+ s);
    }

}
