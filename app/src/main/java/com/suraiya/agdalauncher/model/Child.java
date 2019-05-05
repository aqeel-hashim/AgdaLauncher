package com.suraiya.agdalauncher.model;

import android.Manifest;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.suraiya.agdalauncher.dao.FirebaseManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Suraiya on 11/30/2017.
 */

public class Child{
    private String UUID;
    private long pinTime;
    private String pin;
    private Set<App> appList;
    private DateRange range;

    public Child(long pinTime, String pin) {
        this.pinTime = pinTime;
        this.pin = pin;
        appList = new HashSet<>();
    }

    public Child(){
        appList = new HashSet<>();
        range = new DateRange();
    }

    public DateRange getRange() {
        return range;
    }

    public void setRange(DateRange range) {
        this.range = range;
    }

    public long getPinTime() {
        return pinTime;
    }

    public void setPinTime(long pinTime) {
        this.pinTime = pinTime;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Set<App> getAppList() {
        return appList;
    }

    public void setAppListWithString(Context context, ArrayList<String> appList) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        UsageStatsManager usm = (UsageStatsManager) context
                .getSystemService(context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usm
                .queryUsageStats(0, cal.getTimeInMillis(),
                        System.currentTimeMillis());
        for (UsageStats qsm : queryUsageStats) {
            if(appList.contains(qsm.getPackageName())){
                App app = null;
                try {
                    final PackageManager pm = context.getApplicationContext().getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo( qsm.getPackageName(), 0);
                    if(ai != null) {
                        app = new App(qsm.getPackageName()
                                , qsm
                                , pm.getApplicationIcon(ai)
                                , pm.getApplicationLabel(ai).toString());
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                this.appList.add(app);
            }
        }
    }

    public void setAppListWithApp(Set<App> appList) {
        this.appList = appList;
    }

    public void addApp(App app) {
        appList.add(app);
    }

    public void removeApp(App app) {
        Iterator<App> iterator = appList.iterator();
        while (iterator.hasNext()) {
            App curr = iterator.next();
            if (app.getPackageName().equals(curr.getPackageName())) {
                appList.remove(curr);
            }
        }
    }

    public void startLocationThread(final Activity context){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    FirebaseManager.getInstance(context).updateLocation(getLastLocation(context));



                    Log.d(Child.class.getSimpleName(), "run: "+getLastLocation(context).toString());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public com.suraiya.agdalauncher.model.Location getLastLocation(Activity context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                List<String> providers = locationManager.getProviders(true);
                Location bestLocation = null;
                for (String provider : providers) {
                    Location l = locationManager.getLastKnownLocation(provider);

                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        bestLocation = l;
                    }
                }
                System.out.println("1::"+bestLocation);
                System.out.println("2::"+bestLocation.getLatitude());
                return new com.suraiya.agdalauncher.model.Location(bestLocation);
            }
        }
        return null;
    }

    private static class LastTimeLaunchedComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getLastTimeUsed(), left.getLastTimeUsed());
        }
    }
}
