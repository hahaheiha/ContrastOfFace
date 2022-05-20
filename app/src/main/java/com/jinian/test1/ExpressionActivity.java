package com.jinian.test1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jinian.test1.utils.BitmapUtil;
import com.jinian.test1.utils.DatabaseHelper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.ExpressionAdapter;
import entity.Expression;

public class ExpressionActivity extends AppCompatActivity {

    private static final int CHOOSE_PHOTO = 1;

    public static final String EXP_IMAGE_ID = "exp_image_id";


    private List<Expression> expList = new ArrayList<>();
    private ExpressionAdapter adapter;

    RecyclerView recyclerView;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expression);


        dbHelper = DatabaseHelper.getInstance(this);
        db = dbHelper.getWritableDatabase();
        buildExpList();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ExpressionAdapter(expList);
        recyclerView.setAdapter(adapter);
        FloatingActionButton selfAdd = (FloatingActionButton) findViewById(R.id.selfAdd);
        selfAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ExpressionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExpressionActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                else {
                    openAlbum();
                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
//                    int position = adapter.getItemCount();
                    Uri uri = data.getData();
                    Bitmap bitmap = getBitmapFromUri(uri);
                    String imageBase64 = BitmapUtil.bitmaptoString(bitmap);

                    db.execSQL("insert into imagedb(base64) values(?)", new String[]{imageBase64});
                    Cursor cursor = db.rawQuery("select imageid from imagedb order by imageid desc", null);
                    int imageId = 0;
                    if (cursor.moveToFirst()) {
                        imageId = cursor.getInt(cursor.getColumnIndex("imageid"));
                    }
                    expList.add(0, new Expression(imageId));
                    adapter.notifyItemInserted(0);
                    recyclerView.getLayoutManager().scrollToPosition(0);
                }
        }
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

    private void buildExpList() {
        Cursor cursor = db.rawQuery("select imageid from imagedb", null);
        expList.clear();
        if (cursor.moveToFirst()) {
            do {
//                String name = cursor.getString(cursor.getColumnIndex("name"));
                int imageid = cursor.getInt(cursor.getColumnIndex("imageid"));
                expList.add(new Expression(imageid));
            } while (cursor.moveToNext());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int deletePosition;
        if ((deletePosition = intent.getIntExtra("deletePosition", -1)) != -1) {
            expList.remove(deletePosition);
            adapter.notifyItemRemoved(deletePosition);
            adapter.notifyItemRangeChanged(deletePosition, expList.size() - deletePosition);
        }
    }


}