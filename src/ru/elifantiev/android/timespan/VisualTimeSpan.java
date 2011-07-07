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

    private final DrawLayer selection = new DrawLayer();
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
    private RectF boundaries;
    private Rect bounds = new Rect();

    int minutesTop = 0, minutesBottom = 1440;

    private static Paint initBoundaryPaint(boolean isEdit) {
        Paint r = new Paint();
        r.setColor(isEdit ? 0xFFFF0000 : 0xFF0000FF);
        r.setStrokeWidth(2);
        r.setStyle(Paint.Style.STROKE);
        return r;
    }

    private static Paint initInnerPaint() {
        Paint p  = new Paint();
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
        Paint r  = new Paint();
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
        ret.minutesTop = (int)minTop;
        ret.minutesBottom = (int)minBottom;
        return ret;
    }

    boolean isEditMode() {
        return editMode;
    }

    void toggleEditMode() {
        setEditMode(!editMode);
    }

    void setEditMode(boolean editMode) {
        if(this.editMode != editMode) {
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
        minutesTop = upper;
        minutesBottom = lower;
        if(minutesTop < 0)
            minutesTop = 0;
        if(minutesBottom > 1440)
            minutesBottom = 1440;
        invalidate();
    }

    void invalidate() {
        recalcBoundaries();
        drawSelection();
    }

    void release() {
        selection.release();
    }

    void onSizeChanged(int totalW, int totalH, RectF boundaries) {
        this.boundaries = boundaries;

        xMiddlePoint = boundaries.width() / 2;

        selection.onSizeChange(totalW, totalH);
        recalcBoundaries();
        drawSelection();
    }

    void onDraw(Canvas canvas) {
        selection.drawOn(canvas, 0, 0);
    }

    private void recalcBoundaries() {

        float pixelTop = parent.controlToScreen(parent.minuteToPixelPoint(minutesTop));
        float pixelBottom = parent.controlToScreen(parent.minuteToPixelPoint(minutesBottom));
        topKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                pixelTop - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                pixelTop + KNOB_TOUCH_AREA);

        bottomKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                pixelBottom - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                pixelBottom + KNOB_TOUCH_AREA);

        middleArea = new RectF(boundaries.left + MIDDLE_AREA_PAD, Math.max(boundaries.top, pixelTop) + MIDDLE_AREA_PAD,
                boundaries.right - MIDDLE_AREA_PAD, Math.min(boundaries.bottom, pixelBottom) - MIDDLE_AREA_PAD);

        fullArea = new RectF(boundaries.left, pixelTop, boundaries.right, pixelBottom);
    }

    private void drawSelection() {
        selection.reset();

        float pixelTop = fullArea.top;
        float pixelBottom = fullArea.bottom;
        int minutesSelected = minutesBottom - minutesTop;

        Canvas sCanvas = selection.getCanvas();
        sCanvas.drawRoundRect(
                new RectF(
                        boundaries.left,
                        pixelTop,
                        boundaries.right,
                        pixelBottom),
                10f,
                10f,
                editMode ? pSelectionBoundaryEdit : pSelectionBoundary);


        sCanvas.drawRoundRect(
                new RectF(
                        boundaries.left + strokeWidth,
                        pixelTop + strokeWidth,
                        boundaries.right - strokeWidth,
                        pixelBottom - strokeWidth),
                10f,
                10f,
                pSelection);

//        sCanvas.drawRect(topKnobBoundary, pSelectionBoundary);
//        sCanvas.drawRect(bottomKnobBoundary, pSelectionBoundary);
//        sCanvas.drawRect(middleArea, pSelectionBoundary);

        if(editMode) {
            sCanvas.drawBitmap(upArrow, xMiddlePoint - upArrow.getWidth() / 2, pixelTop - upArrow.getHeight(), pSelKnob);
            sCanvas.drawBitmap(downArrow, xMiddlePoint - downArrow.getWidth() / 2, pixelBottom, pSelKnob);
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

    RectF getArea() {
        return fullArea;
    }

    VisualTimeSpan join(VisualTimeSpan joining) {
        VisualTimeSpan span =  VisualTimeSpan.newInstance(parent);
        span.minutesTop = Math.min(minutesTop, joining.minutesTop);
        span.minutesBottom = Math.max(minutesBottom, joining.minutesBottom);
        return span;
    }

    public int compareTo(VisualTimeSpan visualTimeSpan) {
        int top = Float.compare(minutesTop, visualTimeSpan.minutesTop);
        if (top != 0)
            return top;
        return Float.compare(minutesBottom, visualTimeSpan.minutesBottom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualTimeSpan that = (VisualTimeSpan) o;

        return
                Float.compare(that.minutesBottom, minutesBottom) == 0 &&
                Float.compare(that.minutesTop, minutesTop) == 0;

    }

    @Override
    public int hashCode() {
        int result = 17 + (minutesTop != +0.0f ? Float.floatToIntBits(minutesTop) : 0);
        result = 31 * result + (minutesBottom != +0.0f ? Float.floatToIntBits(minutesBottom) : 0);
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
