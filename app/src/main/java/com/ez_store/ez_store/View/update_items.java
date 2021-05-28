package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class update_items extends AppCompatActivity {

    public static TextView TVResult;
    Button update_btn;
    Button scan_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_items);

        TVResult = findViewById(R.id.barcode_view);
        scan_btn = findViewById(R.id.barcode_icon);
        update_btn = findViewById(R.id.btn_add);

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });
    }

//-------------------------------- Update Button ---------------------------------------------------

    private void update(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
        reference.child(auth.getCurrentUser().getUid()).child("Products").child(String.valueOf(Integer.parseInt(TVResult.getText().toString().trim())));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = new Intent(update_items.this, Update_Info_Activity.class);
                intent.putExtra("Name", snapshot.child("name").getValue(String.class));
                intent.putExtra("ID", snapshot.child("barCode").getValue(String.class));
                intent.putExtra("Description", snapshot.child("description").getValue(String.class));
                intent.putExtra("Image", snapshot.child("imgUri").getValue(String.class).replace(",","."));
                intent.putExtra("Quantity", String.valueOf(snapshot.child("quantity").getValue(int.class)));
                intent.putExtra("Price", String.valueOf(snapshot.child("price").getValue(double.class)));

                String date = snapshot.child("expiredDate").child("day").getValue(int.class).toString();
                date += "/"+snapshot.child("expiredDate").child("month").getValue(int.class).toString();
                date += "/"+snapshot.child("expiredDate").child("year").getValue(int.class).toString();
                intent.putExtra("ExpiredDate", date);
                startActivity(intent);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //------------------------------------------- Scan Code ----------------------------------------

    private void scanCode() {
        IntentIntegrator INT = new IntentIntegrator(this);
        INT.setCaptureActivity(CaptureActivity.class);
        INT.setOrientationLocked(false);
        INT.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        INT.setPrompt("Scanning code");
        INT.setBeepEnabled(true);
        INT.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                TVResult.setText(result.getContents());
            } else {
                Toast.makeText(this, "No Result", Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //----------------------------------------------------------------------------------------------
}