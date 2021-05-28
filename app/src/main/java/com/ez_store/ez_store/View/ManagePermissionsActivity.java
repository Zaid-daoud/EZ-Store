package com.ez_store.ez_store.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ez_store.ez_store.Model.Permission;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManagePermissionsActivity extends AppCompatActivity {

    private String employee_name, employee_id;
    private static final String PERSON = "Person";
    private static final String PERMISSIONS = "Permissions";
    private Switch add_new_items,update_items,add_damaged_items,delete_items,view_items,sell_inDept,sell_inCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_permissions);
        employee_id = getIntent().getStringExtra("Person_id");
        employee_name = getIntent().getStringExtra("Person_name");
        TextView text_name = (TextView) findViewById(R.id.emp_name);
        text_name.setText(employee_name);

        add_new_items = (Switch) findViewById(R.id.add_new_items);
        update_items = (Switch) findViewById(R.id.update_items);
        add_damaged_items = (Switch) findViewById(R.id.add_damaged_items);
        delete_items = (Switch) findViewById(R.id.delete_items);
        view_items = (Switch) findViewById(R.id.view_items);
        sell_inDept = (Switch) findViewById(R.id.sell_in_dept);
        sell_inCash = (Switch) findViewById(R.id.sell_in_cash);

        getEmployeePermissions(employee_id);

        add_new_items.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.add_new_items,"Add New Items Permission");
                } else {
                    removePermission(R.id.add_new_items);
                }
            }
        });
        update_items.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.update_items, "Update Items Permission");
                } else {
                    removePermission(R.id.update_items);
                }
            }
        });
        add_damaged_items.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.add_damaged_items, "Add Damaged Items Permission");
                } else {
                    removePermission(R.id.add_damaged_items);
                }
            }
        });
        delete_items.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.delete_items, "Delete Items Permission");
                } else {
                    removePermission(R.id.delete_items);
                }
            }
        });
        view_items.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.view_items, "View Items Permission");
                } else {
                    removePermission(R.id.view_items);
                }
            }
        });
        sell_inCash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.sell_in_cash, "Sell In Cash Permission");
                } else {
                    removePermission(R.id.sell_in_cash);
                }
            }
        });
        sell_inDept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addPermission(R.id.sell_in_dept, "Sell In Dept Permission");
                } else {
                    removePermission(R.id.sell_in_dept);
                }
            }
        });
    }

    private void removePermission(int permission_id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
        reference.child(employee_id).child(PERMISSIONS).child(String.valueOf(permission_id)).removeValue();
    }

    private void addPermission(int permission_id, String permission_name) {
        Permission permission = new Permission(String.valueOf(permission_id),permission_name);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
        reference.child(employee_id).child(PERMISSIONS).child(String.valueOf(permission_id)).setValue(permission);
        reference.child(employee_id).child("stakeholder").setValue(FirebaseAuth.getInstance().getUid());
    }

    private void getEmployeePermissions(String employee_id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
        reference.child(employee_id).child(PERMISSIONS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    String id = snap.child("id").getValue(String.class);
                    switch (Integer.parseInt(id)){
                        case R.id.add_new_items: add_new_items.setChecked(true);break;
                        case R.id.add_damaged_items:add_damaged_items.setChecked(true);break;
                        case R.id.delete_items:delete_items.setChecked(true);break;
                        case R.id.view_items:view_items.setChecked(true);break;
                        case R.id.sell_in_cash:sell_inCash.setChecked(true);break;
                        case R.id.sell_in_dept:sell_inDept.setChecked(true);break;
                        case R.id.update_items:update_items.setChecked(true);break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagePermissionsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}