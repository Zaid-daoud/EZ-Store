package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ez_store.ez_store.Controller.DeleteItemsAdapter;
import com.ez_store.ez_store.Model.DeleteItemsHelper;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeleteItems extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ImageButton search;
    private EditText edt_itemName;
    FirebaseAuth mAuth;
    DatabaseReference mRef;
    String itemName, itemQuantity, itemImage, itemId,id;
    private ArrayList<DeleteItemsHelper> deleteItemsHelperArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_items);
        mAuth = FirebaseAuth.getInstance();
        id = getIntent().getStringExtra("id");
        mRecyclerView = findViewById(R.id.rv_items);
        edt_itemName = findViewById(R.id.edt_itemName);
        search = findViewById(R.id.imgbtn_search);

        mRecyclerView.setLayoutManager(mLayoutManager);
        showAllItems();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n = edt_itemName.getText().toString();
                try {
                    mRef = FirebaseDatabase.getInstance().getReference("Person").child(id).child("Products");
                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                deleteItemsHelperArrayList = new ArrayList<>();
                                for (DataSnapshot d : snapshot.getChildren()) {
                                    itemName = d.child("name").getValue(String.class);
                                    if (n.equals(itemName)) {
                                        itemId=d.getKey();
                                        itemImage = d.child("imgUri").getValue(String.class);
                                        itemQuantity = d.child("quantity").getValue().toString();
                                        deleteItemsHelperArrayList.add(new DeleteItemsHelper(itemId,itemName, itemQuantity, itemImage));
                                    }
                                }
                            }
                            mRecyclerView.setHasFixedSize(true);
                            mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                            mAdapter = new DeleteItemsAdapter(deleteItemsHelperArrayList);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mRecyclerView.setAdapter(mAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void showAllItems() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference("Person").child(id).child("Products");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        deleteItemsHelperArrayList = new ArrayList<>();
                        for (DataSnapshot d : snapshot.getChildren()) {
                            itemId=d.getKey();
                            itemName = d.child("name").getValue(String.class);
                            itemImage = d.child("imgUri").getValue(String.class);
                            itemQuantity = d.child("quantity").getValue().toString();
                            deleteItemsHelperArrayList.add(new DeleteItemsHelper(itemId,itemName, itemQuantity, itemImage));

                        }
                    }
                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    mAdapter = new DeleteItemsAdapter(deleteItemsHelperArrayList);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
