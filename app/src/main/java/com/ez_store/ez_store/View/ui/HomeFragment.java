package com.ez_store.ez_store.View.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ez_store.ez_store.Controller.HomeAdapter;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.View.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView dashboard;
    private HomeAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
        reference.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                dashboard = (RecyclerView) root.findViewById(R.id.dashboard);
                dashboard.setLayoutManager(new GridLayoutManager(getContext(),2));

                ArrayList<Integer> img_id = new ArrayList<>();
                img_id.add(R.drawable.manage_permissions);
                img_id.add(R.drawable.view_inventory);
                img_id.add(R.drawable.add_items);
                img_id.add(R.drawable.delete_items);
                img_id.add(R.drawable.update_items);
                img_id.add(R.drawable.sell_items);
                img_id.add(R.drawable.payment);
                img_id.add(R.drawable.damaged_items);
                img_id.add(R.drawable.report);

                adapter = new HomeAdapter(img_id,getActivity(), snapshot.child("role").getValue(String.class));
                dashboard.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }
}