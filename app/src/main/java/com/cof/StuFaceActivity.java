package com.cof;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cof.utils.BitmapUtil;
import com.cof.utils.CustomerDialog;
import com.cof.utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.cof.adapter.StuFaceAdapter;
import com.cof.entity.StuFace;

public class StuFaceActivity extends AppCompatActivity {

    private static final int CHOOSE_PHOTO = 1;

    public static final String STU_IMAGE_ID = "stu_image_id";


    private List<StuFace> stuList = new ArrayList<>();
    private StuFaceAdapter adapter;

    RecyclerView recyclerView;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_face);


        dbHelper = DatabaseHelper.getInstance(this);
        db = dbHelper.getWritableDatabase();
        buildStuList();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StuFaceAdapter(stuList);
        recyclerView.setAdapter(adapter);
        FloatingActionButton selfAdd = (FloatingActionButton) findViewById(R.id.selfAdd);
        selfAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(StuFaceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StuFaceActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                else {
                    openAlbum();
                }
            }
        });




    }

    /**
     * 从相册获取图片的URI并转化为Bitmap
     * 并弹出输入框，录入学生信息
     * 点击确认时将学生信息及照片添加至数据库中
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                    Bitmap bitmap = getBitmapFromUri(uri);

                    CustomerDialog inputDialog = new CustomerDialog(this, R.style.Dialog, R.layout.input_dialog);
                    inputDialog.setCanceledOnTouchOutside(false);
                    inputDialog.show();

                    ImageView sPreview = (ImageView) inputDialog.findViewById(R.id.sPreview);
                    sPreview.setImageBitmap(bitmap);

                    TextInputEditText sNo = (TextInputEditText) inputDialog.findViewById(R.id.sNo);
                    TextInputEditText sName = (TextInputEditText) inputDialog.findViewById(R.id.sName);
                    TextInputEditText sGrade = (TextInputEditText) inputDialog.findViewById(R.id.sGrade);
                    TextInputEditText sRoom = (TextInputEditText) inputDialog.findViewById(R.id.sRoom);
                    TextInputEditText sPhone = (TextInputEditText) inputDialog.findViewById(R.id.sPhone);
                    TextInputEditText sTeacher = (TextInputEditText) inputDialog.findViewById(R.id.sTeacher);
                    TextInputEditText sTPhone = (TextInputEditText) inputDialog.findViewById(R.id.sTPhone);

                    TextView dCancel = (TextView) inputDialog.findViewById(R.id.dialog_cancel);
                    TextView dAssure = (TextView) inputDialog.findViewById(R.id.dialog_assure);



                    dAssure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String imageBase64 = BitmapUtil.bitmaptoString(bitmap);

                            String sno = sNo.getText().toString();
                            String sname = sName.getText().toString();
                            String sgrade = sGrade.getText().toString();
                            String sroom = sRoom.getText().toString();
                            String sphone = sPhone.getText().toString();
                            String steacher = sTeacher.getText().toString();
                            String stphone = sTPhone.getText().toString();

                            db.execSQL("insert into imagedb(base64, sno, sname, sgrade, sroom, sphone, steacher, stphone) values(?,?,?,?,?,?,?,?)",
                                    new String[]{imageBase64, sno, sname, sgrade, sroom, sphone, steacher, stphone});

//                            System.out.println(sno+ "|" + sname+ "|" + sgrade+ "|" + sroom+ "|" + sphone+ "|" + steacher+ "|" + stphone);

                            Cursor cursor = db.rawQuery("select imageid from imagedb order by imageid desc", null);
                            int imageId = 0;
                            if (cursor.moveToFirst()) {
                                imageId = cursor.getInt(cursor.getColumnIndex("imageid"));
                            }
                            stuList.add(0, new StuFace(imageId));
                            adapter.notifyItemInserted(0);
                            recyclerView.getLayoutManager().scrollToPosition(0);
                            inputDialog.dismiss();
                            Toast.makeText(StuFaceActivity.this, "添加成功", Toast.LENGTH_SHORT).show();

                        }
                    });

                    dCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            inputDialog.dismiss();
                            Toast.makeText(StuFaceActivity.this, "取消添加", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * 获取相册使用权限结果：同意｜拒绝
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * 通过从相册获取的图片的Uri，将图片转化为Bitmap
     * @param uri
     * @return
     */
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

    /**
     * 获取数据库中所有学生的数据库中的id
     */
    private void buildStuList() {
        Cursor cursor = db.rawQuery("select imageid from imagedb", null);
        stuList.clear();
        if (cursor.moveToFirst()) {
            do {
//                String name = cursor.getString(cursor.getColumnIndex("name"));
                int imageid = cursor.getInt(cursor.getColumnIndex("imageid"));
                stuList.add(new StuFace(imageid));
            } while (cursor.moveToNext());
        }
    }

    /**
     * 当用户删除学生数据时，此方法可以刷新学生列表，得到删除后的布局
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int deletePosition;
        if ((deletePosition = intent.getIntExtra("deletePosition", -1)) != -1) {
            stuList.remove(deletePosition);
            adapter.notifyItemRemoved(deletePosition);
            adapter.notifyItemRangeChanged(deletePosition, stuList.size() - deletePosition);
        }
    }


}