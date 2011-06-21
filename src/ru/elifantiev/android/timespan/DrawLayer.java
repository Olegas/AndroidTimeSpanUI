/*
 * Copyright 2011 Oleg Elifantiev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.elifantiev.android.timespan;

import android.graphics.Bitmap;
import android.graphics.Canvas;

class DrawLayer {
    private Bitmap bitmap;

    DrawLayer() {

    }

    public void reset() {
        if(bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap.Config cfg = bitmap.getConfig();
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(w, h, cfg);
        }
    }

    public void onSizeChange(int width, int height) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public Canvas getCanvas() {
        return new Canvas(bitmap);
    }

    public void drawOn(Canvas canvas, float x, float y) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    public void release() {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
}
