package com.cof.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ImageDataUtil {

    public static LinkedHashMap<Integer, String> getDatabaseImage(Context context) {
        LinkedHashMap<Integer, String> dataMap = new LinkedHashMap<>();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select imageid, base64 from imagedb", null);
        if (cursor.moveToFirst()) {
            do {
                Integer imageid = cursor.getInt(cursor.getColumnIndex("imageid"));
                String base64 = cursor.getString(cursor.getColumnIndex("base64"));
                dataMap.put(imageid, base64);
            } while (cursor.moveToNext());
        }
        return dataMap;
    }
}
