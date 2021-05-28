package com.ez_store.ez_store.View;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;


public class Damaged_Items extends AppCompatActivity implements View.OnClickListener {
    private Button scan , add_btn , view_btn;
    private EditText id , name , quantity , date;

    private String _name;
    private String _id;
    private String _quantity ;
    private String _date;
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

        scan = findViewById(R.id.barcode_icon);
        id = findViewById(R.id.barcode_view);
        quantity = findViewById(R.id.quantity_item);
        date = findViewById(R.id.id_date);
        add_btn = findViewById(R.id.btn_add);
        view_btn = findViewById(R.id.view_btn);

        scan.setOnClickListener(this);


 //------------------- Add Button ------------------------------------------------------------------
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                _name = name.getText().toString().trim();
                _id = id.getText().toString().trim();
                _quantity = quantity.getText().toString().trim();
                _date = date.getText().toString().trim();

                Product Product = new Product(_id , _name , _quantity , _date);
                databaseReference.child(userID).child("Products").child(_id).setValue(Product);
            }
        });
//-------------------- Add Button ------------------------------------------------------------------
        view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //viewDamagedItems();
            }
        });
    }

    /*private void AddDamagedItems(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
        reference.child(auth.getCurrentUser().getUid()).child("Products").child(String.valueOf(Integer.parseInt(id.getText().toString().trim())));
        reference.child(auth.getCurrentUser().getUid()).child("Products").child(String.valueOf(Integer.parseInt(quantity.getText().toString().trim())));
        reference.child(auth.getCurrentUser().getUid()).child("Products").child(String.valueOf(Integer.parseInt(date.getText().toString().trim())));
    }*/


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