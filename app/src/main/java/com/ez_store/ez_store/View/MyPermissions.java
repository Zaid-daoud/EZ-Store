package com.ez_store.ez_store.View;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyPermissions extends AppCompatActivity {

    private TextView permissions ,caption;
    private ImageView empty;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private static final String PERSON = "Person";
    private static final String PERMISSIONS = "Permissions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_permissions);
        permissions = (TextView) findViewById(R.id.employeePermissions);
        caption = (TextView) findViewById(R.id.caption_text);
        empty = (ImageView) findViewById(R.id.empty);
        reference = FirebaseDatabase.getInstance().getReference(PERSON);
        auth = FirebaseAuth.getInstance();

        reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Permissions").exists()){
                  empty.setVisibility(View.GONE);
                  String message = "";
                  int counter = 0;
                  for(DataSnapshot snap : snapshot.child(PERMISSIONS).getChildren()){
                      message += (++counter)+"- "+ snap.child("perm_name").getValue(String.class).replace("Permission","")+"\n\n";
                  }
                  permissions.setText(message);
                  caption.setText(snapshot.child("userName").getValue(String.class) + "'s Permissions");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyPermissions.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}