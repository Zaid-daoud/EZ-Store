package com.ez_store.ez_store.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ez_store.ez_store.Model.SessionManager;
import com.ez_store.ez_store.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private long backPressTime;
    private DrawerLayout drawer;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        auth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        //set header info
        setHeaderInfo(navigationView);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_myProfile, R.id.nav_termsAndConditions,R.id.nav_help,R.id.nav_rateUs)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> {
            Logout();
            return true;
        });
    }

    private void setHeaderInfo(NavigationView navigationView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
        reference.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                View headerView = navigationView.getHeaderView(0);
                TextView userName = headerView.findViewById(R.id.user_name);
                TextView userRole = headerView.findViewById(R.id.user_role);
                ImageView userImage = headerView.findViewById(R.id.user_image);
                userName.setText(snapshot.child("userName").getValue(String.class));
                userRole.setText(snapshot.child("role").getValue(String.class));

                if(snapshot.child("imgUri").getValue(String.class) == null){
                    Picasso.get().load(R.drawable.user_profile).into(userImage);
                }else {
                    Picasso.get().load(snapshot.child("imgUri").getValue(String.class).replace(",",".")).into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void Logout(){
        auth.signOut();
        Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
        Toast.makeText(HomeActivity.this, getResources().getString(R.string.signedOut), Toast.LENGTH_LONG).show();
        finish();
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_up_out, R.anim.slide_up_in);
    }

    @Override
    public void onBackPressed() {

    }
}