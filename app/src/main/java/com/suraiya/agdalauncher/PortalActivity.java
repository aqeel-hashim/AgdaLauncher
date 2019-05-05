package com.suraiya.agdalauncher;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.suraiya.agdalauncher.adapters.AppListAdapter;
import com.suraiya.agdalauncher.dao.FirebaseManager;
import com.suraiya.agdalauncher.model.App;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortalActivity extends AppCompatActivity {

    private RecyclerView appList;
    private AlertDialog pinDialog;
    private View dialogView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);
        appList = (RecyclerView) findViewById(R.id.appList);
        FirebaseManager.getInstance(this).getApplist(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<App> apps = new HashSet<App>();
                final PackageManager pm = getApplicationContext().getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                if(dataSnapshot != null && dataSnapshot.getChildren() != null && dataSnapshot.getChildren().iterator() != null && dataSnapshot.getChildren().iterator().hasNext() && dataSnapshot.getChildren().iterator().next() != null )
                    dataSnapshot.getChildren().forEach((v) -> {
                        if(v != null && v.getValue() != null && v.getValue(App.class) != null) {
                            App app = v.getValue(App.class);
                            packages.forEach((k) -> {
                                if (k.packageName.equals(app.getPackageName())) {
                                    app.setIcon(pm.getApplicationIcon(k));
                                }
                            });
                            apps.add(app);
                        }
                    });
                AppListAdapter adapter = new AppListAdapter(apps, PortalActivity.this, false);
                appList.setAdapter(adapter);
                appList.setLayoutManager(new GridLayoutManager(PortalActivity.this,3));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        dialogView = LayoutInflater.from(this).inflate(R.layout.pin,null,false);
        pinDialog = new AlertDialog.Builder(this).setView(dialogView).create();

        FirebaseManager.currentChild.startLocationThread(this);
        checkUsageAccess();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUsageAccess();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUsageAccess();
    }

    public void checkUsageAccess(){
        boolean granted = false;

        AppOpsManager appOps = (AppOpsManager) PortalActivity.this
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), PortalActivity.this.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (PortalActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        if(!granted){
            new AlertDialog.Builder(PortalActivity.this).setTitle("Agda Launcher").setMessage("Please allow usage access for Agda").setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).setPositiveButton("YES", (dialogInterface, i) -> {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }).create().show();
        }
    }

    public void AppListBtnClick(View view) {
        pinDialog.show();
        pinDialog.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseManager.getInstance(PortalActivity.this).checkPin(((TextView) pinDialog.findViewById(R.id.editText1)).getText().toString(), new FirebaseManager.PinCheckListener() {
                    @Override
                    public void Success() {
                        startActivity(new Intent(PortalActivity.this, AppListActivity.class));
                        pinDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void Failure() {
                        Toast.makeText(PortalActivity.this,"Invalid Pin", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void SettingsBtnClicked(View view) {
        pinDialog.show();
        pinDialog.findViewById(R.id.button1).setOnClickListener(view1 -> FirebaseManager.getInstance(PortalActivity.this).checkPin(((TextView) pinDialog.findViewById(R.id.editText1)).getText().toString(), new FirebaseManager.PinCheckListener() {
            @Override
            public void Success() {
                startActivity(new Intent(PortalActivity.this, SettingsActivity.class));
                pinDialog.dismiss();
                finish();
            }

            @Override
            public void Failure() {
                Toast.makeText(PortalActivity.this,"Invalid Pin", Toast.LENGTH_LONG).show();
            }
        }));
    }

    public void ExitBtnClicked(View view) {
        pinDialog.show();
        pinDialog.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseManager.getInstance(PortalActivity.this).checkPin(((TextView) pinDialog.findViewById(R.id.editText1)).getText().toString(), new FirebaseManager.PinCheckListener() {
                    @Override
                    public void Success() {
                        pinDialog.dismiss();
                        new AlertDialog.Builder(PortalActivity.this).setTitle("Exiting Agda Launcher").setMessage("Are You Sure").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                getPackageManager().clearPackagePreferredActivities(getPackageName());
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(startMain);
                            }
                        }).create().show();

                    }

                    @Override
                    public void Failure() {
                        Toast.makeText(PortalActivity.this,"Invalid Pin", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
