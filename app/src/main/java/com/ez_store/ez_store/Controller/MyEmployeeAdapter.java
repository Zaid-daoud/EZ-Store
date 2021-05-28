package com.ez_store.ez_store.Controller;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Model.Person;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.View.ManagePermissionsActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyEmployeeAdapter extends FirebaseRecyclerAdapter<Person,MyEmployeeAdapter.ViewHolder> {


    private final Activity activity;
    private static final String PERMISSIONS = "Permissions";
    public MyEmployeeAdapter(@NonNull FirebaseRecyclerOptions<Person> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyEmployeeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.employee_item, parent, false);
        return new MyEmployeeAdapter.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Person model) {
        holder.name.setText(model.getUserName());
        holder.phone.setText(model.getPhone());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ManagePermissionsActivity.class);
                intent.putExtra("Person_id",model.getID());
                intent.putExtra("Person_name",model.getUserName());
                activity.startActivity(intent);
                activity. overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        holder.removePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePerson(model);
            }
        });
    }

    private void removePerson(Person person) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");

            reference.child(auth.getCurrentUser().getUid()).child("Employee").child(person.getID()).removeValue();
            reference.child(person.getID()).child(PERMISSIONS).removeValue();
            reference.child(person.getID()).child("employed").setValue(false);

            notifyDataSetChanged();
        }catch (Exception ex){
            Toast.makeText(activity,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,phone;
        ImageView removePerson,edit;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.emp_name);
            phone = (TextView) itemView.findViewById(R.id.emp_phone);
            removePerson = (ImageView) itemView.findViewById(R.id.removeEmployee);
            edit = (ImageView) itemView.findViewById(R.id.employeePermissions);
        }
    }
}
