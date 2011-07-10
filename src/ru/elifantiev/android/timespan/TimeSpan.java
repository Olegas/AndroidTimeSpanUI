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


import java.util.Calendar;

public class TimeSpan implements Comparable<TimeSpan> {

    private final int timeFrom, timeTo;

    final static TimeSpan FULL_DAY = new TimeSpan(0, 1440);

    int getTimeFrom() {
        return timeFrom;
    }

    int getTimeTo() {
        return timeTo;
    }

    TimeSpan(int timeFrom, int timeTo) {
        this.timeFrom = Math.max(0, timeFrom);
        this.timeTo = Math.min(1440, timeTo);
    }

    public TimeSpan newTimeSpan(int from, int to) {
        return new TimeSpan(from, to);
    }

    public static TimeSpan valueOf(String serialized) {
        int timeSplit = serialized.indexOf('-');

        if(timeSplit == -1)
            throw new IllegalArgumentException("Trying to parse from wrong format");

        int timeFrom, timeTo;
        try {
            timeFrom = Integer.valueOf(serialized.substring(0, timeSplit));
            timeTo = Integer.valueOf(serialized.substring(timeSplit + 1));

            if(timeFrom > timeTo)
                throw new IllegalArgumentException("Ranges are reversed");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch(StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return new TimeSpan(timeFrom, timeTo);
    }

    static int getCurrentMinutes(Calendar cal) {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        return hour * 60 + min;
    }

    public int minutesTillBecomeActual(Calendar cal) {
        if(isActual(cal))
            return 0;
        else {
            int val = getCurrentMinutes(cal);
            return timeFrom - val;
        }
    }

    public boolean isActual(Calendar cal) {
        int val = getCurrentMinutes(cal);
        return timeFrom <= val && val <= timeTo;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(timeFrom).append("-").append(timeTo).toString();
    }

    public String toReadableString() {
        int hT = (int)Math.floor(timeFrom / 60f);
        int hB = (int)Math.floor(timeTo / 60f);
        int mT = (int)Math.floor(timeFrom % 60f);
        int mB = (int)Math.floor(timeTo % 60f);
        StringBuilder builder = new StringBuilder();
        builder.append(hT < 10 ? "0" : "").append(hT).append(":").append(mT < 10 ? "0" : "").append(mT).append(" - ");
        builder.append(hB < 10 ? "0" : "").append(hB).append(":").append(mB < 10 ? "0" : "").append(mB);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpan timeSpan = (TimeSpan) o;

        return timeFrom == timeSpan.timeFrom && timeTo == timeSpan.timeTo;
    }

    @Override
    public int hashCode() {
        int result = 17 + timeFrom;
        result = 31 * result + timeTo;
        return result;
    }

    public int compareTo(TimeSpan timeSpan) {
        int delta = timeFrom - timeSpan.timeFrom;
        if(delta != 0)
            return delta;

        return timeTo - timeSpan.timeTo;
    }
}
