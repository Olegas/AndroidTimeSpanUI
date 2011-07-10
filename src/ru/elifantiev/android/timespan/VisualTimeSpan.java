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


import android.R;
import android.graphics.*;
import android.view.MotionEvent;

import static ru.elifantiev.android.timespan.DrawParameters.KNOB_TOUCH_AREA;
import static ru.elifantiev.android.timespan.DrawParameters.MIDDLE_AREA_PAD;

class VisualTimeSpan implements Comparable<VisualTimeSpan> {

    private final DrawLayer fullCanvas = new DrawLayer();
    private final DrawLayer scaleArea = new DrawLayer();
    private final static Paint pSelectionBoundary = initBoundaryPaint(false);
    private final static Paint pSelectionBoundaryEdit = initBoundaryPaint(true);
    private final static Paint pSelection = initInnerPaint();
    private final static Paint pSelKnob = initKnobPaint();
    private final Paint pSpanText;
    private final TimeSpanGroupEditor parent;
    private final float strokeWidth;
    private final Bitmap upArrow, downArrow;

    private float xMiddlePoint;
    private boolean editMode = false;
    private RectF topKnobBoundary, bottomKnobBoundary, middleArea, fullArea;
    private Rect boundaries;
    private Rect bounds = new Rect();

    private float pixelTop, pixelBottom;

    int minutesTop = 0, minutesBottom = 1440;

    private static Paint initBoundaryPaint(boolean isEdit) {
        Paint r = new Paint();
        r.setColor(isEdit ? 0xFFFF0000 : 0xFF0000FF);
        r.setStrokeWidth(2);
        r.setStyle(Paint.Style.STROKE);
        return r;
    }

    private static Paint initInnerPaint() {
        Paint p = new Paint();
        p.setColor(0x550000FF);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private static Paint initKnobPaint() {
        Paint r = new Paint();
        r.setColor(0x550000FF);
        r.setStyle(Paint.Style.FILL_AND_STROKE);
        return r;
    }

    private Paint initTextPaint(float density) {
        Paint r = new Paint();
        r.setStyle(Paint.Style.FILL_AND_STROKE);
        r.setTextSize(20 * density);
        r.setColor(0xFFFFFFFF);
        r.setAntiAlias(true);
        r.setTextAlign(Paint.Align.CENTER);
        return r;
    }

    TimeSpan toTimeSpan() {
        return new TimeSpan(minutesTop, minutesBottom);
    }

    private VisualTimeSpan(TimeSpanGroupEditor parent) {

        pSpanText = initTextPaint(parent.drawParameters.density);

        this.parent = parent;

        pSpanText.getTextBounds("0", 0, 1, bounds);
        strokeWidth = pSelectionBoundary.getStrokeWidth();

        upArrow = BitmapFactory.decodeResource(parent.getContext().getResources(), R.drawable.arrow_up_float);
        downArrow = BitmapFactory.decodeResource(parent.getContext().getResources(), R.drawable.arrow_down_float);
    }

    static VisualTimeSpan newInstance(TimeSpanGroupEditor parent) {
        return new VisualTimeSpan(parent);
    }

    static VisualTimeSpan fromSpan(TimeSpanGroupEditor parent, TimeSpan span) {
        VisualTimeSpan ret = new VisualTimeSpan(parent);
        ret.minutesTop = span.getTimeFrom();
        ret.minutesBottom = span.getTimeTo();
        return ret;
    }

    static VisualTimeSpan newInstanceAtValues(TimeSpanGroupEditor parent, float minTop, float minBottom) {
        VisualTimeSpan ret = new VisualTimeSpan(parent);
        ret.minutesTop = (int) Math.max(0, minTop);
        ret.minutesBottom = (int) Math.min(1440, minBottom);
        return ret;
    }

    boolean isEditMode() {
        return editMode;
    }

    void toggleEditMode() {
        setEditMode(!editMode);
    }

    void setEditMode(boolean editMode) {
        if (this.editMode != editMode) {
            this.editMode = editMode;
            invalidate();
        }
    }

    int getUpperBound() {
        return minutesTop;
    }

    int getLowerBound() {
        return minutesBottom;
    }

    void setBounds(int upper, int lower) {
        minutesTop = Math.max(0, upper);
        minutesBottom = Math.min(1440, lower);
        invalidate();
    }

    void invalidate() {
        recalcBoundaries();
        drawSelection();
    }

    void release() {
        fullCanvas.release();
        scaleArea.release();
    }

    void onSizeChanged(int totalW, int totalH, Rect boundaries) {
        this.boundaries = boundaries;

        xMiddlePoint = boundaries.width() / 2;

        fullCanvas.onSizeChange(totalW, totalH);
        scaleArea.onSizeChange(boundaries.width(), boundaries.height());
        recalcBoundaries();
        drawSelection();
    }

    void onDraw(Canvas canvas) {
        if (parent.isSpanVisible(this)) {
            if (editMode)
                fullCanvas.drawOn(canvas, 0, 0);
            scaleArea.drawOn(canvas, boundaries.left, boundaries.top);
        }
    }

    private void recalcBoundaries() {

        pixelTop = parent.minuteToPixelPoint(minutesTop);
        pixelBottom = parent.minuteToPixelPoint(minutesBottom);

        topKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                parent.controlToScreen(pixelTop) - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                parent.controlToScreen(pixelTop) + KNOB_TOUCH_AREA);

        bottomKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                parent.controlToScreen(pixelBottom) - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                parent.controlToScreen(pixelBottom) + KNOB_TOUCH_AREA);

        middleArea = new RectF(boundaries.left + MIDDLE_AREA_PAD, Math.max(0, pixelTop) + MIDDLE_AREA_PAD,
                boundaries.right - MIDDLE_AREA_PAD, Math.min(boundaries.height(), pixelBottom) - MIDDLE_AREA_PAD);
    }

    private void drawSelection() {
        fullCanvas.reset();
        scaleArea.reset();

        int minutesSelected = minutesBottom - minutesTop;

        Canvas sCanvas = scaleArea.getCanvas();
        sCanvas.drawRoundRect(
                new RectF(
                        0,
                        pixelTop,
                        boundaries.width(),
                        pixelBottom),
                10f,
                10f,
                editMode ? pSelectionBoundaryEdit : pSelectionBoundary);


        sCanvas.drawRoundRect(
                new RectF(
                        0 + strokeWidth,
                        pixelTop + strokeWidth,
                        boundaries.width() - strokeWidth,
                        pixelBottom - strokeWidth),
                10f,
                10f,
                pSelection);

        if (editMode) {
            Canvas fCanvas = fullCanvas.getCanvas();
            fCanvas.drawBitmap(upArrow, xMiddlePoint - upArrow.getWidth() / 2, parent.controlToScreen(pixelTop) - upArrow.getHeight(), pSelKnob);
            fCanvas.drawBitmap(downArrow, xMiddlePoint - downArrow.getWidth() / 2, parent.controlToScreen(pixelBottom), pSelKnob);
        }

        sCanvas.drawText(
                new StringBuilder()
                        .append(toString())
                        .append(" (")
                        .append(minutesSelected / 60)
                        .append("h ")
                        .append(minutesSelected % 60)
                        .append("m)").toString(),
                middleArea.centerX(),
                middleArea.centerY() + bounds.height() / 2,
                pSpanText);
    }

    HitTestResult hitTest(MotionEvent event) {
        float mX = event.getX();
        float mY = event.getY();
        if (topKnobBoundary.contains(mX, mY))
            return HitTestResult.TOP_KNOB;
        if (bottomKnobBoundary.contains(mX, mY))
            return HitTestResult.BOTTOM_KNOB;
        if (middleArea.contains(mX, mY))
            return HitTestResult.JUST_IN;
        return HitTestResult.NOWHERE;
    }

    boolean intersects(VisualTimeSpan other) {
        return (minutesTop < other.minutesTop && other.minutesTop < minutesBottom) ||
                (minutesTop < other.minutesBottom && other.minutesBottom < minutesBottom) ||
                (other.minutesTop < minutesTop && minutesTop < other.minutesBottom) ||
                (other.minutesTop < minutesBottom && minutesBottom < other.minutesBottom);
    }

    VisualTimeSpan join(VisualTimeSpan joining) {
        VisualTimeSpan span = VisualTimeSpan.newInstance(parent);
        span.minutesTop = Math.min(minutesTop, joining.minutesTop);
        span.minutesBottom = Math.max(minutesBottom, joining.minutesBottom);
        return span;
    }

    public int compareTo(VisualTimeSpan other) {
        int top = minutesTop - other.minutesTop;
        if (top != 0)
            return top;
        return minutesBottom - other.minutesBottom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualTimeSpan that = (VisualTimeSpan) o;

        return
                that.minutesBottom == minutesBottom &&
                        that.minutesTop == minutesTop;

    }

    @Override
    public int hashCode() {
        int result = minutesTop;
        result = 31 * result + minutesBottom;
        return result;
    }

    @Override
    public String toString() {
        int hT = minutesTop / 60;
        int hB = minutesBottom / 60;
        int mT = minutesTop % 60;
        int mB = minutesBottom % 60;
        StringBuilder builder = new StringBuilder();
        builder.append(hT < 10 ? "0" : "").append(hT).append(":").append(mT < 10 ? "0" : "").append(mT).append(" - ");
        builder.append(hB < 10 ? "0" : "").append(hB).append(":").append(mB < 10 ? "0" : "").append(mB);
        return builder.toString();
    }

    static enum HitTestResult {
        TOP_KNOB, BOTTOM_KNOB, JUST_IN, NOWHERE
    }
}
