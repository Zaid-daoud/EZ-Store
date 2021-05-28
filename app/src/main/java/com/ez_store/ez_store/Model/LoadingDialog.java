package com.ez_store.ez_store.Model;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.ez_store.ez_store.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog loadingDialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void start(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog,null));
        loadingDialog =builder.create();
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        loadingDialog.getWindow().setLayout(400,400);
    }

    public void dismiss(){
        loadingDialog.dismiss();
    }
}

