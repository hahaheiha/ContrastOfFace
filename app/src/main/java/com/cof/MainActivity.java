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
            dialogTextRight.setText("????????????");
            dialogTextRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogTitle.setText("????????????");
            dialogInfo.setText("?????????????????????????????????????????????????????????");
        }



        sInfo = (MaterialCardView) findViewById(R.id.sInfo);

        chosenImageRight = (ImageView) findViewById(R.id.chosenImageRight);
        chosenImageLeft = (ImageView) findViewById(R.id.chosenImageLeft);
        bottomImage = (ImageView) findViewById(R.id.bottomDefault);

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

            dialogTextLeft.setText("????????????");
            dialogTextLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = getSharedPreferences("isTip", MODE_PRIVATE).edit();
                    editor.putBoolean("isShowAgain", false);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialogTextRight.setText("????????????");
            dialogTextRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialogTitle.setText("???????????????");
            dialogInfo.setText("???????????????????????????????????????\n????????????????????????1920x1080?????????");

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
                Intent intent = new Intent(MainActivity.this, StuFaceActivity.class);
                startActivity(intent);
            }
        });


        chosenImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StuFaceActivity.class);
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

    //????????????????????????
    private void openAlbum() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    //?????????????????????????????????|??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }
                else {
                    Toast.makeText(this, "??????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    //??????????????????????????????
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
                    likeEva.setText("??????????????????????????????");
                }
                else if (maxLikeDegree >= 60) {
                    likeEva.setText("??????????????????????????????");
                }
                else if (maxLikeDegree >= 40) {
                    likeEva.setText("??????????????????????????????");
                }
                else {
                    likeEva.setText("??????????????????????????????");
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

    //????????????????????????
    public void faceMatch() {
        //???????????????????????????
        dbImages = ImageDataUtil.getDatabaseImage(this);
        iteratorDBImg = dbImages.entrySet().iterator();
        Integer max = dbImages.size();
        //?????????????????????????????????????????????????????????
        if (!iteratorDBImg.hasNext()) {
            errorInfo("???????????????");
            return;
        }
        else if (chosenImageRightBitmap == null) {
            errorInfo("????????????????????????");
            return;
        }
        //???????????????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setText("???????????????......");
                progressBar.setMax(max);
                progressBar.setProgress(0);
            }
        });
        //????????????????????????????????????????????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                try {

                    String accessToken = AuthService.getAuth();
                    Integer prog = 1;

                    while (iteratorDBImg.hasNext()) {
                        //???????????????????????????
                        Map.Entry<Integer, String> next = iteratorDBImg.next();
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
                        // ????????????????????????????????????????????????????????????access_token???????????????access_token?????????????????? ???????????????????????????????????????????????????
                        String result = HttpUtil.post(url, accessToken, "application/json", params.toString());
                        ResultMsg resultMsg = GsonUtils.fromJson(result, ResultMsg.class);
                        //???????????????
                        if (resultMsg.getError_code() == 0) {
                            Result conResult = resultMsg.getResult();
                            if (conResult.getScore().intValue() > maxLikeDegree) {
                                maxLikeImgId = next.getKey();
                                maxLikeDegree = conResult.getScore().intValue();
                            }
                            //??????processBar
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
                    //????????????
                    showResult();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return;
    }

    //?????????????????????????????????URI????????????Bitmap
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

    //??????URI??????????????????Bitmap
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

    //??????????????????
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
                    info = "?????????????????????????????????";
                }
                else if (errorCode[0] == 222304) {
                    info = "????????????????????????????????????\n????????????????????????1920x1080?????????";
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
                dialogTextRight.setText("????????????");
                dialogTextRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialogTextLeft.setVisibility(View.GONE);

                dialogTitle.setText("????????????");
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
                if (paramString.equals("???????????????")) {
                    info = "????????????????????????\n??????????????????????????????????????????????????????";
                }
                else if (paramString.equals("????????????????????????")) {
                    info = "???????????????????????????\n?????????????????????????????????";
                }

                Dialog dialog = new CustomerDialog(MainActivity.this, R.style.Dialog, R.layout.dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
                TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
                TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
                TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);
                dialogTextRight.setText("????????????");
                dialogTextRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialogTextLeft.setVisibility(View.GONE);

                dialogTitle.setText("????????????");
                dialogInfo.setText(info);
            }
        });

    }
}
