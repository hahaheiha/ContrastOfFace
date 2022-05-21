package com.cof;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cof.utils.BitmapUtil;
import com.cof.utils.CustomerDialog;
import com.cof.utils.DatabaseHelper;
import com.cof.utils.ImageDataUtil;
import com.cof.utils.ShowBigPhoto;
import com.cof.utils.baidu.AuthService;
import com.cof.utils.baidu.Base64Util;
import com.cof.utils.baidu.GsonUtils;
import com.cof.utils.baidu.HttpUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import entity.Result;
import entity.ResultMsg;

public class MainActivity extends AppCompatActivity {

    public static final int CHOOSE_PHOTO = 2;
    private ImageView chosenImageRight;
    private ImageView chosenImageLeft;
//    private ImageView resultImage;
    private TextView loading;

    private byte[] templateImgBytes = null;
    private byte[] rightImgBytes = null;


    private boolean isVisible;
    private boolean isShowAgain = true;

    private Button rightAddButton;
    private Button leftAddButton;
    private Button startButton;
    private FloatingActionButton addImage;

    private Bitmap chosenImageRightBitmap;
    private HashMap<String, String> stuInfo = new HashMap<>();

    private LinkedHashMap<Integer, String> dbImages;

    private Integer maxLikeImgId = 0;
    private Integer maxLikeDegree = -1;
    private String maxLikeImgBase64 = null;


    Iterator<Map.Entry<Integer, String>> iteratorDBImg;

    private MaterialCardView sInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
        }else{
            Dialog dialog = new CustomerDialog(this, R.style.Dialog, R.layout.dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
            TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
            TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
            TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);

            dialogTextLeft.setVisibility(View.INVISIBLE);
            dialogTextRight.setText("我知道了");
            dialogTextRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogTitle.setText("联网失败：");
            dialogInfo.setText("网络连接失败，请检查网络连接是否正常。");
        }



        sInfo = (MaterialCardView) findViewById(R.id.sInfo);

        chosenImageRight = (ImageView) findViewById(R.id.chosenImageRight);
        chosenImageLeft = (ImageView) findViewById(R.id.chosenImageLeft);
//        resultImage = (ImageView) findViewById(R.id.resultImage);
        loading = (TextView) findViewById(R.id.loading);

        addImage =  (FloatingActionButton) findViewById(R.id.floatingButtonCenter);
        rightAddButton = (Button) findViewById(R.id.addRight);
        leftAddButton = (Button) findViewById(R.id.addLeft);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setVisibility(View.INVISIBLE);
        rightAddButton.setVisibility(View.INVISIBLE);
        leftAddButton.setVisibility(View.INVISIBLE);

        SharedPreferences pref = getSharedPreferences("isTip", MODE_PRIVATE);
        isShowAgain = pref.getBoolean("isShowAgain", true);


        if (isShowAgain) {
            Dialog dialog = new CustomerDialog(this, R.style.Dialog, R.layout.dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
            TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
            TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
            TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);

            dialogTextLeft.setText("不再提示");
            dialogTextLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = getSharedPreferences("isTip", MODE_PRIVATE).edit();
                    editor.putBoolean("isShowAgain", false);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialogTextRight.setText("我知道了");
            dialogTextRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogTitle.setText("用户须知：");
            dialogInfo.setText("请确保上传的图片存在人脸。\n上传的模板图和人脸图的文件大小不超过 2 MB。\n" +
                    "上传的图片应为 JPG/JPEG 格式。\n暂不支持黑白照片。\n--开始使用吧--");

        }

        sInfo.setVisibility(View.INVISIBLE);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    rightAddButton.setVisibility(View.VISIBLE);
                    leftAddButton.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.VISIBLE);
                    addImage.setImageResource(R.drawable.ic_cancel);
                    isVisible = true;

                }
                else {
                    rightAddButton.setVisibility(View.INVISIBLE);
                    leftAddButton.setVisibility(View.INVISIBLE);
                    startButton.setVisibility(View.INVISIBLE);
                    addImage.setImageResource(R.drawable.ic_and);
                    isVisible = false;
                }
            }
        });

        rightAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                }
                else {

                    openAlbum();
                }
            }
        });

        leftAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpressionActivity.class);
                startActivity(intent);
            }
        });


        chosenImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpressionActivity.class);
                startActivity(intent);
            }
        });

        chosenImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosenImageRightBitmap == null) return;

                ShowBigPhoto showBigPhoto = new ShowBigPhoto(MainActivity.this);
                showBigPhoto.show();
                ImageView detailPhoto = (ImageView) showBigPhoto.findViewById(R.id.detailPhoto);
                detailPhoto.setImageBitmap(chosenImageRightBitmap);
                detailPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBigPhoto.dismiss();
                    }
                });

                Button detailCancelButton = (Button) showBigPhoto.findViewById(R.id.detailCancelButton);
                detailCancelButton.setVisibility(View.GONE);

                Button detailSureButton = (Button) showBigPhoto.findViewById(R.id.detailSureButton);
                detailSureButton.setVisibility(View.GONE);
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sInfo.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
                faceMatch();
            }
        });

    }


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        if (intent.getIntExtra(ExpressionActivity.EXP_IMAGE_ID, -1) != -1) {
////            setSelectImg(intent);
//            setSelectImg();
//        }
//    }
//
//
//    private void setSelectImg() {
//        dbImages = ImageDataUtil.getDatabaseImage(this);
//        iteratorDBImg = dbImages.entrySet().iterator();
//    }

    private void openAlbum() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }
                else {
                    Toast.makeText(this, "您拒绝了相册使用权限，请开启后再试。", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }


    public void showDialogResult() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select sno, sname, sgrade, sroom, sphone, steacher, stphone from imagedb where imageid = ?", new String[]{maxLikeImgId.toString()});

        if (cursor.moveToFirst()) {

            String sno = cursor.getString(cursor.getColumnIndex("sno"));
            String sname = cursor.getString(cursor.getColumnIndex("sname"));
            String sgrade = cursor.getString(cursor.getColumnIndex("sgrade"));
            String sroom = cursor.getString(cursor.getColumnIndex("sroom"));
            String sphone = cursor.getString(cursor.getColumnIndex("sphone"));
            String steacher = cursor.getString(cursor.getColumnIndex("steacher"));
            String stphone = cursor.getString(cursor.getColumnIndex("stphone"));

            stuInfo.put("sno", sno);
            stuInfo.put("sname", sname);
            stuInfo.put("sgrade", sgrade);
            stuInfo.put("sroom", sroom);
            stuInfo.put("sphone", sphone);
            stuInfo.put("steacher", steacher);
            stuInfo.put("stphone", stphone);

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                StringBuilder builder = new StringBuilder();
//                builder.append(maxLikeImgId).append("\n").append(maxLikeDegree);

                Bitmap target = BitmapUtil.stringtoBitmap(dbImages.get(maxLikeImgId));
                chosenImageLeft.setImageBitmap(target);

                loading.setVisibility(View.INVISIBLE);
                sInfo.setVisibility(View.VISIBLE);

                TextView likeDegree = (TextView) findViewById(R.id.likeDegree);
                TextView likeEva = (TextView) findViewById(R.id.likeEva);
                TextView sno = (TextView) findViewById(R.id.sno_dis);
                TextView sname = (TextView) findViewById(R.id.sname_dis);
                TextView sgrade = (TextView) findViewById(R.id.sgrade_dis);
                TextView sroom = (TextView) findViewById(R.id.sroom_dis);
                TextView sphone = (TextView) findViewById(R.id.sphone_dis);
                TextView steacher = (TextView) findViewById(R.id.steacher_dis);
                TextView stphone = (TextView) findViewById(R.id.stphone_dis);

                likeDegree.setText(maxLikeDegree + "%");

                //TODO 根据相似程度分级
                likeEva.setText("同一个人的可能性极高");

                sno.setText(stuInfo.get("sno"));
                sname.setText(stuInfo.get("sname"));
                sgrade.setText(stuInfo.get("sgrade"));
                sroom.setText(stuInfo.get("sroom"));
                sphone.setText(stuInfo.get("sphone"));
                steacher.setText(stuInfo.get("steacher"));
                stphone.setText(stuInfo.get("stphone"));



            }
        });

    }

    public String faceMatch() {

        dbImages = ImageDataUtil.getDatabaseImage(this);
        iteratorDBImg = dbImages.entrySet().iterator();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                try {

                    String accessToken = AuthService.getAuth();

                    while (iteratorDBImg.hasNext()) {

                        Map.Entry<Integer, String> next = iteratorDBImg.next();
//            byte[] bytes = FileUtil.readFileByBytes("D:\\JavaProjects\\ContrastTestBaidu\\src\\main\\resources\\1.png");
                        String encode1 = next.getValue();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading.setText("正在对比中......");
                                chosenImageLeft.setImageBitmap(BitmapUtil.stringtoBitmap(next.getValue()));
                            }
                        });


                        String encode2 = Base64Util.encode(rightImgBytes);


                        ArrayList<String> params = new ArrayList<>();

                        HashMap<String, String> map = new HashMap<>();

                        map.put("image", encode1);
                        map.put("image_type", "BASE64");
                        map.put("face_type", "LIVE");

                        params.add(GsonUtils.toJson(map));

                        map.clear();

                        map.put("image", encode2);
                        map.put("image_type", "BASE64");
                        map.put("face_type", "LIVE");

                        params.add(GsonUtils.toJson(map));
//


                        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
//            String accessToken = "[调用鉴权接口获取的token]";

                        String result = HttpUtil.post(url, accessToken, "application/json", params.toString());
//                    showContrastResult(result);

                        ResultMsg resultMsg = GsonUtils.fromJson(result, ResultMsg.class);

                        if (resultMsg.getError_code() == 0) {
                            Result conResult = resultMsg.getResult();
                            if (conResult.getScore().intValue() > maxLikeDegree) {
                                maxLikeImgId = next.getKey();
                                maxLikeDegree = conResult.getScore().intValue();
                            }
                        }


                    }

                    showDialogResult();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 请求url


        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    chosenImageRightBitmap = getBitmapFromUri(uri);

                    ShowBigPhoto showBigPhoto = new ShowBigPhoto(MainActivity.this);
                    showBigPhoto.show();
                    ImageView detailPhoto = (ImageView) showBigPhoto.findViewById(R.id.detailPhoto);
                    detailPhoto.setImageBitmap(chosenImageRightBitmap);


                    Button detailCancelButton = (Button) showBigPhoto.findViewById(R.id.detailCancelButton);
                    Button detailSureButton = (Button) showBigPhoto.findViewById(R.id.detailSureButton);


                    detailCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showBigPhoto.dismiss();
                        }
                    });

                    detailSureButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loading.setText("");
                            chosenImageRight.setImageBitmap(chosenImageRightBitmap);
                            rightImgBytes = BitmapUtil.toByteArray(chosenImageRightBitmap);
                            showBigPhoto.dismiss();
                        }
                    });
                }
                break;
        }
    }



    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            ParcelFileDescriptor r = getContentResolver().openFileDescriptor(uri, "r");
            bitmap = BitmapFactory.decodeFileDescriptor(r.getFileDescriptor());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private void errorInfo(String paramString) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String info = "未知错误";
                if (paramString.contains("NO_FACE_FOUND")) {
                    info = "存在图片未检测到人脸。";
                }
                else if (paramString.contains("IMAGE_FILE_TOO_LARGE")) {
                    info = "存在上传的图像文件太大。\n要求图片文件大小不超过 2 MB";
                }
                else if (paramString.contains("IMAGE_ERROR_UNSUPPORTED_FORMAT")) {
                    info = "存在图像无法正确解析，有可能不是一个图像文件、或有数据破损。\n导致原因可能为：图片格式不为 JPEG/JPG 格式";
                }
                else if (paramString.contains("INVALID_IMAGE_SIZE")) {
                    info = "存在上传的图像像素尺寸太大或太小。\n最小200*200像素，最大4096*4096像素.";
                }
                else if (paramString.contains("BAD_FACE")) {
                    info = "上传的图片人脸不符合要求。\n可能情况: 只有半张脸。";
                }
                else if (paramString.contains("INVALID_RECTANGLE")) {
                    info = "存在传入的人脸框格式不符合要求，或者人脸框位于图片外。";
                }
                else if (paramString.contains("IMAGE_DOWNLOAD_TIMEOUT")) {
                    info = "下载图片超时。";
                }

                Dialog dialog = new CustomerDialog(MainActivity.this, R.style.Dialog, R.layout.dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
                TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
                TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
                TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);
                dialogTextRight.setText("我知道了");
                dialogTextRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialogTextLeft.setVisibility(View.INVISIBLE);

                dialogTitle.setText("错误信息");
                dialogInfo.setText(info);
            }
        });

    }
}
