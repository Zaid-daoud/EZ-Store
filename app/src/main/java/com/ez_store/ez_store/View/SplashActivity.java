package com.ez_store.ez_store.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private Connection connection;
    private long backPressTime;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final String PERSON = "Person";
    private DatabaseReference reference;
    private static final int PICK_IMAGE = 1;


    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(PERSON);

        if (ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.INTERNET
                    , Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
        } else {
            if(user != null && user.isEmailVerified()){
                String userID = user.getUid();
                reference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            finish();
                            startActivity( new Intent(SplashActivity.this, HomeActivity.class));
                            Toast.makeText(SplashActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        connection = new Connection(this);
    }

    public void goToLoginActivity(View view) {
        if(connection.isConnected){
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
        else {
            Toast.makeText(SplashActivity.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_LONG).show();
        }
    }

    public void goToRegisterActivity(View view) {
        if(connection.isConnected){
            Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
        else {
            Toast.makeText(SplashActivity.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            this.overridePendingTransition(R.anim.slide_up_out, R.anim.slide_up_in);
            return;
        }
        else{
            Toast.makeText(SplashActivity.this,"Press back again to exit", Toast.LENGTH_LONG).show();
        }
        backPressTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.registerNetworkCallback();
    }

    @Override
    protected void onPause() {
        connection.unregisterNetworkCallback();
        super.onPause();
    }
}