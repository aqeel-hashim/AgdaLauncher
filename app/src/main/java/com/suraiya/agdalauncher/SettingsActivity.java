package com.suraiya.agdalauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.suraiya.agdalauncher.dao.FirebaseManager;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void ChangePinBtnClicked(View view) {
        FirebaseManager.getInstance(this).signOut();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public void InstructionBtnClicked(View view) {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }
}
