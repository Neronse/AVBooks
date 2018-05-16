package com.aventica.neronse.avbooks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.aventica.neronse.avbooks.data.BooksDBHelper;
import com.aventica.neronse.avbooks.data.BooksTable;

public class AVBooksContentProvider extends ContentProvider {
    public static final String CONTENT_AUTHORITY = "com.aventica.neronse.avbooks.gbooks";
    public static final Uri CONTENT_URI = Uri.parse(
            "content://" + CONTENT_AUTHORITY + "/elements"
    );
    private static final UriMatcher uriMatcher;

    // вся таблица
    private static final int ALL_ROWS = 100;
    // одна строка
    private static final int SINGLE_ROW = 101;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, "elements", ALL_ROWS);
        // # это число
        uriMatcher.addURI(CONTENT_AUTHORITY, "elements/#", SINGLE_ROW);
    }

    private BooksDBHelper dbHelper;
    private SQLiteStatement statement;

    @Override
    public boolean onCreate() {
        dbHelper = new BooksDBHelper(getContext());

        statement = dbHelper.getWritableDatabase().compileStatement("insert into avbooks (authors, title, smallThumbnail, largeThumbnail, description, previewLink) values (?, ?, ?, ?, ?, ?)");
        return false;
    }

    private String getSelection(Uri uri, String selection) {
        switch (uriMatcher.match(uri)) {
            case SINGLE_ROW:
                String rowID = uri.getPathSegments().get(1);
                selection = BooksTable.COLUMN_ID + "=" + rowID
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;
        }
        return selection;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        selection = getSelection(uri, selection);
        int deleteCount = db.delete(BooksTable.TABLE_BOOKS, selection, selectionArgs);
        if (deleteCount > 0) getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                return "vnd.android.cursor.dir/vnd.gbooks.elemental";
            case SINGLE_ROW:
                return "vnd.android.cursor.item/vnd.gbooks.elemental";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(BooksTable.TABLE_BOOKS, null, values);
        if (id > -1) {
            Uri inserted = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(inserted, null);
            return uri;
        } else return null;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        selection = getSelection(uri, selection);
        Cursor cursor = db.query(BooksTable.TABLE_BOOKS, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        selection = getSelection(uri, selection);

        int updateCount = db.update(BooksTable.TABLE_BOOKS, values, selection, selectionArgs);
        if (updateCount > 0) getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int insertedCount = 0;
        try {
            db.beginTransaction();
            for (ContentValues value : values) {
                statement.bindString(1, value.getAsString(BooksTable.AUTHORS));
                statement.bindString(2, value.getAsString(BooksTable.TITLE));
                statement.bindString(3, value.getAsString(BooksTable.SMALL_IMAGE));
                statement.bindString(4, value.getAsString(BooksTable.LARGE_IMAGE));
                statement.bindString(5, value.getAsString(BooksTable.DESCRIPTION));
                statement.bindString(6, value.getAsString(BooksTable.PREVIEW_LINK));
                statement.executeInsert();
                insertedCount++;
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            insertedCount = 0;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        if (insertedCount > 0) getContext().getContentResolver().notifyChange(uri, null);
        return insertedCount;
    }
}
