package com.suraiya.agdalauncher.model;

import java.util.concurrent.TimeUnit;

/**
 * Created by Suraiya on 12/14/2017.
 */

public class DateRange {
    private long startTime;
    private long endTime;

    public DateRange(){
        startTime = 0;
        endTime = 0;
    }

    public DateRange(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setValues(int startHour, int startMin, int endHour, int endMin){
        this.startTime = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(startHour) + TimeUnit.MINUTES.toSeconds(startMin));
        this.endTime = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(endHour) + TimeUnit.MINUTES.toSeconds(endMin));
    }
}
