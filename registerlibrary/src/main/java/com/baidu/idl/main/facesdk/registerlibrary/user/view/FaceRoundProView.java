/**
 * Copyright (C) 2017 Baidu Inc. All rights reserved.
 */
package com.baidu.idl.main.facesdk.registerlibrary.user.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.idl.main.facesdk.registerlibrary.user.utils.DensityUtils;


/**
 * 人脸检测区域View
 */
public class FaceRoundProView extends View {

    public static final float WIDTH_SPACE_RATIO = 0.3f; // 圆相当于占短边的3/5
    public static final float HEIGHT_RATIO = 0.1f;

    public static final int COLOR_BG = Color.parseColor("#121212");
    // public static final int COLOR_ROUND = Color.parseColor("#33CC83");

    private Paint mBGPaint;
    private Paint mFaceRoundPaint;
    private Paint mBitmapPaint;
    private Paint mTextPaint;
    private Paint mProgPaint;   // 绘制画笔

    private float mX;
    private float mY;
    private float mR;

    private Bitmap mBitmap;
    private String mTipText;

    private boolean isDrawProgress;
    public FaceRoundProView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBGPaint.setColor(COLOR_BG);
        mBGPaint.setStyle(Paint.Style.FILL);
        mBGPaint.setAntiAlias(true);
        mBGPaint.setDither(true);

        mFaceRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // mFaceRoundPaint.setColor(COLOR_ROUND);
        mFaceRoundPaint.setStyle(Paint.Style.FILL);
        mFaceRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mFaceRoundPaint.setAntiAlias(true);
        mFaceRoundPaint.setDither(true);

        // 初始化进度圆环画笔
        mProgPaint = new Paint();
        mProgPaint.setStyle(Paint.Style.STROKE);    // 只描边，不填充
        mProgPaint.setStrokeCap(Paint.Cap.ROUND);   // 设置圆角
        mProgPaint.setAntiAlias(true);              // 设置抗锯齿
        mProgPaint.setDither(true);                 // 设置抖动
        mProgPaint.setStrokeWidth(22);
        mProgPaint.setColor(Color.parseColor("#00BAF2"));

        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setColor(Color.RED);
        mBitmapPaint.setStrokeWidth(3);
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(DensityUtils.dip2px(getContext(), 22));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
    }

    public float getRound() {
        return mR;
    }

    public void setBitmapSource(int source , boolean isDrawProgress) {
        mBitmap = BitmapFactory.decodeResource(getResources(), source);
        this.isDrawProgress = isDrawProgress;
        invalidate();
    }

    public void setTipText(String tipText) {
        mTipText = tipText;
        if (!TextUtils.isEmpty(tipText)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float canvasWidth = right - left;
        float canvasHeight = bottom - top;
        if (canvasWidth >= canvasHeight) {
            // 横屏显示器
            float x = canvasWidth / 2;
            float y = (canvasHeight / 2) - ((canvasHeight / 2) * HEIGHT_RATIO);
            float r = canvasHeight * WIDTH_SPACE_RATIO;
            mX = x;
            mY = y;
            mR = r;
        } else {
            // 竖屏显示器
            float x = canvasWidth / 2;
            float y = canvasHeight / 2;
            float r = canvasWidth / 3;

            mX = x;
            mY = y;
            mR = r;
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 遮罩
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawPaint(mBGPaint);
        canvas.drawCircle(mX, mY, mR, mFaceRoundPaint);
        // 画图片
        if (mBitmap != null) {
            Rect src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());  // 原图片
            Rect dst = new Rect((int) (mX - mR - 30), (int) (mY - mR - 30),
                    (int) (mX + mR + 30), (int) (mY + mR + 30));                       // 目标图片
            canvas.drawBitmap(mBitmap, src, dst, mBitmapPaint);
        }
        // 画文字
        if (!TextUtils.isEmpty(mTipText)) {
            float instance = getBottom() - getTop() - mY;
            if (516 > instance) {  // 516为效果图尺寸间距，33为字体高度的一半
                canvas.drawText(mTipText, mX, mY + instance - 33, mTextPaint);
            } else {
                canvas.drawText(mTipText, mX, mY + 516, mTextPaint);
            }
        }
        if (isDrawProgress){
            RectF mRectF = new RectF((int) (mX - mR - 25), (int) (mY - mR - 25),
                    (int) (mX + mR + 25), (int) (mY + mR + 25));
            canvas.drawArc(mRectF, 275, 360 * 75 / 100,
                    false, mProgPaint);
        }

    }
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}