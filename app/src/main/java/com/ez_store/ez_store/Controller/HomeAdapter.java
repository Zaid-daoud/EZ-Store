package com.ez_store.ez_store.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.View.Add_Items;
import com.ez_store.ez_store.View.Damaged_Items;
import com.ez_store.ez_store.View.DeleteItems;
import com.ez_store.ez_store.View.OpenPayment;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.View.MyEmployeesActivity;

import com.ez_store.ez_store.View.SellActivity;
import com.ez_store.ez_store.View.openInventory;
import com.ez_store.ez_store.View.update_items;

import com.ez_store.ez_store.View.MyPermissions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Activity activity;
    private final ArrayList<Integer> image_id_List;
    private final String role;
    private static final String OWNER = "Owner";

    public HomeAdapter(ArrayList<Integer> image_id_List, Activity activity, String role) {
        this.activity = activity;
        this.image_id_List = image_id_List;
        this.role = role;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.home_item, parent, false);
        return new HomeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        int img = image_id_List.get(position);
        Picasso.get().load(img).into(holder.front);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = FirebaseAuth.getInstance().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Person");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        try {
                            if(role.equals(OWNER)){
                                if (img == R.drawable.payment) {
                                    click(img, false, snapshot.child(id));
                                } else if (snapshot.child(id).child("Payment").exists()) {
                                    click(img, false, snapshot.child(id));
                                } else {
                                    Toast.makeText(activity.getApplicationContext(), "You need to renew your subscription", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                if(snapshot.child(id).child("employed").getValue(boolean.class)) {
                                    String stakeholder = snapshot.child(id).child("stakeholder").getValue(String.class);
                                    if (snapshot.child(stakeholder).child("Payment").exists()) {
                                        click(img, false, snapshot.child(id));
                                    } else {
                                        Toast.makeText(activity.getApplicationContext(), "Your stakeholder need to renew his subscription", Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Toast.makeText(activity, "Sorry, you cannot benefit from the application services\n" +
                                            " because you are not employed yet", Toast.LENGTH_LONG).show();
                                }
                            }
                        }catch (Exception ex){
                            Toast.makeText(activity,ex.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return image_id_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView front;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            front = (ImageView) itemView.findViewById(R.id.front);
        }
    }

    private void click(int img, boolean hasPermission, DataSnapshot snapshot) {
        switch (img) {
            case R.drawable.manage_permissions:
                openManagePermission(snapshot.child("id").getValue(String.class));
                break;
            case R.drawable.view_inventory:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.id.view_items));
                if (role.equals(OWNER)) {
                    openInventory(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openInventory(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.add_items:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.id.add_new_items));
                if (role.equals(OWNER)) {
                    openAddAlertDialog(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openAddAlertDialog(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.delete_items:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.id.delete_items));
                if (role.equals(OWNER)) {
                    openDeleteAlertDialog(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openDeleteAlertDialog(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.damaged_items:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.id.add_damaged_items));
                if (role.equals(OWNER)) {
                    openAddDamagedAlertDialog(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openAddDamagedAlertDialog(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.sell_items:
                openSellAlertDialog(snapshot);
                break;
            case R.drawable.payment:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.drawable.payment));
                if (role.equals(OWNER)) {
                    openPayment(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openPayment(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.update_items:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.drawable.update_items));
                if (role.equals(OWNER)) {
                    openUpdateAlertDialog(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openUpdateAlertDialog(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
            case R.drawable.report:
                hasPermission = snapshot.child("Permissions").hasChild(String.valueOf(R.drawable.report));
                if (role.equals(OWNER)) {
                    openReport(snapshot.child("id").getValue(String.class));
                } else if (hasPermission) {
                    openReport(snapshot.child("stakeholder").getValue(String.class));
                } else {
                    Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void openReport(String id) {

    }

    private void openUpdateAlertDialog(String id) {
        Intent intent = new Intent(activity, update_items.class);
        intent.putExtra("id", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void openPayment(String id) {
        if (role.equals(OWNER)) {
            Intent intent = new Intent(activity, OpenPayment.class);
            intent.putExtra("id", id);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            Toast.makeText(activity,"This Property Only For Owners",Toast.LENGTH_LONG).show();
        }
    }

    private void openSellAlertDialog(DataSnapshot snap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sell_dialog, null);
        builder.setView(dialogView);
        ImageView cash = (ImageView) dialogView.findViewById(R.id.cash);
        ImageView dept = (ImageView) dialogView.findViewById(R.id.dept);
        AlertDialog alertDialog = builder.create();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
        alertDialog.show();
        reference.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                cash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        cash(snap, snapshot);
                    }
                });

                dept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        dept(snap, snapshot);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(activity.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });
    }

    private void dept(DataSnapshot snap, DataSnapshot snapshot) {
        String Cashier = snapshot.child("userName").getValue(String.class);
        boolean hasPermission = snap.child("Permissions").hasChild(String.valueOf(R.id.sell_in_dept));
        if (role.equals(OWNER)) {
            getCustomerName(Cashier, snap.child("id").getValue(String.class));
        } else if (hasPermission) {
            getCustomerName(Cashier, snap.child("stakeholder").getValue(String.class));
        } else {
            Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
        }
    }

    private void cash(DataSnapshot snap, DataSnapshot snapshot) {
        boolean hasPermission = snap.child("Permissions").hasChild(String.valueOf(R.id.sell_in_cash));
        if (role.equals(OWNER)) {
            Intent intent = new Intent(activity, SellActivity.class);
            intent.putExtra("id", snap.child("id").getValue(String.class));
            intent.putExtra("Cashier", snapshot.child("userName").getValue(String.class));
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else if (hasPermission) {
            Intent intent = new Intent(activity, SellActivity.class);
            intent.putExtra("id", snap.child("stakeholder").getValue(String.class));
            intent.putExtra("Cashier", snapshot.child("userName").getValue(String.class));
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            Toast.makeText(activity, "Sorry you don\'t have this permission", Toast.LENGTH_LONG).show();
        }
    }

    private void getCustomerName(String cashier, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.customer_name, null);
        builder.setView(dialogView);
        AppCompatButton done = (AppCompatButton) dialogView.findViewById(R.id.Done);
        EditText name = (EditText) dialogView.findViewById(R.id.customer_name);
        AlertDialog alertDialog = builder.create();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customer = name.getText().toString().trim();
                if (!customer.isEmpty()) {
                    Intent intent = new Intent(activity, SellActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("Customer", customer);
                    intent.putExtra("Cashier", cashier);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    Toast.makeText(activity.getApplicationContext(), "Enter Customer Name", Toast.LENGTH_LONG).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void openAddDamagedAlertDialog(String id) {
        Intent intent = new Intent(activity, Damaged_Items.class);
        intent.putExtra("id", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void openDeleteAlertDialog(String id) {
        Intent intent = new Intent(activity, DeleteItems.class);
        intent.putExtra("id", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void openAddAlertDialog(String id) {
        Intent intent = new Intent(activity, Add_Items.class);
        intent.putExtra("id", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void openInventory(String id) {
        Intent intent = new Intent(activity, openInventory.class);
        intent.putExtra("id", id);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void openManagePermission(String id) {
        Intent intent;
        if (role.equals(OWNER)) {
            intent = new Intent(activity, MyEmployeesActivity.class);
        } else{
            intent = new Intent(activity, MyPermissions.class);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}
