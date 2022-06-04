package com.cof.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.cof.R;


public class CustomerDialog extends Dialog{

    private Context context;


    public CustomerDialog(@NonNull Context context) {
        super(context);
        this.context=context;
        this.setContentView(R.layout.dialog);
    }

    public CustomerDialog(@NonNull Context context, int themeResId, int layoutId) {
        super(context, themeResId);
        this.context = context;
        this.setContentView(layoutId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.logout_dialog);
    }
}
