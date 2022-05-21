package com.cof.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.jinian.test1.R;

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


// 大图显示之后，点击图片消失
//        imgView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                this.dismiss();
//            }
//        });
    }

    //设置当前imgView的图片
//    private ImageView getView() {
//        ImageView imgView = new ImageView(context);
//        imgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        imgView.setImageBitmap(bitmap);
//        return imgView;
//    }
//
//    private Bitmap decodeBigPhoto() {
//        if (bitmap != null) {
//            Bitmap bigBitmap;
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            bigBitmap = BitmapFactory.decodeFile(bitmap);
//            opt.inSampleSize = 1;
//            opt.inJustDecodeBounds = false;
//
//            bigBitmap = BitmapFactory.decodeFile(bitmap, opt);
////iv_photo.setImageBitmap(bigBitmap);
//            return bigBitmap;
//// releaseBitmap();
//        }
//        return null;
//
//    }
}
