package com.suraiya.agdalauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amirarcane.lockscreen.activity.EnterPinActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.suraiya.agdalauncher.dao.FirebaseManager;
import com.suraiya.agdalauncher.model.App;
import com.suraiya.agdalauncher.model.DateRange;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HomeActivity extends Activity {
    private ProgressDialog dialog;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 88888888;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loging, please wait.");
        dialog.setCancelable(false);
        dialog.show();
        checkLogin();

    }

    public void checkLogin(){
        Log.d("TTTTTTTTTT", "checkLoginActivity: start");
        FirebaseManager.getInstance(this).checkLogin(exists -> {
            if(!exists) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                Log.d("TTTTTTTTTT", "onCreate: checklogin true");
            }
            else{
                checkUsageAccess();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseManager.getInstance(this).updateUsageStats(FirebaseManager.currentChild.getAppList());
        dialog.setMessage("Loging, please wait.");
        dialog.show();
        checkLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        final boolean[] check = {false};



        FirebaseManager.currentChild.getAppList().forEach((app -> {
            if(app.getPackageName().equals(getForegroundApp())) check[0] = true;
        }));

        Toast.makeText(this, getForegroundApp(), Toast.LENGTH_SHORT).show();

        try {
            if (new ForegroundCheckTask().execute(this).get()) {
    //            Intent intent = new Intent(HomeActivity.this, EnterPinActivity.class);
    //            startActivity(intent);
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                //PinViewHolder view = new PinViewHolder(LayoutInflater.from(this).inflate(R.layout.activity_enterpin,null,false),true, this);


                Log.d("TTTTTTTTTT", "doInBackground: ");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            List<String> strList = FirebaseManager.currentChild.getAppList().stream()
                    .map( App::getPackageName )
                    .collect(Collectors.toList());
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                Log.d("GGGGGGG", "isAppOnForeground: "+appProcess.processName);
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && !appProcess.processName.equals(packageName) && !strList.contains(appProcess.processName)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        dialog.setMessage("Loging, please wait.");
        dialog.show();
        checkLogin();
    }

    public void checkUsageAccess(){
        boolean granted = false;
        Log.d("TTTTTTTTTT", "checkUsageAccess: ");
        AppOpsManager appOps = (AppOpsManager) HomeActivity.this
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), HomeActivity.this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (HomeActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        if(!granted){
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog)).setTitle("Agda Launcher").setMessage("Please allow usage access for Agda").setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).setPositiveButton("YES", (dialogInterface, i) -> {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }).create().show();
        }
    }

    public void homeBtnClick(View view) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(FirebaseManager.currentChild.getRange().getStartTime());
        //formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        formatter.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName()));
        Date dateEnd = new Date(FirebaseManager.currentChild.getRange().getEndTime());
        int startreturnFormat = Integer.parseInt(formatter.format(date).split(":")[0]); //Final Result.
        int startreturnFormatminut = Integer.parseInt(formatter.format(date).split(":")[1]); //Final Result.
        int endreturnFormat = Integer.parseInt(formatter.format(dateEnd).split(":")[0]); //Final Result.
        int endreturnFormatminut = Integer.parseInt(formatter.format(dateEnd).split(":")[1]); //Final Result.
        final Calendar c = Calendar.getInstance();
        Date datecurrent = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
                Locale.ENGLISH);
        String time = dateFormat.format(datecurrent);
        Log.d("TESTTIMEFUUCK", "onDataChange: " + Integer.parseInt(time.split(":")[0]) + ":" + Integer.parseInt(time.split(":")[1]) + "  >  " + startreturnFormat + ":" + startreturnFormatminut + "  &&   " + Integer.parseInt(time.split(":")[0]) + ":" + Integer.parseInt(time.split(":")[1]) + "<" + endreturnFormat + ":" + endreturnFormatminut);
        long currenttime = (Integer.parseInt(time.split(":")[0]) * 60) + Integer.parseInt(time.split(":")[1]);
        long starttime = (startreturnFormat * 60) + startreturnFormatminut;
        long endtime = (endreturnFormat * 60) + endreturnFormatminut;
        if (currenttime > starttime && currenttime < endtime) {
            Log.d("TESTTIMEFUUCK", "doInBackground: canUSe" +
                    "");
            startActivity(new Intent(this, PortalActivity.class));
        }

    }

    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestPermission() {
        Toast.makeText(this, "Need to request permission", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "resultCode " + resultCode);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                if (!hasPermission()){
                    requestPermission();
                }
                break;
        }
        if(resultCode == RESULT_OK && requestCode == 89076){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 90234);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public String getForegroundApp() {
        String currentApp = "NULL";
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList != null && appList.size() > 0) {
            TreeMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }

        return currentApp;
    }

    private String querySettingPkgName() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }

        return resolveInfos.get(0).activityInfo.packageName;
    }

}
