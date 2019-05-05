package com.suraiya.agdalauncher.dao;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suraiya.agdalauncher.model.App;
import com.suraiya.agdalauncher.model.Child;
import com.suraiya.agdalauncher.model.DateRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Suraiya on 11/30/2017.
 */

public class FirebaseManager implements FirebaseAuth.AuthStateListener{
    private static String UUID;
    public static Child currentChild;
    private static FirebaseManager instance;
    private FirebaseAuth mAuth;
    private Activity context;
    private FirebaseDatabase database;

    private FirebaseManager(Activity context){
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
        Log.d("TTTTTTTTTT", "FirebaseManager: start");
        database = FirebaseDatabase.getInstance();
    }

    public static FirebaseManager getInstance(Activity context){
        if(instance == null)
            instance = new FirebaseManager(context);
        return instance;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            UUID = user.getUid();
        }else{

        }
    }

    public void signIn(String email,String password, OnCompleteListener<AuthResult> completeListener){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(context, completeListener);
    }

    public void updateLocation(com.suraiya.agdalauncher.model.Location location){
        DatabaseReference reference = database.getReference(UUID).child("location");
        reference.setValue(location);
    }

    public long getScreenPinTime(){
        final long[] time = new long[1];
        DatabaseReference reference = database.getReference(UUID).child("screenpintime");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                time[0] = dataSnapshot.getValue(long.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                time[0] = 0;
            }
        });
        return time[0];
    }

    public void setScreenPinRange(DateRange range){
        DatabaseReference reference = database.getReference(UUID).child("screenpinrange");
        reference.setValue(range);
    }

    public DatabaseReference getScreenPinRange(ValueEventListener listener){
        DatabaseReference reference = database.getReference(UUID).child("screenpinrange");
        reference.addValueEventListener(listener);
        return reference;
    }

    public void updateUsageStats(Set<App> apps){
        DatabaseReference reference = database.getReference(UUID).child("apps");
        reference.setValue(null);
        reference.setValue(Arrays.asList(apps.toArray(new App[apps.size()])));
    }

    public void getApplist(ValueEventListener eventListener){
        DatabaseReference reference = database.getReference(UUID).child("apps");
        reference.addValueEventListener(eventListener);
    }

    public interface PinCheckListener{
        public void Success();
        public void Failure();
    }

    public void checkPin(final String pin, final PinCheckListener listener){
        final DatabaseReference reference = database.getReference(UUID).child("pin");
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String parentPin = dataSnapshot.getValue(String.class);
                Log.d("HIIII", "onDataChange: "+parentPin);
                if(parentPin.equals(pin)){
                    listener.Success();
                    Log.d("HIIII", "onDataChange: fuk you.");
                }else{
                    listener.Failure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.Failure();
            }
        };
        reference.addValueEventListener(valueEventListener);
    }

    public void addPin(String pin){
        DatabaseReference reference = database.getReference(UUID).child("pin");
        reference.setValue(pin);
        if(currentChild == null){
            currentChild = new Child(getScreenPinTime(), pin);
        }else{
            currentChild.setPin(pin);
        }
    }

    public void checkLogin(LoginCheckComplete loginCheck){
        if(mAuth.getCurrentUser() != null){
            Log.d("TTTTTTTTTT", "checkLogin: start");
            UUID = mAuth.getCurrentUser().getUid();
            if(currentChild == null)
                currentChild = new Child();

            DatabaseReference dbreference = database.getReference(UUID).child("apps");
            dbreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("TTTTTTTTTT", "onDataChange: found worked cunt");
                    ValueEventListener current = this;
                    getScreenPinRange(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentChild.setRange(dataSnapshot.getValue(DateRange.class));
                            Log.d("TTTTTTTTTT", "onDataChange: worked range");
                            loginCheckComplete = (e) -> { dbreference.removeEventListener(current);};
                            loginCheckComplete.Complete(true);
                            loginCheck.Complete(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loginCheck.Complete(false);
                }
            });

        }else{
            loginCheck.Complete(false);
        }
    }

    public void signOut(){
        mAuth.signOut();
        UUID = "";
        currentChild = null;
    }

    public interface LoginCheckComplete{
        public void Complete(boolean exists);
    }

    private LoginCheckComplete loginCheckComplete;
}
