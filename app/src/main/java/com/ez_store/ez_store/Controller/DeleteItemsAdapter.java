package com.ez_store.ez_store.Controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Model.DeleteItemsHelper;
import com.ez_store.ez_store.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DeleteItemsAdapter extends RecyclerView.Adapter<DeleteItemsAdapter.ExampleViewHolder> {

    private ArrayList<DeleteItemsHelper> deleteItemsList;
    private Context context;
    private OnItemClickListenr mListener;
    private String task;

    public interface OnItemClickListenr {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListenr listener) {
        mListener = listener;
    }

    public DeleteItemsAdapter(ArrayList<DeleteItemsHelper> deleteItemsHelperArrayList) {
        deleteItemsList = deleteItemsHelperArrayList;
    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage, delete;
        public TextView itemName, itemQuantity;

        public ExampleViewHolder(@NonNull View itemView, final OnItemClickListenr listner) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_img);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            delete = itemView.findViewById(R.id.img_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listner != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            listner.onDeleteClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        DeleteItemsAdapter.ExampleViewHolder evh = new DeleteItemsAdapter.ExampleViewHolder(v, mListener);
        context = parent.getContext();
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        DeleteItemsHelper current = deleteItemsList.get(position);


        Picasso.get().load(current.getItemImage().replace(",", ".")).into(holder.itemImage);
        holder.itemName.setText(current.getItemName());
        holder.itemQuantity.setText(current.getItemQuantity());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText taskEditText = new EditText(context);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Items")
                        .setMessage("Enter the quantity that you want to delete!")
                        .setView(taskEditText)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                task = String.valueOf(taskEditText.getText());
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                String id = mAuth.getUid();
                                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Person").child(id).child("Products").child(current.getId());
                                DatabaseReference rRef = FirebaseDatabase.getInstance().getReference("Person").child(id).child("Products").child(current.getId());
                                mRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int x = Integer.parseInt(snapshot.child("quantity").getValue().toString());
                                        int y = x - Integer.parseInt(task);
                                        task = "0";
                                        if (y >= 0) {
                                            rRef.child("quantity").setValue(y);
                                        } else {
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return deleteItemsList.size();
    }


}
