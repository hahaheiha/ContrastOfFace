package com.cof;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

public class ImageDataUtil {

    public static HashMap<Integer, String> getDatabaseImage(Context context) {
        HashMap<Integer, String> dataMap = new HashMap<>();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select imageid, base64 from imagedb", null);
        if (cursor.moveToFirst()) {
            do {
                int imageid = cursor.getInt(cursor.getColumnIndex("imageid"));
                String base64 = cursor.getString(cursor.getColumnIndex("base64"));
                dataMap.put(imageid, base64);
            } while (cursor.moveToNext());
        }
        return dataMap;
    }
}
