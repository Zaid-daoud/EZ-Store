package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ez_store.ez_store.Controller.SellAdapter;
import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.databinding.ActivitySellBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

public class SellActivity extends AppCompatActivity {

    public static ActivitySellBinding binding;
    private DatabaseReference reference;
    private static final String PERSON = "Person";
    private SellAdapter adapter;
    public static int quantity = 0;
    public static double total = 0;
    public static ArrayList<Product> products;
    private EditText bc;
    private CharSequence s, t;
    private String customer,cashier,id;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        connection = new Connection(this);
        id = getIntent().getStringExtra("id");
        customer = getIntent().getStringExtra("Customer");
        cashier = getIntent().getStringExtra("Cashier");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sell);
        binding.cashier.setText("Cashier : "+cashier);
        if(customer != null) {
            binding.customer.setVisibility(View.VISIBLE);
            binding.customer.setText("Customer : " + customer);
        }
        reference = FirebaseDatabase.getInstance().getReference(PERSON);
        products = new ArrayList<>();
        Date d = new Date();
        s = DateFormat.format("dd/MM/yyyy", d.getTime());
        t = DateFormat.format("hh:mm:ss", d.getTime());
        binding.time.setText("Time : " + t);
        binding.date.setText("Date : " + s);

        binding.addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readBarcode();
            }
        });
    }

    private void readBarcode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SellActivity.this);
        LayoutInflater inflater = SellActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.read_barcode_dialog, null);
        builder.setView(dialogView);
        AppCompatButton camera = (AppCompatButton) dialogView.findViewById(R.id.camera);
        AppCompatButton add = (AppCompatButton) dialogView.findViewById(R.id.Add);
        bc = (EditText) dialogView.findViewById(R.id.barcode);
        AlertDialog alertDialog = builder.create();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.isConnected) {
                    IntentIntegrator INT = new IntentIntegrator(SellActivity.this);
                    INT.setPrompt("Press volume up for flash");
                    INT.setBeepEnabled(true);
                    INT.setOrientationLocked(true);
                    INT.setCaptureActivity(CaptureActivity.class);
                    INT.initiateScan();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(SellActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected) {
                    String barcode = bc.getText().toString().trim();
                    alertDialog.dismiss();
                    check(barcode);
                }else {
                    Toast.makeText(SellActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }

    private void check(String barcode) {
        reference.child(id).child("Products").child(barcode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Product product = new Product();
                            product.setBarCode(barcode);
                            product.setExpiredDate(snapshot.child("expiredDate").getValue(String.class));
                            product.setDescription(snapshot.child("description").getValue(String.class));
                            product.setImgUri(snapshot.child("imgUri").getValue(String.class));
                            product.setQuantity(snapshot.child("quantity").getValue(int.class));
                            product.setName(snapshot.child("name").getValue(String.class));
                            product.setPrice(snapshot.child("price").getValue(Double.class));

                            if(products.isEmpty()){
                                getQuantity(product);
                            }else {
                                boolean exist = false;
                                for (Product p : products) {
                                    if (p.getBarCode().equals(barcode)) {
                                        exist = true;
                                    }
                                }
                                if(!exist){
                                    getQuantity(product);
                                }else {
                                    Toast.makeText(SellActivity.this, "This item already exist", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "This item doesn\'t exist in your inventory", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getQuantity(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SellActivity.this);
        LayoutInflater inflater = SellActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_item_to_bill, null);
        builder.setView(dialogView);
        AppCompatButton done = (AppCompatButton) dialogView.findViewById(R.id.Done);
        EditText q = (EditText) dialogView.findViewById(R.id.quantity);
        AlertDialog alertDialog = builder.create();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.isConnected) {
                    if (!q.getText().toString().trim().isEmpty()) {
                        if (Integer.parseInt(q.getText().toString().trim()) > 0) {
                            if (product.getQuantity() > 0) {
                                if (Integer.parseInt(q.getText().toString().trim()) <= product.getQuantity()) {
                                    alertDialog.dismiss();
                                    product.setQuantity(Integer.parseInt(q.getText().toString().trim()));
                                    products.add(product);
                                    quantity += product.getQuantity();
                                    total += product.getPrice() * product.getQuantity();
                                    binding.totalPrice.setText("Total : " + total);
                                    binding.totalQuantity.setText("Quantity : " + quantity);
                                    showItem();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Sorry .. You have only (" + quantity + ") items in the inventory\n" +
                                            "You Should Add More Items Before Selling", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Out Of Stock", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Quantity must be more than (0) ", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(SellActivity.this, "Enter the required quantity", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(SellActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.show();
    }

    private void showItem() {
        binding.items.setLayoutManager(new LinearLayoutManager(SellActivity.this));
        adapter = new SellAdapter(products,SellActivity.this,id);
        binding.items.setAdapter(adapter);

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected) {
                    products.clear();
                    quantity = 0;
                    total = 0;
                    startActivity(new Intent(SellActivity.this, HomeActivity.class));
                    SellActivity.this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    finish();
                }else {
                    Toast.makeText(SellActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connection.isConnected) {
                    if (customer != null) {
                        sell("Dept");
                    } else {
                        sell("Cash");
                    }
                }else {
                    Toast.makeText(SellActivity.this, R.string.fui_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sell(String payment) {
        String _id = reference.push().getKey();
        for (Product product : products) {
            reference.child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PERSON).child(id);
                    ref.child(payment).child(_id).child("total").setValue(total);
                    ref.child(payment).child(_id).child(product.getBarCode()).child("barCode").setValue(product.getBarCode());
                    ref.child(payment).child(_id).child(product.getBarCode()).child("quantity").setValue(product.getQuantity());
                    ref.child(payment).child(_id).child("Cashier").setValue(cashier);
                    ref.child(payment).child(_id).child("Date").setValue(s.toString());
                    ref.child(payment).child(_id).child("Time").setValue(t.toString());
                    if(customer!=null) {
                        ref.child(payment).child(_id).child("Customer").setValue(customer);
                    }
                    int Q = snapshot.child("Products").child(product.getBarCode()).child("quantity").getValue(int.class);
                    ref.child("Products").child(product.getBarCode()).child("quantity").setValue(Q - product.getQuantity());
                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                    total = 0;
                    quantity = 0;
                    products.clear();
                    binding.totalPrice.setText("Total : " + total);
                    binding.totalQuantity.setText("Quantity : " + quantity);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult IR = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (IR.getContents() != null) {
            String barcode = IR.getContents();
            bc.setText(barcode);
            check(barcode);
        } else {
            Toast.makeText(getApplicationContext(), "Make sure your bar code is aligned", Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}