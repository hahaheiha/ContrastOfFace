package com.cof.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper dbHelperInstance;

    public static final String DATABASE_NAME = "imagedb.db";

    public static final String CREATE_IMAGEDB = "create table imagedb (" +
            "imageid integer primary key autoincrement, " +
            "base64 text," +
            "sno text," +
            "sname text," +
            "sgrade text," +
            "sroom text," +
            "sphone text," +
            "steacher text," +
            "stphone text)";

    private Context mContext;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if (dbHelperInstance == null) {
            dbHelperInstance = new DatabaseHelper(context);
        }
        return dbHelperInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_IMAGEDB);
//        HashMap<String, Integer> expMap = DatabaseUtil.getExpMap();
//        HashMap<String, String> imageMap = DatabaseUtil.getDefaultImageMap();
//
//        ContentValues values = new ContentValues();
//
//        for (Map.Entry<String, String> entry : imageMap.entrySet()) {
//            values.clear();
////            String name = entry.getKey();
//            String base64 = entry.getValue();
////            values.put("name", name);
//            values.put("base64", base64);
//            db.insert("imagedb", null, values);
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        HashMap<String, Integer> expMap = ExpressionActivity.getExpMap();
//        HashMap<String, String> imageMap = DefaultImageBase64.getDefaultImageMap();
//
//        ContentValues values = new ContentValues();
//        for (Map.Entry<String, Integer> entrySet : expMap.entrySet()) {
//            values.clear();
//            String name = entrySet.getKey();
//            Integer imageid = entrySet.getValue();
//            String base64 = imageMap.get(name);
//            values.put("imageid", imageid);
//            values.put("name", name);
//            values.put("base64", base64);
//            db.insert("imagedb", null, values);
//        }
    }
}
