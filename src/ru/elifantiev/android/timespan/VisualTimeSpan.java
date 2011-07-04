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
import android.content.Context;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

import static ru.elifantiev.android.timespan.DrawParameters.KNOB_TOUCH_AREA;
import static ru.elifantiev.android.timespan.DrawParameters.MIDDLE_AREA_PAD;

class VisualTimeSpan implements Comparable<VisualTimeSpan> {

    private final DrawLayer selection = new DrawLayer();
    private final static Paint pSelectionBoundary = initSelectionPaint();
    private final static Paint pSelection = initSelectionPaint2();
    private final static Paint pSelKnob = initKnobPaint();
    private final Paint pSpanText;
    private final TimeSpanGroupEditor parent;
    private final float strokeWidth;
    private final Bitmap upArrow, downArrow;

    private float selectionTop = -1, selectionBottom = -1, xMiddlePoint;
    private RectF topKnobBoundary, bottomKnobBoundary, middleArea, fullArea;
    private RectF boundaries;
    private Rect bounds = new Rect();

    float minutesTop = 0, minutesBottom = 1440;

    private static Paint initSelectionPaint() {
        Paint r = new Paint();
        r.setColor(0xFF0000FF);
        r.setStrokeWidth(2);
        r.setStyle(Paint.Style.STROKE);
        return r;
    }

    private static Paint initSelectionPaint2() {
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
        return new TimeSpan((int)minutesTop, (int)minutesBottom);
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
        ret.minutesTop = minTop;
        ret.minutesBottom = minBottom;
        return ret;
    }

    float getUpperBound() {
        return selectionTop;
    }

    float getLowerBound() {
        return selectionBottom;
    }

    void setBounds(float upper, float lower) {
        selectionTop = upper;
        selectionBottom = lower;
        invalidate();
    }

    private void invalidate() {
        recalcBoundaries();
        drawSelection();
    }

    void release() {
        selection.release();
    }

    void onSizeChanged(int totalW, int totalH, RectF boundaries) {
        this.boundaries = boundaries;

        xMiddlePoint = boundaries.width() / 2;

        if (selectionTop == -1 && selectionBottom == -1) {
            selectionTop =  parent.controlToScreen(parent.minutesToPixel(minutesTop));
            selectionBottom = parent.controlToScreen(parent.minutesToPixel(minutesBottom));
        }

        selection.onSizeChange(totalW, totalH);
        recalcBoundaries();
        drawSelection();
    }

    void onDraw(Canvas canvas) {
        selection.drawOn(canvas, 0, 0);
    }

    private void recalcBoundaries() {

        topKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                selectionTop - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                selectionTop + KNOB_TOUCH_AREA);

        bottomKnobBoundary = new RectF(
                xMiddlePoint - KNOB_TOUCH_AREA,
                selectionBottom - KNOB_TOUCH_AREA,
                xMiddlePoint + KNOB_TOUCH_AREA,
                selectionBottom + KNOB_TOUCH_AREA);

        middleArea = new RectF(boundaries.left + MIDDLE_AREA_PAD, selectionTop + MIDDLE_AREA_PAD,
                boundaries.right - MIDDLE_AREA_PAD, selectionBottom - MIDDLE_AREA_PAD);

        fullArea = new RectF(boundaries.left, selectionTop, boundaries.right, selectionBottom);
    }

    private void drawSelection() {
        selection.reset();

        minutesTop = Math.round(parent.pixelToMinutes(parent.screenToControl(selectionTop)));
        minutesBottom = Math.round(parent.pixelToMinutes(parent.screenToControl(selectionBottom)));
        float minutesSelected = Math.round(parent.pixelToMinutes(selectionBottom - selectionTop));

        Canvas sCanvas = selection.getCanvas();
        sCanvas.drawRoundRect(
                new RectF(
                        boundaries.left,
                        selectionTop,
                        boundaries.right,
                        selectionBottom),
                10f,
                10f,
                pSelectionBoundary);


        sCanvas.drawRoundRect(
                new RectF(
                        boundaries.left + strokeWidth,
                        selectionTop + strokeWidth,
                        boundaries.right - strokeWidth,
                        selectionBottom - strokeWidth),
                10f,
                10f,
                pSelection);

//        sCanvas.drawRect(topKnobBoundary, pSelectionBoundary);
//        sCanvas.drawRect(bottomKnobBoundary, pSelectionBoundary);
//        sCanvas.drawRect(middleArea, pSelectionBoundary);

        sCanvas.drawBitmap(upArrow, xMiddlePoint - upArrow.getWidth() / 2, selectionTop - upArrow.getHeight(), pSelKnob);
        sCanvas.drawBitmap(downArrow, xMiddlePoint - downArrow.getWidth() / 2, selectionBottom, pSelKnob);

        sCanvas.drawText(
                new StringBuilder()
                        .append(toString())
                        .append(" (")
                        .append((int)Math.floor(minutesSelected / 60f))
                        .append("h ")
                        .append((int)Math.floor(minutesSelected % 60f))
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
            return HitTestResult.MIDDLE_DRAG;
        if (fullArea.contains(mX, mY))
            return HitTestResult.JUST_IN;
        return HitTestResult.NOWHERE;
    }

    RectF getArea() {
        return fullArea;
    }

    VisualTimeSpan join(VisualTimeSpan joining) {
        VisualTimeSpan span =  VisualTimeSpan.newInstance(parent);
        span.selectionTop = Math.min(selectionTop, joining.selectionTop);
        span.selectionBottom = Math.max(selectionBottom, joining.selectionBottom);
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
        int hT = (int)Math.floor(minutesTop / 60f);
        int hB = (int)Math.floor(minutesBottom / 60f);
        int mT = (int)Math.floor(minutesTop % 60f);
        int mB = (int)Math.floor(minutesBottom % 60f);
        StringBuilder builder = new StringBuilder();
        builder.append(hT < 10 ? "0" : "").append(hT).append(":").append(mT < 10 ? "0" : "").append(mT).append(" - ");
        builder.append(hB < 10 ? "0" : "").append(hB).append(":").append(mB < 10 ? "0" : "").append(mB);
        return builder.toString();
    }

    String getRawRange() {
        return String.format("%d-%d", (int)minutesTop, (int)minutesBottom);
    }

    static enum HitTestResult {
        TOP_KNOB, BOTTOM_KNOB, MIDDLE_DRAG, JUST_IN, NOWHERE
    }
}
