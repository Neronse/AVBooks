package com.aventica.neronse.avbooks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BooksDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_FILE = "avbooks.db";
    private static final  int DATABASE_VERSION = 1;

    public BooksDBHelper(Context context) {
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        BooksTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        BooksTable.onUpgrade(db, oldVersion, newVersion);
    }
}
