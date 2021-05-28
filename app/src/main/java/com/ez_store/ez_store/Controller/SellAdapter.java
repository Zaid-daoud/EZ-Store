package com.ez_store.ez_store.Controller;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.View.SellActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class SellAdapter extends RecyclerView.Adapter<SellAdapter.ViewHolder>{

    private ArrayList<Product> products;
    private Activity activity;
    private String id;
    public SellAdapter(ArrayList<Product> products, Activity activity ,String id) {
        this.products = products;
        this.activity = activity;
        this.id = id;
    }

    @NonNull
    @NotNull
    @Override
    public SellAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.sell_items, parent, false);
        return new SellAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SellAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.name.setText(product.getName());
        holder.quantity.setText(String.valueOf(product.getQuantity()));
        holder.item_price.setText(String.valueOf(product.getPrice()));
        holder.total_price.setText(String.valueOf(product.getPrice() * product.getQuantity()));
        Picasso.get().load(product.getImgUri().replace(",",".")).into(holder.image);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                products.remove(product);
                SellActivity.products.remove(product);
                SellActivity.quantity -= product.getQuantity();
                SellActivity.total -= product.getQuantity() * product.getPrice();
                SellActivity.binding.totalPrice.setText("Total : "+SellActivity.total);
                SellActivity.binding.totalQuantity.setText("Quantity : "+SellActivity.quantity);
                notifyDataSetChanged();
            }
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(product.getQuantity() > 1) {
                    product.setQuantity(product.getQuantity() - 1);
                    holder.quantity.setText(String.valueOf(product.getQuantity()));
                    holder.item_price.setText(String.valueOf(product.getPrice()));
                    holder.total_price.setText(String.valueOf(product.getPrice() * product.getQuantity()));
                    SellActivity.quantity -= 1;
                    SellActivity.total -= product.getPrice();
                    SellActivity.products.get(position).setQuantity(product.getQuantity());
                    SellActivity.binding.totalPrice.setText("Total : "+SellActivity.total);
                    SellActivity.binding.totalQuantity.setText("Quantity : "+SellActivity.quantity);
                }
            }
        });

        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
                reference.child(id).child("Products").child(product.getBarCode())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                int quantity = snapshot.child("quantity").getValue(int.class);
                                if(quantity > product.getQuantity()){
                                    product.setQuantity(product.getQuantity()+1);
                                    holder.quantity.setText(String.valueOf(product.getQuantity()));
                                    holder.item_price.setText(String.valueOf(product.getPrice()));
                                    holder.total_price.setText(String.valueOf(product.getPrice() * product.getQuantity()));
                                    SellActivity.quantity += 1;
                                    SellActivity.total += product.getPrice();
                                    SellActivity.products.get(position).setQuantity(product.getQuantity());
                                    SellActivity.binding.totalPrice.setText("Total : "+SellActivity.total);
                                    SellActivity.binding.totalQuantity.setText("Quantity : "+SellActivity.quantity);
                                }else {
                                    Toast.makeText(activity, "Sorry .. You have only (" + quantity + ") items in the inventory\n" +
                                            "You Should Add More Items Before Selling", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                Toast.makeText(activity,error.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image,delete,minus,plus;
        TextView name,quantity,item_price,total_price;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            delete = itemView.findViewById(R.id.delete);
            minus = itemView.findViewById(R.id.minus);
            plus = itemView.findViewById(R.id.plus);
            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            item_price = itemView.findViewById(R.id.item_price);
            total_price = itemView.findViewById(R.id.total_price);
        }
    }
}
