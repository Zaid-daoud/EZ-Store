package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.ez_store.ez_store.Controller.MyEmployeeAdapter;
import com.ez_store.ez_store.Controller.ProductsAdapter;
import com.ez_store.ez_store.Model.Person;
import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.Model.SessionManager;
import com.ez_store.ez_store.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class openInventory extends AppCompatActivity {

    RecyclerView Product;
    ProductsAdapter productsAdapter;
    private final static String PERSON = "Person";
    private DatabaseReference reference;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_inventory);
        id = getIntent().getStringExtra("id");
        showProduct();

    }
    private void showProduct() {
        reference = FirebaseDatabase.getInstance().getReference(PERSON).child(id).child("Products");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Product> products = new ArrayList<>();
                for(DataSnapshot snap : snapshot.getChildren()) {
                    final String name = snap.child("name").getValue(String.class);
                    final String barcode = snap.child("barCode").getValue(String.class);
                    final String desc = snap.child("description").getValue(String.class);
                    final String img = snap.child("imgUri").getValue(String.class);
                    final String price = snap.child("price").getValue(double.class).toString();
                    final String quantity = snap.child("quantity").getValue(int.class).toString();
                    String date = snap.child("expiredDate").getValue(String.class);

                    try {
                        products.add(new Product(barcode,name,desc,img,price,quantity,new SimpleDateFormat("dd/MM/yyyy").parse(date)));
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }

                Product = (RecyclerView) findViewById(R.id.products);
                Product.setLayoutManager(new LinearLayoutManager(openInventory.this));
                productsAdapter = new ProductsAdapter(products);
                Product.setAdapter(productsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(openInventory.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

}