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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;

import java.util.Set;
import java.util.TreeSet;

import static ru.elifantiev.android.timespan.DrawParameters.SIDE_PAD;


public class TimeSpanGroupEditor extends View implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private int viewportTop = 8 * 60 + 45;
    private int hoursOnScreen = 12;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private Paint pOuter, pLine;

    private TreeSet<VisualTimeSpan> displayedSpans = new TreeSet<VisualTimeSpan>();

    private RectF boundaries;

    private String[] labels = new String[24];

    private final DrawLayer scale = new DrawLayer();
    private boolean isMeasured = false;
    private VisualTimeSpan activeSpan = null;
    private VisualTimeSpan.HitTestResult activeSpanMode = VisualTimeSpan.HitTestResult.NOWHERE;
    private VisualDaysSelector daysSelector;
    private float dragStart = 0f;
    DrawParameters drawParameters;


    public TimeSpanGroupEditor(Context context) {
        super(context);
        init();
    }

    public TimeSpanGroupEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeSpanGroupEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), this);
        gestureDetector.setOnDoubleTapListener(this);
        //scaleGestureDetector = new ScaleGestureDetector(getContext(), this);

        drawParameters = new DrawParameters(getContext());

        pOuter = new Paint();
        pOuter.setColor(Color.GRAY);
        pOuter.setStrokeWidth(2);
        pOuter.setStyle(Paint.Style.STROKE);

        pLine = new Paint();
        pLine.setColor(Color.GRAY);
        pLine.setStrokeWidth(1);
        pLine.setTextSize(11 * drawParameters.density);
        pLine.setAntiAlias(true);
        pLine.setStyle(Paint.Style.FILL_AND_STROKE);

        for(int i = 0; i < 24; i++)
            labels[i] = String.format("%02d:00", i);

        daysSelector = VisualDaysSelector.newInstance(getContext());
    }

    private void changeViewport(float viewportDelta) {
        int desiredValue = Math.round(viewportTop + viewportDelta);
        if(desiredValue < 0)
            desiredValue = 0;
        else if(desiredValue > 1440 - hoursOnScreen * 60)
            desiredValue = 1440 - hoursOnScreen * 60;
        if(viewportTop != desiredValue) {
            int d = viewportTop - desiredValue;
            viewportTop = desiredValue;
            scale.reset();
            drawScale();
            for(VisualTimeSpan span : displayedSpans)
                span.setBounds(span.getUpperBound() + d, span.getLowerBound() + d);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*if (scaleGestureDetector.onTouchEvent(event)) {
            return true;
        } else*/ if (gestureDetector.onTouchEvent(event))
            return true;
        else {
            if(activeSpan != null && event.getAction() == MotionEvent.ACTION_UP) {
                if(checkOverlap())
                    invalidate();
                return true;
            }
            else
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        isMeasured = true;
        boundaries = new RectF(
            getPaddingLeft() + SIDE_PAD,
            getPaddingTop() + drawParameters.TB_PAD,
            w - getPaddingRight() - SIDE_PAD * 2 - drawParameters.DAY_SELECTOR_AREA_WIDTH,
            h - getPaddingBottom() - drawParameters.TB_PAD);

        scale.onSizeChange(w, h);
        drawScale();

        if(displayedSpans.size() == 0)
            appendSpan(VisualTimeSpan.newInstance(this));

        for(VisualTimeSpan span : displayedSpans)
            span.onSizeChanged(w, h, boundaries);

        daysSelector.onSizeChanged(w, h, new RectF(
            w - getPaddingRight() - SIDE_PAD - drawParameters.DAY_SELECTOR_AREA_WIDTH,
            getPaddingTop() + drawParameters.TB_PAD,
            w - getPaddingRight() - SIDE_PAD,
            h - getPaddingBottom() - drawParameters.TB_PAD
        ));

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        scale.drawOn(canvas, 0, 0);
        daysSelector.onDraw(canvas);
        for(VisualTimeSpan span : displayedSpans)
            span.onDraw(canvas);
    }

    private void drawScale() {
        Canvas canvas = scale.getCanvas();
        canvas.drawRect(boundaries, pOuter);

        int extraMin = (60 - (viewportTop % 60)) % 60;
        float offsetTop = minuteToPixelPoint(viewportTop + extraMin);

        for(int i = 0; i < hoursOnScreen; i++) {
            float offsetY = boundaries.top + offsetTop + boundaries.height() * i / hoursOnScreen;
            canvas.drawLine(boundaries.left, offsetY, boundaries.right, offsetY, pLine);
            canvas.drawText(
                    labels[viewportTop / 60 + (extraMin > 0 ? 1 : 0) + i],
                    boundaries.left + 10,
                    offsetY + drawParameters.SCALE_LABEL_TOP_PADDING,
                    pLine);
        }
    }

    public boolean onDown(MotionEvent motionEvent) {
        activeSpan = null;
        activeSpanMode = VisualTimeSpan.HitTestResult.NOWHERE;
        for(VisualTimeSpan span : displayedSpans) {
            VisualTimeSpan.HitTestResult hitTest = span.hitTest(motionEvent);
            if(hitTest != VisualTimeSpan.HitTestResult.NOWHERE) {
                activeSpanMode = hitTest;
                activeSpan = span;
                if(hitTest == VisualTimeSpan.HitTestResult.JUST_IN)
                    dragStart = motionEvent.getY();
                break;
            }
        }
        return
                activeSpan != null ||
                boundaries.contains(motionEvent.getX(), motionEvent.getY()) ||
                daysSelector.hitTest(motionEvent) >= 0;
    }

    public void onShowPress(MotionEvent motionEvent) {
        if(activeSpan != null)
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if(daysSelector.handleTap(motionEvent)) {
            invalidate();
            return true;
        }
        return false;
    }

    public boolean onScroll(MotionEvent start, MotionEvent finish, float dX, float dY) {
        if(activeSpan == null || !activeSpan.isEditMode()) {
            changeViewport(pixelAmountToMinutes(dY));
            return true;
        }
        float selectionTop = activeSpan.getUpperBound(), selectionBottom = activeSpan.getLowerBound();
        float val = alignValue(finish.getY());
        boolean alter = false;
        if(activeSpanMode == VisualTimeSpan.HitTestResult.TOP_KNOB) {
            val = checkUpperBound(val);
            if(alter = (boundaries.top <= val && val <= selectionBottom))
                selectionTop = val;
        }
        else if(activeSpanMode == VisualTimeSpan.HitTestResult.BOTTOM_KNOB) {
            val = checkLowerBound(val);
            if((alter = (selectionTop <= val && val <= boundaries.bottom)))
                selectionBottom = val;
        }
        else if(activeSpanMode == VisualTimeSpan.HitTestResult.JUST_IN) {
            float delta = val - dragStart;
            if(boundaries.top < activeSpan.getUpperBound() + delta &&
                    activeSpan.getLowerBound() + delta < boundaries.bottom) {
                selectionTop += delta;
                selectionBottom += delta;
            } else if(boundaries.top > selectionTop + delta) {
                selectionBottom -= selectionTop - boundaries.top;
                selectionTop = boundaries.top;
            } else if(selectionBottom + delta > boundaries.bottom) {
                selectionTop += boundaries.bottom - selectionBottom;
                selectionBottom = boundaries.bottom;
            }
            dragStart = val;
            alter = true;
        }


        if(alter) {
            displayedSpans.remove(activeSpan);
            activeSpan.setBounds(selectionTop, selectionBottom);
            displayedSpans.add(activeSpan);
            invalidate();
        }

        return alter;
    }

    public void onLongPress(MotionEvent motionEvent) {
        if(activeSpan != null) {
            activeSpan.toggleEditMode();
            activeSpan = null;
            invalidate();
        }
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    private float checkUpperBound(float toPoint) {
        return Math.max(boundaries.top, toPoint);
    }

    private float checkLowerBound(float toPoint) {
        return Math.min(boundaries.bottom, toPoint);
    }

    private float alignValue(float value) {
        return controlToScreen(alignToSpan(screenToControl(value)));
    }

    float screenToControl(float pixelVal) {
        return pixelVal - drawParameters.TB_PAD;
    }

    float controlToScreen(float pixelVal) {
        return pixelVal + drawParameters.TB_PAD;
    }

    float pixelPointToMinutes(float pixelVal) {
        return (pixelVal * (hoursOnScreen * 60) / boundaries.height()) + viewportTop;
    }

    float pixelAmountToMinutes(float pixelAmount) {
        return pixelPointToMinutes(pixelAmount) - viewportTop;
    }

    float minuteToPixelPoint(float minutes) {
        return (minutes - viewportTop) * boundaries.height() / (hoursOnScreen * 60);
    }

    /**
     * Возвращает ближайшую пиксельную точку кратную 5 минутам
     * @param val исходная пиксельная точка относительно контрола
     * @return ближайшая пиксельная точка кратная 5 минутам относительно контрола
     */
    private float alignToSpan(float val) {
        float minutesVal = pixelPointToMinutes(val);
        double fiveSpan = Math.floor(minutesVal / 5) + (Math.floor(minutesVal % 5) >=3 ? 1 : 0);
        return minuteToPixelPoint((float) (fiveSpan * 5));
    }

    private void appendSpan(VisualTimeSpan newOne) {
        displayedSpans.add(newOne);
        if(isMeasured)
            newOne.onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), boundaries);
    }

    public void clear() {
        Set<VisualTimeSpan> copy = new TreeSet<VisualTimeSpan>(displayedSpans);
        for(VisualTimeSpan span : copy)
            removeSpan(span);
    }

    private void removeSpan(VisualTimeSpan span) {
        if(displayedSpans.contains(span)) {
            span.release();
            displayedSpans.remove(span);
        }
    }

    public void setValue(TimeSpanGroup group) {
        clear();
        daysSelector.setSelectedDays(group.getDayMask());
        for(TimeSpan span : group.getCollection()) {
            appendSpan(VisualTimeSpan.fromSpan(this, span));
        }

    }

    public TimeSpanGroup getValue() {
        int days = daysSelector.getSelectedDays();
        TimeSpanGroup result;
        try {
            result = TimeSpanGroup.fromVisualSpanCollection(days, displayedSpans);
        } catch (IllegalArgumentException e) {
            Log.e("TimeSpanGroupEditor", "WTF??? Invalid day mask from VisualDaysSelector!");
            result = TimeSpanGroup.emptyEverydayGroup();
        }

        return result;
    }

    private boolean checkOverlap() {
        boolean alter = false;
        VisualTimeSpan current = null;
        if(displayedSpans.size() < 2)
            return false;
        Set<VisualTimeSpan> copySet = new TreeSet<VisualTimeSpan>(displayedSpans);
        for(VisualTimeSpan span : copySet) {
            if(current != null) {
                if(current.getArea().intersect(span.getArea())) {
                    alter = true;
                    removeSpan(current);
                    removeSpan(span);
                    appendSpan(current = current.join(span));
                } else {
                    current = span;
                }
            }
            else
                current = span;
        }
        return alter;
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        if(boundaries.contains(motionEvent.getX(), motionEvent.getY())) {
            for(VisualTimeSpan span : displayedSpans) {
                VisualTimeSpan.HitTestResult hitTestResult = span.hitTest(motionEvent);
                if(hitTestResult == VisualTimeSpan.HitTestResult.JUST_IN) {
                    if(displayedSpans.size() > 1) {
                        displayedSpans.remove(span);
                        invalidate();
                    }
                    return true;
                }
            }

            float minutesTapped = pixelPointToMinutes(screenToControl(motionEvent.getY()));
            appendSpan(VisualTimeSpan.newInstanceAtValues(this, minutesTapped - 90, minutesTapped + 90));
            invalidate();
            return true;
        }

        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if(scaleGestureDetector.getScaleFactor() > 2.0)
            return true;
        else
            return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        hoursOnScreen *= scaleGestureDetector.getScaleFactor();
        invalidate();
    }
}
