package com.ez_store.ez_store.View;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ez_store.ez_store.Model.PaymentHelperClass;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OpenPayment extends AppCompatActivity {
    EditText payer_name, payer_account, payer_amount, payer_purpose, payer_mobile;
    TextView Remaining_amount;
    String _name, _account, _amount, _purpose, _mobile;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_payment);
        payer_name = findViewById(R.id.payer_name);
        payer_account = findViewById(R.id.bank_account);
        payer_amount = findViewById(R.id.amount);
        payer_purpose = findViewById(R.id.purpose);
        payer_mobile = findViewById(R.id.mobile_no);

        firebaseDatabase = FirebaseDatabase.getInstance("https://ez-store-9f8d8-default-rtdb.firebaseio.com/");
        currentuser = getIntent().getStringExtra("id");
    }

    public void MakePayment(View view) {
        _name = payer_name.getText().toString();
        _account = payer_account.getText().toString();
        _amount = payer_amount.getText().toString();
        _purpose = payer_purpose.getText().toString();
        _mobile = payer_mobile.getText().toString();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);


        PaymentHelperClass payment = new PaymentHelperClass(_name, _account, _amount, _purpose, _mobile, formattedDate);
        databaseReference = firebaseDatabase.getReference("Person").child(currentuser).child("Payment");
        databaseReference.child(_account).setValue(payment);


        Toast.makeText(this, "PAYMENT ADDED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
    }
}