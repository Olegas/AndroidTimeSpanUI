package ru.elifantiev.android.timespan;


import java.sql.Time;
import java.util.*;

public class TimeSpanGroupCollection {

    public static String toString(Collection<TimeSpanGroup> groupCollection) {
        StringBuilder builder = new StringBuilder();
        for(TimeSpanGroup group : groupCollection) {
            builder.append(group.toString()).append("|");
        }
        return builder.toString();
    }

    public static Collection<TimeSpanGroup> valueOf(String serialized) {
        if(serialized == null || "".equals(serialized))
            return Collections.emptySet();

        ArrayList<TimeSpanGroup> result = new ArrayList<TimeSpanGroup>();
        String[] groups = serialized.split("\\|");
        for(String group : groups) {
            if(!"".equals(group)) {
                result.add(TimeSpanGroup.valueOf(group));
            }
        }
        return result;
    }

    /**
     * Возвращает минимальное время ожидания до момента, когда данная группа будет актуальна
     * @param groupCollection группа
     * @return Минимальное время ожидания в минутах.
     * Возвращает Integer.MAX_VALUE если группа пустая
     * Возвращает 0 если группа активна в данный момент
     */
    public static int getMinutesTillBecomeActive(Collection<TimeSpanGroup> groupCollection) {
        int minWait = Integer.MAX_VALUE;
        for(TimeSpanGroup group : groupCollection) {
            int thisWait = group.minutesTillBecomeAvailable();
            if(thisWait < minWait)
                minWait = thisWait;
        }
        return minWait;
    }

    public static int tillMaxContinuousRangeEnd(Set<TimeSpanGroup> groupCollection) {
        int left = 0, right = -1;
        Calendar cal = Calendar.getInstance();
        int mNow = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        for(TimeSpanGroup group : groupCollection) {
            if(group.isToday()) {
                for(TimeSpan span : group.getCollection()) {
                    int sLeft = span.getTimeFrom();
                    int sRight = span.getTimeTo();
                    if(sLeft <= mNow && mNow <= sRight) {
                        if(right == -1) { // first active span ever
                            left = sLeft;
                            right = sRight;
                        } else {
                            if((left <= sLeft && sLeft <= right) || (sLeft <= left && left <= sRight))
                                right = sRight;
                        }
                    }
                }
            }
        }
        return right > 0 ? right - mNow : right;
    }

}
