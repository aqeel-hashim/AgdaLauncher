package com.suraiya.agdalauncher.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.suraiya.agdalauncher.R;
import com.suraiya.agdalauncher.adapters.viewholders.AppListViewHolder;
import com.suraiya.agdalauncher.dao.FirebaseManager;
import com.suraiya.agdalauncher.model.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Suraiya on 11/30/2017.
 */

public class AppListAdapter extends RecyclerView.Adapter<AppListViewHolder> implements AppListViewHolder.AppListSelectionListner {

    Set<App> apps;
    private Context context;
    private boolean isFullList;
    private Set<App> selectedApps;

    public AppListAdapter(Set<App> apps, Context context, boolean isFullList) {
        this.apps = apps;
        this.context = context;
        this.isFullList = isFullList;
        selectedApps = new HashSet<>();
    }

    public AppListAdapter(Set<App> apps, Set<App> slectedApps, Context context, boolean isFullList) {
        this.apps = apps;
        this.context = context;
        this.isFullList = isFullList;
        this.selectedApps = slectedApps;
        Log.d("LLLLLLLL", "AppListAdapter: "+selectedApps.size());
        selectedApps = new HashSet<>();
    }

    @Override
    public AppListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppListViewHolder(LayoutInflater.from(context).inflate(R.layout.app_list_element,parent, false));
    }

    @Override
    public void onBindViewHolder(AppListViewHolder holder, int position) {
        holder.setAppListSelectionListner(this);
        boolean exists = false;
        for(App app : FirebaseManager.currentChild.getAppList()){
            if(((App)apps.toArray()[position]).getPackageName().equals(app.getPackageName())){
                exists = true;
                selectedApps.add(((App)apps.toArray()[position]));
                break;
            }
        }
        holder.bindView(((App)apps.toArray()[position]),isFullList, context, exists);

    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public Set<App> getSelectedApps(){ return selectedApps; }

    @Override
    public void AppSelected(App app) {
        selectedApps.add(app);
    }

    @Override
    public void AppDeselected(App app) {
        selectedApps.remove(app);
    }
}
