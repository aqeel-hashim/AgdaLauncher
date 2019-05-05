package com.suraiya.agdalauncher.model;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;

import java.util.Date;

/**
 * Created by Suraiya on 11/30/2017.
 */

public class App {
    private String packageName;
    private UsageStats stats;
    @Exclude
    private Drawable icon;
    private String AppName;

    App(){
        packageName = "";
        stats = null;
        icon = null;
        AppName = "";
    }

    public App(String packageName, UsageStats stats, Drawable icon, String appName) {
        this.packageName = packageName;
        this.stats = stats;
        this.icon = icon;
        AppName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public UsageStats getStats() {
        return stats;
    }

    public void setStats(UsageStats stats) {
        this.stats = stats;
    }

    @Exclude
    public Drawable getIcon() {
        return icon;
    }

    @Exclude
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }


}
