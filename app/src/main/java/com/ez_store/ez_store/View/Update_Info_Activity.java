package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Update_Info_Activity extends AppCompatActivity {


    String name , id , image , description , quantity , price , date;

    EditText item_name , item_description , item_quantity , item_price , item_date;
    ImageView back , item_image;
    Button update;

    private StorageReference storageReference;
    private Uri ImgUri;
    private Connection connection;
    private static LoadingDialog loadingDialog;
    private static final int PICK_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update__info_);

        name = getIntent().getStringExtra("Name");
        id = getIntent().getStringExtra("ID");
        description = getIntent().getStringExtra("Description");
        image = getIntent().getStringExtra("Image");
        quantity = getIntent().getStringExtra("Quantity");
        price = getIntent().getStringExtra("Price");
        date = getIntent().getStringExtra("ExpiredDate");



        item_name = findViewById(R.id.name_item);
        item_description = findViewById(R.id.descrption_item);
        item_quantity = findViewById(R.id.quantity_item);
        item_price = findViewById(R.id.price_item);
        item_date = findViewById(R.id.date_item);
        back = findViewById(R.id.add_cust_back_button);
        item_image = findViewById(R.id.image_item);
        update = findViewById(R.id.update_btn);


        item_name.setHint(name);
        item_description.setHint(description);
        item_quantity.setHint(quantity);
        item_price.setHint(price);
        item_date.setHint(date);
        Picasso.get().load(image).into(item_image);

        connection = new Connection(Update_Info_Activity.this);
        loadingDialog = new LoadingDialog(Update_Info_Activity.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        item_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person")
                        .child(auth.getCurrentUser().getUid()).child("Products").child(id);

                if(item_name.getText().toString().isEmpty() == false)
                {
                    reference.child("name").setValue(item_name.getText().toString().trim());
                }
                if(item_description.getText().toString().isEmpty() == false)
                {
                    reference.child("description").setValue(item_description.getText().toString().trim());
                }
                if(item_quantity.getText().toString().isEmpty() == false)
                {
                    reference.child("quantity").setValue(Integer.parseInt(item_quantity.getText().toString().trim()));
                }
                if(item_price.getText().toString().isEmpty() == false)
                {
                    reference.child("price").setValue(Double.parseDouble(item_price.getText().toString().trim()));
                }
                if(item_date.getText().toString().isEmpty() == false)
                {
                    try {
                        reference.child("date").setValue(new SimpleDateFormat("dd/MM/yyyy").parse(item_price.getText().toString().trim()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                reference.child("imgUri").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(image != snapshot.getValue(String.class))
                        {
                            setImage(ImgUri , snapshot , reference);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
    }

    private void openGallery() {
        //if (connection.isConnected) {
        loadingDialog.start();
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery, PICK_IMAGE);
        // } else {
        Toast.makeText(Update_Info_Activity.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        // }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Update_Info_Activity.this.RESULT_OK && data != null && data.getData() != null) {

            ImgUri = data.getData();
            item_image.setImageURI(ImgUri);
            loadingDialog.dismiss();

        }
    }

    private void setImage(Uri imgUri, DataSnapshot snapshot, DatabaseReference reference) {

        ContentResolver cr = Update_Info_Activity.this.getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + map.getExtensionFromMimeType(cr.getType(imgUri)));

        ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.getValue(String.class).replace(",", "."));
                            storageReference.delete();


                            reference.child("imgUri").setValue(uri.toString().replace("." , ","));

                            loadingDialog.dismiss();
                        } catch (Exception ex) {
                            Toast.makeText(Update_Info_Activity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Update_Info_Activity.this, "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }
}