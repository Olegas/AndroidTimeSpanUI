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
