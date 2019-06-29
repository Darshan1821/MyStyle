package com.dexter.mystyle.activity;

import android.app.Activity;
import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public ProgressDialog getProgressDialog(String title, String message, Boolean cancelable, Boolean setTouchOutsideCancelable, Activity activity){
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(setTouchOutsideCancelable);
        return progressDialog;
    }
}
