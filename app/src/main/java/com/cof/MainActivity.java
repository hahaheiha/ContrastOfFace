package com.cof;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.cof.utils.ImageDataUtil;
import com.cof.utils.ShowBigPhoto;
import com.cof.utils.baidu.AuthService;
import com.cof.utils.baidu.Base64Util;
import com.cof.utils.baidu.GsonUtils;
import com.cof.utils.baidu.HttpUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jinian.test1.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import entity.Result;
import entity.ResultMsg;

public class MainActivity extends AppCompatActivity {

    public int leftCallCount = 0;   //左图调用次数
    public int rightCallCount = 0;  //右图调用次数
    public static final int CHOOSE_PHOTO = 2;
    private ImageView chosenImageRight;
    private ImageView chosenImageLeft;
    private ImageView resultImage;
    private TextView saveText;

    byte[] templateImgBytes = null;
    byte[] mergeImgBytes = null;


    private boolean isVisible;
    private boolean isShowAgain = true;

    private Button rightAddButton;
    private Button leftAddButton;
    private FloatingActionButton addImage;

    private Bitmap chosenImageRightBitmap;
    private Bitmap chosenImageLeftBitmap;
    private Bitmap resultMergeBitmap;

//    private String accessToken;

    private String resultData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
        }else{
            Dialog dialog = new CustomerDialog(this, R.style.Dialog);
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



//        new Runnable() {
//            @Override
//            public void run() {
//                accessToken = AuthService.getAuth();
//                System.out.println(accessToken);
//            }
//        };




        chosenImageRight = (ImageView) findViewById(R.id.chosenImageRight);
        chosenImageLeft = (ImageView) findViewById(R.id.chosenImageLeft);
        resultImage = (ImageView) findViewById(R.id.resultImage);
        saveText = (TextView) findViewById(R.id.saveText);

        addImage =  (FloatingActionButton) findViewById(R.id.floatingButtonCenter);
        rightAddButton = (Button) findViewById(R.id.addRight);
        leftAddButton = (Button) findViewById(R.id.addLeft);

        rightAddButton.setVisibility(View.INVISIBLE);
        leftAddButton.setVisibility(View.INVISIBLE);

        SharedPreferences pref = getSharedPreferences("isTip", MODE_PRIVATE);
        isShowAgain = pref.getBoolean("isShowAgain", true);

        if (isShowAgain) {
            Dialog dialog = new CustomerDialog(this, R.style.Dialog);
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

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVisible) {
                    rightAddButton.setVisibility(View.VISIBLE);
                    leftAddButton.setVisibility(View.VISIBLE);
                    addImage.setImageResource(R.drawable.ic_cancel);
                    isVisible = true;
                }
                else {
                    rightAddButton.setVisibility(View.INVISIBLE);
                    leftAddButton.setVisibility(View.INVISIBLE);
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


        resultImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (resultMergeBitmap == null) {
                    Toast.makeText(MainActivity.this, "未获取到融合图，请检查后重试。", Toast.LENGTH_SHORT).show();
                    return false;
                }

                DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String bitName = format.format(new Date()) + ".JPEG";
                Dialog dialog = new CustomerDialog(MainActivity.this, R.style.Dialog);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
                TextView dialogInfo = (TextView) dialog.findViewById(R.id.dialog_info);
                TextView dialogTextRight = (TextView) dialog.findViewById(R.id.dialog_text_right);
                TextView dialogTextLeft = (TextView) dialog.findViewById(R.id.dialog_text_left);
                dialogTextRight.setText("确定");
                dialogTextRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        saveImage(resultMergeBitmap, bitName);
                        dialog.dismiss();
                    }
                });
                dialogTextLeft.setText("取消");
                dialogTextLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialogTitle.setText("保存");
                dialogInfo.setText("是否保存该图片？\n保存路径为: DCIM/" + bitName);

                return false;
            }
        });

        resultImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShowBigPhoto showBigPhoto = new ShowBigPhoto(MainActivity.this);
                showBigPhoto.show();
                ImageView detailPhoto = (ImageView) showBigPhoto.findViewById(R.id.detailPhoto);
                detailPhoto.setImageBitmap(resultMergeBitmap);
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
//                showBigPhoto.showDetailPhoto();
            }
        });

        chosenImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosenImageLeftBitmap == null) return;

                ShowBigPhoto showBigPhoto = new ShowBigPhoto(MainActivity.this);
                showBigPhoto.show();
                ImageView detailPhoto = (ImageView) showBigPhoto.findViewById(R.id.detailPhoto);
                detailPhoto.setImageBitmap(chosenImageLeftBitmap);
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getIntExtra(ExpressionActivity.EXP_IMAGE_ID, -1) != -1) {
            setSelectImg(intent);
        }
    }

    private void setSelectImg(Intent intent) {
        leftCallCount++;
        int imageid = intent.getIntExtra(ExpressionActivity.EXP_IMAGE_ID, 0);
        HashMap<Integer, String> databaseImageMap = ImageDataUtil.getDatabaseImage(this);
        chosenImageLeftBitmap = BitmapUtil.stringtoBitmap(databaseImageMap.get(imageid));
        templateImgBytes = BitmapUtil.toByteArray(chosenImageLeftBitmap);
        chosenImageLeft.setImageBitmap(chosenImageLeftBitmap);
        saveText.setText("");
        if(leftCallCount != rightCallCount) {
            rightCallCount = leftCallCount - 1;
            mergeImgBytes = null;
            resultImage.setImageBitmap(null);
            resultMergeBitmap = null;
            chosenImageRight.setImageBitmap(null);
            chosenImageRightBitmap = null;
            return;
        }
        createFacePP();
    }

    private void openAlbum() {
//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
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

    public void createFacePP() {
//            new Runnable() {
//                @Override
//                public void run() {
                    String result = faceMatch();
                    System.out.println(result);
//                }
//            };

//        if(templateImgBytes == null || mergeImgBytes == null) return;
//        FacePPApi facePPApi = new FacePPApi("PlK7L0c71QMIEhowrGDLuZcznML2qDHS","h3RYwP1_2jriAZaJqAQ1DansKH-5srfE-5srfE");
//        HashMap<String, String> map = new HashMap<>();
////        map.put("merge_rate", "50");
////        map.put("feature_rate", "50");
//
//
//        facePPApi.mergeFace(map, templateImgBytes, mergeImgBytes, new IFacePPCallBack<MergeFaceResponse>() {
//            @Override
//            public void onSuccess(MergeFaceResponse paramT) {
//                showMergeResult(paramT);
//            }
//
//            @Override
//            public void onFailed(String paramString) {
//                System.out.println(paramString);
//                errorInfo(paramString);
//            }
//        });
    }

    public String faceMatch() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
                try {

                    String accessToken = AuthService.getAuth();

//            byte[] bytes = FileUtil.readFileByBytes("D:\\JavaProjects\\ContrastTestBaidu\\src\\main\\resources\\1.png");
                    String encode1 = Base64Util.encode(templateImgBytes);

//            byte[] bytes1 = FileUtil.readFileByBytes("D:\\JavaProjects\\ContrastTestBaidu\\src\\main\\resources\\2.png");
                    String encode2 = Base64Util.encode(mergeImgBytes);


                    ArrayList<String> params = new ArrayList<>();

                    HashMap<String, String> map = new HashMap<>();

                    map.put("image", encode1);
                    map.put("image_type", "BASE64");
                    map.put("face_type", "LIVE");
//            map.put("quality_control", "");
//            map.put("liveness_control", "");


                    params.add(GsonUtils.toJson(map));

                    map.clear();

                    map.put("image", encode2);
                    map.put("image_type", "BASE64");
                    map.put("face_type", "LIVE");
//            map.put("quality_control", );
//            map.put("liveness_control", );

                    params.add(GsonUtils.toJson(map));
//
//            HashMap<String, String> map = new HashMap<>();

//            String param = GsonUtils.toJson(map);



                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
//            String accessToken = "[调用鉴权接口获取的token]";

                    String result = HttpUtil.post(url, accessToken, "application/json", params.toString());
                    showMergeResult(result);
//                    System.out.println(result);
////                    return result;
//                    resultData = result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        TestBackGround testBackGround = new TestBackGround();
//        String accessToken = testBackGround.doInBackground(null);

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
                    detailPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showBigPhoto.dismiss();
                        }
                    });

                    Button detailCancelButton = (Button) showBigPhoto.findViewById(R.id.detailCancelButton);
                    detailCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            if (rightCallCount != leftCallCount) {
//                                leftCallCount = rightCallCount - 1;
//                                resultImage.setImageBitmap(null);
//                                resultMergeBitmap = null;
//                                chosenImageLeftBitmap = null;
//                                chosenImageLeft.setImageBitmap(null);
//                                chosenImageRightBitmap = null;
//                                chosenImageRight.setImageBitmap(null);
//                                mergeImgBytes = null;
//                            }
                            showBigPhoto.dismiss();
                        }
                    });

                    Button detailSureButton = (Button) showBigPhoto.findViewById(R.id.detailSureButton);
                    detailSureButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rightCallCount++;
                            if (rightCallCount != leftCallCount) {
                                leftCallCount = rightCallCount - 1;
                                templateImgBytes = null;
                                resultImage.setImageBitmap(null);
                                resultMergeBitmap = null;
                                chosenImageLeftBitmap = null;
                                chosenImageLeft.setImageBitmap(null);
                            }
                            saveText.setText("");
                            chosenImageRight.setImageBitmap(chosenImageRightBitmap);
                            mergeImgBytes = BitmapUtil.toByteArray(chosenImageRightBitmap);
                            showBigPhoto.dismiss();
                            createFacePP();
                        }
                    });
//                    if (rightCallCount != leftCallCount) {
//                        leftCallCount = rightCallCount - 1;
//                        resultMergeBitmap = null;
//                        resultImage.setImageBitmap(null);
//                        chosenImageLeftBitmap = null;
//                        chosenImageLeft.setImageBitmap(null);
//                        mergeImgBytes = null;
//                        return;
//                    }

                }
                break;
        }
    }

    private void showMergeResult(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rightAddButton.setVisibility(View.INVISIBLE);
                leftAddButton.setVisibility(View.INVISIBLE);
                addImage.setImageResource(R.drawable.ic_and);
                isVisible = false;
//                resultMergeBitmap = BitmapUtil.stringtoBitmap(paramT.getResult());
//                resultImage.setImageBitmap(resultMergeBitmap);

                ResultMsg resultMsg = GsonUtils.fromJson(result, ResultMsg.class);
                if (resultMsg.getError_code() != 0) {
                    saveText.setText("未知错误！请稍后重试！");
                }
                else {
                    Result result = resultMsg.getResult();
                    saveText.setText(result.getScore().toString());
                }

//                System.out.println(paramT.getConfidence());
            }
        });
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

    private void saveImage(Bitmap bitmap, String bitName) {
        String fileName ;
        File file ;
        fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/"+bitName ;
        file = new File(fileName);
        try {

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
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

                Dialog dialog = new CustomerDialog(MainActivity.this, R.style.Dialog);
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
