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
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cof.entity.Result;
import com.cof.entity.ResultMsg;

public class MainActivity extends AppCompatActivity {

    public static final int CHOOSE_PHOTO = 2;
    private ImageView chosenImageRight;
    private ImageView chosenImageLeft;
    private ImageView bottomImage;

    private ProgressBar progressBar;
    private TextView loading;

    private byte[] rightImgBytes = null;


    private boolean isVisible;
    private boolean isShowAgain = true;

    private Button rightAddButton;
    private Button leftAddButton;
    private Button startButton;
    private FloatingActionButton addImage;

    private Bitmap chosenImageRightBitmap = null;
    private HashMap<String, String> stuInfo = new HashMap<>();

    private LinkedHashMap<Integer, String> dbImages;

    private Integer maxLikeImgId = 0;
    private Integer maxLikeDegree = -1;

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

            dialogTextLeft.setVisibility(View.GONE);
            dialogTextRight.setText("我知道了");
            dialogTextRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogTitle.setText("联网失败");
            dialogInfo.setText("网络连接失败，请检查网络连接是否正常。");
        }



        sInfo = (MaterialCardView) findViewById(R.id.sInfo);

        chosenImageRight = (ImageView) findViewById(R.id.chosenImageRight);
        chosenImageLeft = (ImageView) findViewById(R.id.chosenImageLeft);
        bottomImage = (ImageView) findViewById(R.id.bottomDefault);
//        resultImage = (ImageView) findViewById(R.id.resultImage);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        loading = (TextView) findViewById(R.id.loading);



        addImage =  (FloatingActionButton) findViewById(R.id.floatingButtonCenter);
        rightAddButton = (Button) findViewById(R.id.addRight);
        leftAddButton = (Button) findViewById(R.id.addLeft);
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setVisibility(View.GONE);
        rightAddButton.setVisibility(View.GONE);
        leftAddButton.setVisibility(View.GONE);

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
            dialogTitle.setText("使用须知：");
            dialogInfo.setText("请确保上传的图片存在人脸。\n上传的图片尺寸在1920x1080以下。");

        }

        sInfo.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

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
                    rightAddButton.setVisibility(View.GONE);
                    leftAddButton.setVisibility(View.GONE);
                    startButton.setVisibility(View.GONE);
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
                rightAddButton.setVisibility(View.GONE);
                leftAddButton.setVisibility(View.GONE);
                startButton.setVisibility(View.GONE);
                addImage.setImageResource(R.drawable.ic_and);
                isVisible = false;
                sInfo.setVisibility(View.GONE);
                bottomImage.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                faceMatch();
            }
        });

    }

    //申请使用相册权限
    private void openAlbum() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    //得到申请权限结果：同意|拒绝
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

    //展示对比结果至主界面
    public void showResult() {
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

                Bitmap target = BitmapUtil.stringtoBitmap(dbImages.get(maxLikeImgId));
                chosenImageLeft.setImageBitmap(target);

                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                bottomImage.setVisibility(View.GONE);
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

                if (maxLikeDegree >= 85) {
                    likeEva.setText("同一个人的可能性极高");
                }
                else if (maxLikeDegree >= 60) {
                    likeEva.setText("同一个人的可能性较高");
                }
                else if (maxLikeDegree >= 40) {
                    likeEva.setText("同一个人的可能性较低");
                }
                else {
                    likeEva.setText("同一个人的可能性极低");
                }

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

    //开始进行人脸对比
    public void faceMatch() {

        dbImages = ImageDataUtil.getDatabaseImage(this);
        iteratorDBImg = dbImages.entrySet().iterator();
        Integer max = dbImages.size();

        if (!iteratorDBImg.hasNext()) {
            errorInfo("数据库为空");
            return;
        }
        else if (chosenImageRightBitmap == null) {
            errorInfo("未设置需对比人脸");
            return;
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setText("正在对比中......");
                progressBar.setMax(max);
                progressBar.setProgress(0);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                try {

                    String accessToken = AuthService.getAuth();
                    Integer prog = 1;

                    while (iteratorDBImg.hasNext()) {

                        Map.Entry<Integer, String> next = iteratorDBImg.next();
//            byte[] bytes = FileUtil.readFileByBytes("D:\\JavaProjects\\ContrastTestBaidu\\src\\main\\resources\\1.png");
                        String encode1 = next.getValue();

                        Integer finalProg = prog;


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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chosenImageLeft.setImageBitmap(BitmapUtil.stringtoBitmap(next.getValue()));
                                    progressBar.setProgress(finalProg);
                                }
                            });

                        }
                        else {
                            errorInfo(resultMsg);
                            return;
                        }
                        prog++;
                    }

                    showResult();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return;
    }

    //得到从相册选取的图片的URI并转化为Bitmap
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

    //通过URI将图片转化为Bitmap
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

    //展示错误信息
    private void errorInfo(ResultMsg resultMsg) {

        String errorMsg = resultMsg.getError_msg();
        final Integer[] errorCode = {resultMsg.getError_code()};

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                sInfo.setVisibility(View.GONE);
                bottomImage.setVisibility(View.VISIBLE);

                String info = null;
                if (errorCode[0] == 222202) {
                    info = "存在图片未检测到人脸。";
                }
                else if (errorCode[0] == 222304) {
                    info = "存在上传的图像文件太大。\n请确保图片尺寸在1920x1080以下。";
                }
                else {
                    info = "ERROR CODE: " + errorCode + "\n" + "ERROR MSG: " + errorMsg;
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
                dialogTextLeft.setVisibility(View.GONE);

                dialogTitle.setText("错误信息");
                dialogInfo.setText(info);
            }
        });

    }

    private void errorInfo(String paramString) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                sInfo.setVisibility(View.GONE);
                bottomImage.setVisibility(View.VISIBLE);

                String info = null;
                if (paramString.equals("数据库为空")) {
                    info = "人脸数据库为空！\n请添加学生人脸至数据库中再进行对比！";
                }
                else if (paramString.equals("未设置需对比人脸")) {
                    info = "未设置需对比人脸！\n请设置需要对比的人脸！";
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
                dialogTextLeft.setVisibility(View.GONE);

                dialogTitle.setText("错误信息");
                dialogInfo.setText(info);
            }
        });

    }
}
