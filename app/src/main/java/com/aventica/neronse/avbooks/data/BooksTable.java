package com.aventica.neronse.avbooks.data;

import android.database.sqlite.SQLiteDatabase;

public class BooksTable {
    public static final String TABLE_BOOKS = "avbooks";
    public static final String COLUMN_ID = "_id";
    public static final String AUTHORS = "authors";
    public static final String TITLE = "title";
    public static final String SMALL_IMAGE = "smallThumbnail";
    public static final String LARGE_IMAGE = "largeThumbnail";
    public static final String DESCRIPTION = "description";
    public static final String PREVIEW_LINK = "previewLink";

    private static final String GBOOKS_CREATE = "create table " + TABLE_BOOKS + " (" + COLUMN_ID + " integer primary key autoincrement, " +
            AUTHORS + " text not null, " + TITLE + " text not null, " + SMALL_IMAGE + " text not null, " + LARGE_IMAGE + " text not null, " + DESCRIPTION  +" text not null, " + PREVIEW_LINK + " text not null);";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(GBOOKS_CREATE);

    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(sqLiteDatabase);
    }
}
