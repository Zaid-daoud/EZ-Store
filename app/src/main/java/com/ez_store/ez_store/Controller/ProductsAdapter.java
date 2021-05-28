package com.ez_store.ez_store.Controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Model.Person;
import com.ez_store.ez_store.Model.Product;
import com.ez_store.ez_store.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    private ArrayList<Product> products;
    public ProductsAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.inventory_items, parent, false);
        return new ProductsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.product_name.setText("Name: "+product.getName());
        holder.product_Description.setText("Description: "+product.getDescription());
        holder.product_Price.setText("Price: "+product.getPrice()+" JOD");
        holder.product_quantity.setText("Quantity: "+product.getQuantity());
        holder.product_Date.setText("Expired Date: "+(product.getExpiredDate().toString()));
        Picasso.get().load(product.getImgUri().replace(",",".")).into(holder.product_image);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView product_name,product_Description,product_Price,product_quantity , product_Date;
        ImageView product_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            product_name = (TextView) itemView.findViewById(R.id.product_name);
            product_Description = (TextView) itemView.findViewById(R.id.product_Description);
            product_Price = (TextView) itemView.findViewById(R.id.product_Price);
            product_quantity = (TextView) itemView.findViewById(R.id.product_quantity);
            product_image = (ImageView) itemView.findViewById(R.id.product_image);
            product_Date = (TextView) itemView.findViewById(R.id.product_Date);
        }
    }
}
