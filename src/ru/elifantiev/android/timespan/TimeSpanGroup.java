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
import android.content.res.Resources;
import android.util.Log;

import java.util.*;

public class TimeSpanGroup implements Comparable<TimeSpanGroup> {

    static final int SUNDAY = 1;         // Вс
    static final int MONDAY = 2;         // Пн
    static final int THUESDAY = 4;       // Вт
    static final int WEDNESDAY = 8;      // Ср
    static final int THURSDAY = 16;      // Чт
    static final int FRIDAY = 32;        // Пт
    static final int SATURDAY = 64;      // Сб
    static final int EVERYDAY = 127;

    private TreeSet<TimeSpan> storage = new TreeSet<TimeSpan>();
    private final int dayMask;

    TimeSpanGroup() {
        dayMask = EVERYDAY;
    }

    TimeSpanGroup(int dayMask) {
        this.dayMask = dayMask;
    }

    TimeSpanGroup(int dayMask, Collection<TimeSpan> spans) {
        this(dayMask);
        storage.addAll(spans);
    }

    public static TimeSpanGroup fromSpanCollection(int dayMask, Collection<TimeSpan> spans) throws IllegalArgumentException {
        if (dayMask < 0 || dayMask > EVERYDAY)
            throw new IllegalArgumentException("Incorrect days mask");

        return new TimeSpanGroup(dayMask, spans);
    }

    public static TimeSpanGroup anytimeGroup() {
        return new TimeSpanGroup(EVERYDAY, Arrays.asList(TimeSpan.FULL_DAY));
    }

    public static TimeSpanGroup fromVisualSpanCollection(int dayMask, Collection<VisualTimeSpan> spans) throws IllegalArgumentException {
        if (dayMask < 0 || dayMask > EVERYDAY)
            throw new IllegalArgumentException("Incorrect days mask");

        TimeSpanGroup result = new TimeSpanGroup(dayMask);
        for (VisualTimeSpan span : spans) {
            result.storage.add(span.toTimeSpan());
        }
        return result;
    }

    public static TimeSpanGroup emptyGroup(int dayMask) throws IllegalArgumentException {
        if (dayMask < 0 || dayMask > EVERYDAY)
            throw new IllegalArgumentException("Incorrect days mask");
        return new TimeSpanGroup(dayMask);
    }

    public static TimeSpanGroup emptyEverydayGroup() {
        return new TimeSpanGroup();
    }

    public static TimeSpanGroup valueOf(String serialized) throws IllegalArgumentException {
        int dayPart = serialized.indexOf(':');

        if (dayPart == -1)
            throw new IllegalArgumentException("Trying to parse from wrong format: " + serialized);

        int dayMask;
        String[] spans;
        try {
            dayMask = Integer.parseInt(serialized.substring(0, dayPart));
            spans = serialized.substring(dayPart + 1).split(",");

            if (dayMask < 0 || dayMask > EVERYDAY)
                throw new IllegalArgumentException("Invalid day mask");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        TimeSpanGroup result = new TimeSpanGroup(dayMask);

        for (String span : spans) {
            result.storage.add(TimeSpan.valueOf(span));
        }

        return result;
    }

    public int minutesTillBecomeAvailable() {
        return minutesTillBecomeAvailable(Calendar.getInstance());
    }

    public int minutesTillBecomeAvailable(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int myDays = (dayMask | (dayMask << 7)) >> dayOfWeek;
        int daysCount = -1;

        if (isToday(cal)) { // Сегодня группа активны
            if (isActual(cal)) // Может активны до сих пор?
                return 0;
            else { // Нет? Значит проверим когда ближайший период активности.
                int minSpanDelta = Integer.MAX_VALUE;
                for (TimeSpan span : storage) {
                    int tillThis = span.minutesTillBecomeActual(cal);
                    if (tillThis > 0 && minSpanDelta > tillThis)
                        minSpanDelta = tillThis;
                }
                if (minSpanDelta != Integer.MAX_VALUE) // no any
                    return minSpanDelta;
            }
        }

        // Сегодня не группа не активна? Найдем ближайший день когда будет
        for (int i = 0; i < 7; i++) {
            if ((myDays & (1 << i)) != 0) {
                daysCount = (i + 1) * 1440;
                break;
            }
        }

        if (daysCount == -1) {
            throw new IllegalStateException("Wrong time span state");
        }

        return daysCount + storage.first().getTimeFrom() - TimeSpan.getCurrentMinutes(cal);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(dayMask).append(":");
        Iterator<TimeSpan> i = storage.iterator();
        if (i.hasNext())
            while (true) {
                builder.append(i.next().toString());
                if (i.hasNext())
                    builder.append(",");
                else
                    break;
            }
        return builder.toString();
    }

    public boolean isToday() {
        return isToday(Calendar.getInstance());
    }

    boolean isToday(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return (((1 << (dayOfWeek - 1)) & dayMask) != 0);
    }

    public boolean isActual() {
        return isActual(Calendar.getInstance());
    }

    boolean isActual(Calendar cal) {
        if (isToday(cal)) {
            for (TimeSpan span : storage)
                if (span.isActual(cal))
                    return true;
        }
        return false;
    }

    public String toReadableString(Context ctx) {
        StringBuilder builder = new StringBuilder();
        Resources resources = ctx.getResources();
        String[] dayLabels = resources.getStringArray(ru.elifantiev.android.timespan.R.array.day_labels);
        boolean isFirst = true;
        if (dayMask == EVERYDAY)
            builder.append(resources.getString(R.string.everyday));
        else if (dayMask == 65)
            builder.append(resources.getString(R.string.weekend));
        else if (dayMask == 62)
            builder.append(resources.getString(R.string.weekdays));
        else {
            for (int i = 0; i < 7; i++) {
                int d1 = dayMask & (1 << i);
                if (d1 != 0) {
                    if (!isFirst)
                        builder.append(", ");
                    else
                        isFirst = false;
                    builder.append(dayLabels[i]);
                }
            }
        }
        builder.append(": ");
        isFirst = true;
        for (TimeSpan span : storage) {
            if (isFirst)
                isFirst = false;
            else
                builder.append(", ");
            builder.append(span.toReadableString());
        }
        return builder.toString();
    }

    int getDayMask() {
        return dayMask;
    }

    public TreeSet<TimeSpan> getCollection() {
        return new TreeSet<TimeSpan>(storage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpanGroup that = (TimeSpanGroup) o;

        return dayMask == that.dayMask && storage.equals(that.storage);
    }

    @Override
    public int hashCode() {
        int result = storage.hashCode();
        result = 31 * result + dayMask;
        return result;
    }

    public int compareTo(TimeSpanGroup timeSpanGroup) {
        if (dayMask != timeSpanGroup.dayMask) {
            for (int i = 0; i < 7; i++) {
                int d1 = dayMask & (1 << i);
                int d2 = timeSpanGroup.dayMask & (1 << i);
                if (d2 != d1)
                    return d1 - d2;
            }
        }

        Iterator<TimeSpan> iSelf = storage.iterator();
        Iterator<TimeSpan> iHis = timeSpanGroup.storage.iterator();

        while (iSelf.hasNext()) {
            TimeSpan my = iSelf.next();
            if (iHis.hasNext()) {
                TimeSpan his = iHis.next();
                int myStart = my.getTimeFrom(), hisStart = his.getTimeFrom();
                if (myStart != hisStart)
                    return hisStart - myStart;
                else {
                    int myEnd = my.getTimeTo(), hisEnd = his.getTimeTo();
                    if(myEnd != hisEnd)
                        return hisEnd - myEnd;
                }
            } else
                return 1;
        }
        return iHis.hasNext() ? -1 : 0;

    }
}
