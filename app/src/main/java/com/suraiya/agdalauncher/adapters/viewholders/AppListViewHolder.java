package com.suraiya.agdalauncher.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.suraiya.agdalauncher.R;
import com.suraiya.agdalauncher.model.App;

/**
 * Created by Suraiya on 11/30/2017.
 */

public class AppListViewHolder extends RecyclerView.ViewHolder{
    private ImageView appImage;
    private TextView appName;
    private View mainView;
    private boolean selectionToggle = false;
    private AppListSelectionListner listner;

    public AppListViewHolder(View itemView) {
        super(itemView);

        appImage = (ImageView) itemView.findViewById(R.id.AppImage);
        appName = (TextView) itemView.findViewById(R.id.AppName);
        mainView = itemView;
    }

    public void bindView(final App app, final boolean isFullList, final Context context,boolean selected){
        if(selected && isFullList){
            mainView.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
            selectionToggle = true;
            Log.d("LLLLLLL", "bindView: is selected");
        }else{
            mainView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            selectionToggle = false;
        }
        appImage.setImageDrawable(app.getIcon());
        appName.setText(app.getAppName());
        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFullList && !selectionToggle){
                    mainView.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
                    selectionToggle = true;
                    listner.AppSelected(app);
                }else if(isFullList){
                    mainView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
                    selectionToggle = false;
                    listner.AppDeselected(app);
                }else{
                    final PackageManager pm = context.getApplicationContext().getPackageManager();
                    Intent i = pm.getLaunchIntentForPackage(app.getPackageName());
                    Log.d("HHHHHH", "onClick: "+app.getPackageName());
                    context.startActivity(i);
                }
            }
        });
    }

    public void setAppListSelectionListner(AppListSelectionListner listner) {
        this.listner = listner;
    }

    public interface AppListSelectionListner {
        public void AppSelected(App app);
        public void AppDeselected(App app);
    }

}
