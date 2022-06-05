package com.cof.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.cof.R;


public class ShowBigPhoto extends Dialog {
    private Context context;
//    private Bitmap bitmap;


    public ShowBigPhoto(Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.context = context;
        this.setContentView(R.layout.showbigphoto);
//        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showDetailPhoto() {
// 全屏显示的方法
//        ImageView imgView = getView();
        this.setContentView(R.layout.showbigphoto);
        this.show();

    }
}
