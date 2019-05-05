package com.suraiya.agdalauncher;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.suraiya.agdalauncher.adapters.AppListAdapter;
import com.suraiya.agdalauncher.dao.FirebaseManager;
import com.suraiya.agdalauncher.model.App;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppListActivity extends AppCompatActivity {

    private RecyclerView appList;
    private AppListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        appList = (RecyclerView) findViewById(R.id.appListSelect);
        Set<App> showApps = new HashSet<>();

        final PackageManager pm = this.getApplicationContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d("JJJJJJJJJJJJJJJ", "onCreate: "+packages.size());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        UsageStatsManager lUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String,UsageStats> queryUsageStats = lUsageStatsManager
                .queryAndAggregateUsageStats(cal.getTimeInMillis(),
                        System.currentTimeMillis());
        for (ApplicationInfo qsm : packages) {
            App app = null;
            Log.d("JJJJJJJJJJJJJJJ", "onCreate: "+qsm.packageName);
            UsageStats stats = null;
            stats = queryUsageStats.get(qsm.packageName);
            if( ((qsm.flags & ApplicationInfo.FLAG_SYSTEM) == 0)) {
                app = new App(qsm.packageName
                        , stats
                        , pm.getApplicationIcon(qsm)
                        , pm.getApplicationLabel(qsm).toString());

                showApps.add(app);
            }
        }



        adapter = new AppListAdapter(showApps,this, true);
        appList.setAdapter(adapter);
        appList.setLayoutManager(new GridLayoutManager(this,3));
    }

    public void AppProceedBtnClick(View view) {
        FirebaseManager.currentChild.setAppListWithApp(adapter.getSelectedApps());
        FirebaseManager.getInstance(this).updateUsageStats(FirebaseManager.currentChild.getAppList());
        startActivity(new Intent(this, PortalActivity.class));
        finish();

    }
}
