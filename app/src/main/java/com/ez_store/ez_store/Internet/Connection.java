package com.ez_store.ez_store.Internet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class Connection extends AppCompatActivity {
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback callback;
    Context context;
    NetworkRequest request;
    public boolean isConnected = false;

    public Connection(Context _context){
        context = _context;
    }

    public void registerNetworkCallback(){
        connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        request = new  NetworkRequest.Builder().build();

        try {
            callback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    isConnected = true;
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    isConnected = false;
                }
            };

            // register
            connectivityManager.registerNetworkCallback(request,callback);
        }
        catch (Exception e){
            isConnected = false;
        }

    }

    public void unregisterNetworkCallback() {
        try {
            //unregister
            connectivityManager.unregisterNetworkCallback(callback);
        }
        catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
