package ru.elifantiev.android.timespan;


import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;

class VisualDaysSelector {

    private final DrawLayer days = new DrawLayer();
    private final Paint pText;

    private final RectF[] dayBoxes = new RectF[7];
    private final RectF[] checkSizes = new RectF[7];
    private int selected = 0;
    private RectF boundaries;
    private final Bitmap checkOn, checkOff;
    private final String[] labels;
    private final int weekStart;
    private final Rect checkDimensions;

    VisualDaysSelector(Context ctx) {

        labels = ctx.getResources().getStringArray(ru.elifantiev.android.timespan.R.array.day_labels);

        if(labels.length != 7)
            throw new IllegalArgumentException("Invalid labels array specified in library resources");

        pText = new Paint();
        pText.setStrokeWidth(2);
        pText.setTextAlign(Paint.Align.CENTER);
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
        days.drawOn(canvas, 0, 0);
    }

    void onSizeChanged(int totalW, int totalH, RectF boundaries) {
        this.boundaries = boundaries;
        pText.setTextSize(Math.min(boundaries.width(), boundaries.height() / 7) / 4);
        days.onSizeChange(totalW, totalH);
        precalcBoxes();
        drawDays();
    }

    private void precalcBoxes() {
        float tH = boundaries.height();
        float btnW = Math.min(boundaries.width(), tH / 7);
        float spaceH = (tH - (btnW * 7)) / 6;

        for(int i = 0; i < 7; i++) {
            float topPt = (btnW + spaceH) * i;
            dayBoxes[i] = new RectF(boundaries.left, boundaries.top + topPt, boundaries.right, boundaries.top + topPt + btnW);
            checkSizes[i] = new RectF(
                                boundaries.left + (boundaries.right - boundaries.left) / 2 - (2 * checkOff.getWidth() / 3),
                                boundaries.top + topPt,
                                boundaries.right - (boundaries.right - boundaries.left) / 2 + (2 * checkOff.getWidth() / 3),
                                 boundaries.top + topPt + (4 * checkOff.getHeight() / 3));
        }
    }

    void release() {
        days.release();
    }

    private void drawDays() {
        days.reset();
        Canvas canvas = days.getCanvas();
        for(int i = weekStart; i < 7 + weekStart; i++) {
            RectF dayBox = dayBoxes[i - weekStart];
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
            if(dayBoxes[i - weekStart].contains(event.getX(), event.getY()))
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
