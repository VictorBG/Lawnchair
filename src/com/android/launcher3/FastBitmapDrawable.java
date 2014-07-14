/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

class FastBitmapDrawable extends Drawable {

    private static final ColorMatrix sTempSaturationMatrix = new ColorMatrix();
    private static final ColorMatrix sTempBrightnessMatrix = new ColorMatrix();

    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Bitmap mBitmap;
    private int mAlpha;

    private float mSatutation = 1;
    private int mBrightness = 0;

    FastBitmapDrawable(Bitmap b) {
        mAlpha = 255;
        mBitmap = b;
        setBounds(0, 0, b.getWidth(), b.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect r = getBounds();
        // Draw the bitmap into the bounding rect
        canvas.drawBitmap(mBitmap, null, r, mPaint);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // No op
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setFilterBitmap(boolean filterBitmap) {
        mPaint.setFilterBitmap(filterBitmap);
        mPaint.setAntiAlias(filterBitmap);
    }

    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @Override
    public int getMinimumWidth() {
        return getBounds().width();
    }

    @Override
    public int getMinimumHeight() {
        return getBounds().height();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public float getSaturation() {
        return mSatutation;
    }

    public void setSaturation(float saturation) {
        mSatutation = saturation;
        updateFilter();
    }

    public int getBrightness() {
        return mBrightness;
    }

    public void addBrightness(int amount) {
        mBrightness += amount;
        updateFilter();
    }

    public void setBrightness(int brightness) {
        mBrightness = brightness;
        updateFilter();
    }

    private void updateFilter() {
        if (mSatutation != 1 || mBrightness != 0) {
            sTempSaturationMatrix.setSaturation(mSatutation);

            if (mBrightness != 0) {
                // Brightness: C-new = C-old*(1-amount) + amount
                float scale = 1 - mBrightness / 255.0f;
                sTempBrightnessMatrix.setScale(scale, scale, scale, 1);
                float[] array = sTempBrightnessMatrix.getArray();

                // Add the amount to RGB components of the matrix, as per the above formula.
                // Fifth elements in the array correspond to the constant being added to
                // red, blue, green, and alpha channel respectively.
                array[4] = mBrightness;
                array[9] = mBrightness;
                array[14] = mBrightness;
                sTempSaturationMatrix.preConcat(sTempBrightnessMatrix);
            }
            mPaint.setColorFilter(new ColorMatrixColorFilter(sTempSaturationMatrix));
        } else {
            mPaint.setColorFilter(null);
        }
    }
}
