package com.ez_store.ez_store.View;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.ez_store.ez_store.R.layout.activity_add__items;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Add_Items extends AppCompatActivity {

    private StorageReference storageReference;
    private Uri ImgUri;
    private Connection connection;
    private static LoadingDialog loadingDialog;
    private static final int PICK_IMAGE = 1;
    EditText name, id,description,quantity,date,price;
    String Name,  _barcode,_description,_quantity , _price , _id;
    Date _date;
    ImageView Image;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Button scan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_add__items);
        _id = getIntent().getStringExtra("id");
        connection = new Connection(Add_Items.this);
        loadingDialog = new LoadingDialog(Add_Items.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        name = findViewById(R.id.name_item);
        id = findViewById(R.id.id_item);
        description = findViewById(R.id.descrption_item);
        quantity = findViewById(R.id.quantity_item);
        date = findViewById(R.id.Date_item);
        price = findViewById(R.id.price_item);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Person");

        Image  = findViewById(R.id.Item_pic);
        Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


        scan=findViewById(R.id.button4);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator INT=new IntentIntegrator(
                        Add_Items.this
                );
                INT.setPrompt("Press volume up for flash");
                INT.setBeepEnabled(true);
                INT.setOrientationLocked(true);
                INT.setCaptureActivity(CaptureActivity.class);
                INT.initiateScan();
            }
        });
    }



    public void AddItems(View view) {

        setImage(ImgUri);
        Toast.makeText(this, "ITEM ADDED SUCCESSFULLY", Toast.LENGTH_SHORT).show();

    }

    private void openGallery() {
        if (connection.isConnected) {
            loadingDialog.start();
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(gallery, PICK_IMAGE);
        } else {
            Toast.makeText(Add_Items.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Add_Items.this.RESULT_OK && data != null && data.getData() != null) {
            ImgUri = data.getData();
            Image.setImageURI(ImgUri);
            loadingDialog.dismiss();
        }

        IntentResult IR = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data
        );

        try {
            if (IR.getContents() != null) {
                id.setText(IR.getContents());
                Toast.makeText(Add_Items.this, IR.getContents(), Toast.LENGTH_LONG).show();

            } else
                Toast.makeText(getApplicationContext(), "Make sure your bar code is aligned", Toast.LENGTH_LONG).show();
        } catch (Exception e) {

        }
    }


    private void setImage(Uri imgUri) {
        ContentResolver cr = Add_Items.this.getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + map.getExtensionFromMimeType(cr.getType(imgUri)));

        ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            if (Image != null) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(Image.toString().replace(",", "."));
                                storageReference.delete();
                            }
                            loadingDialog.dismiss();
                        } catch (Exception ex) {
                            Toast.makeText(Add_Items.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                        Name = name.getText().toString().trim();
                        _barcode = id.getText().toString().trim();
                        _description = description.getText().toString().trim();
                        _quantity = quantity.getText().toString().trim();
                        _price = price.getText().toString().trim();
                        try {
                            _date = new SimpleDateFormat("dd/MM/yyyy").parse(date.getText().toString().trim());
                        } catch (ParseException e) {
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }


                        Product Product = new Product(_barcode, Name, _description, uri.toString().replace(".", ","), _price, _quantity, _date);

                        databaseReference.child(_id).child("Products").child(_barcode).setValue(Product);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Add_Items.this, "Failed", Toast.LENGTH_LONG).show();
            }
        });
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