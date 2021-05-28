package com.ez_store.ez_store.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Controller.MyEmployeeAdapter;
import com.ez_store.ez_store.Model.Person;
import com.ez_store.ez_store.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class MyEmployeesActivity extends AppCompatActivity {

    private final static String PERSON = "Person";
    private DatabaseReference reference;private RecyclerView recycler;
    private MyEmployeeAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_employees);
        reference = FirebaseDatabase.getInstance().getReference(PERSON);
        showEmployee();
        
        EditText phone = (EditText) findViewById(R.id.phone_text);
        CountryCodePicker codePicker = (CountryCodePicker) findViewById(R.id.countryCode);
        codePicker.registerCarrierNumberEditText(phone);

        Button AddEmployee = (Button) findViewById(R.id.AddEmployee);
        AddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findEmployee(codePicker.getFullNumberWithPlus().replace(" ", "").replace("-", ""));
            }
        });
    }

    private void showEmployee() {
        recycler = (RecyclerView) findViewById(R.id.myEmployees);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<Person> options =
                new FirebaseRecyclerOptions.Builder<Person>()
                        .setQuery(reference.child(FirebaseAuth.getInstance().getUid()).child("Employee"), Person.class)
                        .build();
        adapter = new MyEmployeeAdapter(options, MyEmployeesActivity.this);
        recycler.setAdapter(adapter);
    }

    private void findEmployee(String phone) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExist = false;
                DataSnapshot employee = null;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.child("phone").getValue(String.class).equals(phone)) {
                        if(snap.child("employed").getValue(boolean.class)) {
                            Toast.makeText(MyEmployeesActivity.this,"This Employee Already Has A Job",Toast.LENGTH_LONG).show();
                            break;
                        }
                        else {
                            isExist = true;
                            employee = snap;
                            String empId = snap.child("id").getValue(String.class);
                            reference.child(empId).child("employed").setValue(true);
                            reference.child(empId).child("stakeholder").setValue(FirebaseAuth.getInstance().getUid());
                        }
                    }
                }
                if(isExist){
                    if(employee.child("role").getValue(String.class).equals("Owner")){
                        Toast.makeText(MyEmployeesActivity.this,"You Can't Add Owner As Employee",Toast.LENGTH_LONG).show();
                    }
                    else {
                        addEmployee(employee);
                    }
                }
                else {
                    Toast.makeText(MyEmployeesActivity.this,"This User Doesn't Exist",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyEmployeesActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addEmployee(DataSnapshot employee_id) {
        try {
            Person emp = new Person();
            emp.setID(employee_id.child("id").getValue(String.class));
            emp.setUserName(employee_id.child("userName").getValue(String.class));
            emp.setPhone(employee_id.child("phone").getValue(String.class));
            emp.setEmployed(true);

            DatabaseReference reference = FirebaseDatabase.getInstance()
                    .getReference(PERSON).child(FirebaseAuth.getInstance().getUid()).child("Employee");
            reference.child(emp.getID()).setValue(emp);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}