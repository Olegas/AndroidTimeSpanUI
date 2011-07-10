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


import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;

class VisualDaysSelector {

    private final DrawLayer days = new DrawLayer();
    private final Paint pText;

    private final Rect[] dayBoxes = new Rect[7];
    private final Rect[] checkSizes = new Rect[7];
    private int selected = 0;
    private Rect boundaries;
    private final Bitmap checkOn, checkOff;
    private final String[] labels;
    private final int weekStart;
    private final Rect checkDimensions;

    VisualDaysSelector(Context ctx) {

        labels = ctx.getResources().getStringArray(ru.elifantiev.android.timespan.R.array.day_labels);

        if(labels.length != 7)
            throw new IllegalArgumentException("Invalid labels array specified in library resources");

        DrawParameters drawParameters = new DrawParameters(ctx);

        pText = new Paint();
        pText.setStrokeWidth(1);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(15 * drawParameters.density);
        pText.setAntiAlias(true);
        pText.setColor(0xFFFFFFFF);
        pText.setStyle(Paint.Style.FILL_AND_STROKE);

        weekStart = ctx.getResources().getInteger(ru.elifantiev.android.timespan.R.integer.weekStartOffset);

        checkOn = BitmapFactory.decodeResource(ctx.getResources(), android.R.drawable.checkbox_on_background);
        checkOff = BitmapFactory.decodeResource(ctx.getResources(), android.R.drawable.checkbox_off_background);

        checkDimensions = new Rect(0, 0, checkOff.getWidth(), checkOff.getHeight());
    }

    static VisualDaysSelector newInstance(Context ctx) {
        return new VisualDaysSelector(ctx);
    }

    public void onDraw(Canvas canvas) {
        days.drawOn(canvas, boundaries.left, boundaries.top);
    }

    void onSizeChanged(Rect boundaries) {
        this.boundaries = boundaries;
        //pText.setTextSize(Math.min(boundaries.width(), boundaries.height() / 7) / 4);
        days.onSizeChange(boundaries.width(), boundaries.height());
        precalcBoxes();
        drawDays();
    }

    private void precalcBoxes() {
        int tH = boundaries.height();
        int btnW = Math.min(boundaries.width(), tH / 7);
        int spaceH = (tH - (btnW * 7)) / 6;

        for(int i = 0; i < 7; i++) {
            int topPt = (btnW + spaceH) * i;
            dayBoxes[i] = new Rect(0, topPt, boundaries.width(), topPt + btnW);
            checkSizes[i] = new Rect(
                                (boundaries.width()) / 2 - (2 * checkOff.getWidth() / 3),
                                topPt,
                                boundaries.width() / 2 + (2 * checkOff.getWidth() / 3),
                                topPt + (4 * checkOff.getHeight() / 3));
        }
    }

    void release() {
        days.release();
    }

    private void drawDays() {
        days.reset();
        Canvas canvas = days.getCanvas();
        for(int i = weekStart; i < 7 + weekStart; i++) {
            Rect dayBox = dayBoxes[i - weekStart];
            canvas.drawBitmap(
                    (selected & (1 << (i % 7))) != 0 ? checkOn : checkOff,
                    checkDimensions,
                    checkSizes[i - weekStart],
                    pText);
            //canvas.drawRect(dayBox, (selected & (1 << i)) != 0 ? pSelected : pBoundary);
            canvas.drawText(labels[i % 7], dayBox.centerX(), dayBox.centerY() + pText.getTextSize(), pText);
        }
    }

    int hitTest(MotionEvent event) {
        for(int i = weekStart; i < 7 + weekStart; i++) {
            if(dayBoxes[i - weekStart].contains(
                    (int)event.getX() - boundaries.left,
                    (int)event.getY() - boundaries.top))
                return  i % 7;
        }
        return -1;
    }

    boolean handleTap(MotionEvent event) {
        int what = hitTest(event);
        if(what >= 0) {
            selected ^= (1 << what);
            drawDays();
            return true;
        }

        return false;
    }

    void setSelectedDays(int days) {
        selected = days;
    }

    int getSelectedDays() {
        return selected;
    }

}
