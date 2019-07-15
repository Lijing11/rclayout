/*
 * Copyright 2018 GcsSloop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 2018-04-13 23:18:02
 *
 * GitHub: https://github.com/GcsSloop
 * WeiBo: http://weibo.com/GcsSloop
 * WebSite: http://www.gcssloop.com
 */

package com.gcssloop.widget.helper;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import com.gcssloop.rclayout.R;

import java.util.ArrayList;

/**
 * 作用：圆角辅助工具
 * 作者：GcsSloop
 */
public class RCHelper {
    public float[] radii = new float[8];   // top-left, top-right, bottom-right, bottom-left
    public Path mClipPath;                 // 剪裁区域路径
    public Paint mPaint;                   // 画笔
    public boolean mRoundAsCircle = false; // 圆形
    public int mStrokeColor;               // 描边颜色
    public int mBackgroundColor;           // 背景颜色
    public int mStrokeWidth;               // 描边半径
    public boolean mClipBackground;        // 是否剪裁背景
    public RectF mAreaRegion;             // 内容区域
    public RectF mLayer;                   // 画布图层大小
    public int mStrokeNormalColor;
    public int mStrokePressedColor;
    public int mStrokeSelectedColor;
    public int mStrokeEnabledColor;
    public int mBackgroundNormalColor;
    public int mBackgroundPressedColor;
    public int mBackgroundSelectedColor;
    public int mBackgroundEnabledColor;


    public void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RCAttrs);
        mRoundAsCircle = ta.getBoolean(R.styleable.RCAttrs_round_as_circle, false);
        mClipBackground = ta.getBoolean(R.styleable.RCAttrs_clip_background, true);

        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.RCAttrs_stroke_width, 0);
        mStrokeNormalColor = ta.getColor(R.styleable.RCAttrs_stroke_normal_color,Color.TRANSPARENT);
        mStrokeEnabledColor = ta.getColor(R.styleable.RCAttrs_stroke_enabled_color,mStrokeNormalColor);
        mStrokePressedColor = ta.getColor(R.styleable.RCAttrs_stroke_pressed_color,mStrokeNormalColor);
        mStrokeSelectedColor = ta.getColor(R.styleable.RCAttrs_stroke_selected_color,mStrokeNormalColor);
        mStrokeColor = mStrokeNormalColor;

        mBackgroundNormalColor = ta.getColor(R.styleable.RCAttrs_background_normal_color,Color.TRANSPARENT);
        mBackgroundEnabledColor = ta.getColor(R.styleable.RCAttrs_background_enabled_color,mBackgroundNormalColor);
        mBackgroundPressedColor = ta.getColor(R.styleable.RCAttrs_background_pressed_color,mBackgroundNormalColor);
        mBackgroundSelectedColor = ta.getColor(R.styleable.RCAttrs_background_selected_color,mBackgroundNormalColor);
        mBackgroundColor = mBackgroundNormalColor;

        int roundCorner = ta.getDimensionPixelSize(R.styleable.RCAttrs_round_corner, 0);

        int roundCornerTopLeft = ta.getDimensionPixelSize(
                R.styleable.RCAttrs_round_corner_top_left, roundCorner);
        int roundCornerTopRight = ta.getDimensionPixelSize(
                R.styleable.RCAttrs_round_corner_top_right, roundCorner);
        int roundCornerBottomLeft = ta.getDimensionPixelSize(
                R.styleable.RCAttrs_round_corner_bottom_left, roundCorner);
        int roundCornerBottomRight = ta.getDimensionPixelSize(
                R.styleable.RCAttrs_round_corner_bottom_right, roundCorner);
        ta.recycle();

        radii[0] = roundCornerTopLeft;
        radii[1] = roundCornerTopLeft;

        radii[2] = roundCornerTopRight;
        radii[3] = roundCornerTopRight;

        radii[4] = roundCornerBottomRight;
        radii[5] = roundCornerBottomRight;

        radii[6] = roundCornerBottomLeft;
        radii[7] = roundCornerBottomLeft;

        mLayer = new RectF();
        mClipPath = new Path();
        mAreaRegion = new RectF();
        mPaint = new Paint();
        mPaint.setColor(mBackgroundColor);
        mPaint.setAntiAlias(true);
    }

    public void onSizeChanged(View view, int w, int h) {
        mLayer.set(0, 0, w, h);
        refreshRegion(view);
    }

    public void refreshRegion(View view) {
        int w = (int) mLayer.width();
        int h = (int) mLayer.height();
        mClipPath.reset();
        if (mRoundAsCircle) {
            float d = w >= h ? h : w;
            float r = d / 2-mStrokeWidth;
            PointF center = new PointF(w / 2, h / 2);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                mClipPath.addCircle(center.x, center.y, r, Path.Direction.CW);
                mClipPath.moveTo(0, 0);  // 通过空操作让Path区域占满画布
                mClipPath.moveTo(w, h);
            } else {
                float y = h / 2 - r;
                mClipPath.moveTo(0, y);
                mClipPath.addCircle(center.x, y + r, r, Path.Direction.CW);
            }
        } else {
            RectF storke = new RectF();
                storke.left = mStrokeWidth;
                storke.top = mStrokeWidth;
                storke.right = w-mStrokeWidth;
                storke.bottom = h -mStrokeWidth;
            mClipPath.addRoundRect(storke, radii, Path.Direction.CW);
        }
        mAreaRegion.left = mStrokeWidth+view.getPaddingLeft();
        mAreaRegion.top = mStrokeWidth +view.getPaddingTop();
        mAreaRegion.right = w-mStrokeWidth-view.getPaddingRight();
        mAreaRegion.bottom = h - mStrokeWidth - view.getPaddingBottom();
    }

    public void drawBackGround(Canvas canvas){

        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        if(mClipBackground){
            canvas.drawPath(mClipPath, mPaint);
        }else {
            canvas.drawRect(mLayer, mPaint);
        }
    }
    public void drawStroke(Canvas canvas){
        if (mStrokeWidth > 0) {
            // 绘制描边
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            mPaint.setColor(mStrokeColor);
            mPaint.setStrokeWidth(mStrokeWidth * 2);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mClipPath, mPaint);
        }
    }

    public void onClipDraw(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawPath(mClipPath, mPaint);
        } else {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            final Path path = new Path();
            path.addRect(0, 0, (int) mLayer.width(), (int) mLayer.height(), Path.Direction.CW);
            path.op(mClipPath, Path.Op.DIFFERENCE);
            canvas.drawPath(path, mPaint);
        }

        if (mStrokeWidth > 0) {
            // 支持半透明描边，将与描边区域重叠的内容裁剪掉
//            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
//            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(mStrokeWidth * 2);
//            mPaint.setStyle(Paint.Style.STROKE);
//            canvas.drawPath(mClipPath, mPaint);
            // 绘制描边
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            mPaint.setColor(mStrokeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mClipPath, mPaint);
        }

    }




    public void drawableStateChanged(View view) {
        if (view instanceof RCAttrs) {
            if(!view.isEnabled()){
             mBackgroundColor = mBackgroundEnabledColor;
             mStrokeColor = mStrokeEnabledColor;
            }else if(view.isPressed()){
                mBackgroundColor = mBackgroundPressedColor;
                mStrokeColor = mStrokePressedColor;
            }else if(view.isSelected()){
                mBackgroundColor = mBackgroundSelectedColor;
                mStrokeColor = mStrokeSelectedColor;
            }else {
                mBackgroundColor = mBackgroundNormalColor;
                mStrokeColor = mStrokeNormalColor;
            }
            ((RCAttrs) view).onViewStatusChanged();
        }
    }


}
