package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;


public class Damaged_Items extends AppCompatActivity implements View.OnClickListener {
    private Button scan , add_btn ;
    private EditText id , quantity ;

    private String _id;
    private String _quantity ;
    private String userID;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Connection connection;
    private static LoadingDialog loadingDialog;

    //public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    //private final static String default_notification_channel_id = "default" ;
   // Button btnDate ;
   // final Calendar myCalendar = Calendar. getInstance () ;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damaged__items);

        userID = getIntent().getStringExtra("id");
        connection = new Connection(Damaged_Items.this);
        loadingDialog = new LoadingDialog(Damaged_Items.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        scan = findViewById(R.id.barcode_icon);
        id = findViewById(R.id.barcode_view);
        quantity = findViewById(R.id.id_quantity);

        add_btn = findViewById(R.id.btn_add);


        scan.setOnClickListener(this);


 //------------------- Add Button ------------------------------------------------------------------
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                _id = id.getText().toString().trim();
                _quantity = quantity.getText().toString().trim();



                databaseReference.child(userID).child("Products").child(_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int Q = snapshot.child("quantity").getValue(Integer.class);
                        databaseReference.child(userID).child("Products").child(_id).child("quantity").setValue(Q-(Integer.parseInt(_quantity)));
                        databaseReference.child(userID).child("Products").child("DamagedItems").child(_id).child("id").setValue(_id);
                        databaseReference.child(userID).child("Products").child("DamagedItems").child(_id).child("quantity").setValue(Integer.parseInt(_quantity));
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


    }



    //------------------------------------------- Scan Code ----------------------------------------
    @Override
    public void onClick(View view) {
        scanCode();
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                id.setText(result.getContents());

                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scanCode();
                    }
                }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.cancel();
            } else {
                Toast.makeText(this, "No Result", Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}