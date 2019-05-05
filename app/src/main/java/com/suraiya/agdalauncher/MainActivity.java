package com.suraiya.agdalauncher;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.suraiya.agdalauncher.dao.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView emailTxt;
    private EditText passwordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailTxt = (AutoCompleteTextView) findViewById(R.id.email);
        passwordTxt = (EditText) findViewById(R.id.password);
    }

    public void loginBtnClick(View view) {
        Toast.makeText(this, "Login Start", Toast.LENGTH_SHORT).show();
        FirebaseManager.getInstance(this).signIn(emailTxt.getText().toString(), passwordTxt.getText().toString(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(MainActivity.this,PinActivity.class));
                }else{
                    Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
