package com.suraiya.agdalauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.suraiya.agdalauncher.dao.FirebaseManager;
import com.suraiya.agdalauncher.model.Child;

public class PinActivity extends Activity {

    private EditText pinTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        pinTxt = (EditText) findViewById(R.id.editTextPin);
    }

    public void pinBtnClick(View view) {
        FirebaseManager.getInstance(this).addPin(pinTxt.getText().toString());
        startActivity(new Intent(this, HomeActivity.class));
    }
}
