package com.aventica.neronse.avbooks;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.aventica.neronse.avbooks.data.BooksTable;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.util.List;

public class GBooksService extends IntentService {
    private static final String ACTION_SEARCH = "com.aventica.neronse.avbooks.action.SEARCH";
    private static final String ACTION_MORE = "com.aventica.neronse.avbooks.action.MORE_BOOKS";

    private static final String QUERY = "com.aventica.neronse.avbooks.extra.PARAM1";
    private static final String START_INDEX = "com.aventica.neronse.avbooks.extra.PARAM2";

    private Handler mHandler;

    public GBooksService() {
        super("GBooksService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }


    public static void startActionSearch(Context context, String query) {
        context.getContentResolver().delete(AVBooksContentProvider.CONTENT_URI, null, null);
        Intent intent = new Intent(context, GBooksService.class);
        intent.setAction(ACTION_SEARCH);
        intent.putExtra(QUERY, query);
        context.startService(intent);
    }

    public static void startActionMoreBooks(Context context, String query, long startIndex) {
        Intent intent = new Intent(context, GBooksService.class);
        intent.setAction(ACTION_SEARCH);
        intent.putExtra(QUERY, query);
        intent.putExtra(START_INDEX, startIndex);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        try {
            final Books books = new Books.Builder(new NetHttpTransport(), jsonFactory, null)
                    .setApplicationName(BuildConfig.APPLICATION_NAME)
                    .setGoogleClientRequestInitializer(new BooksRequestInitializer(BuildConfig.API_KEY))
                    .build();
            if (intent != null) {
                final String action = intent.getAction();
                if (ACTION_SEARCH.equals(action)) {
                    final String query = intent.getStringExtra(QUERY);
                    Books.Volumes.List volumesList = books.volumes().list(query).setMaxResults(40L).setPrintType("books");
                    Volumes volumes = volumesList.execute();
                    handleActionSearch(volumes);
                } else if (ACTION_MORE.equals(action)) {
                    final String query = intent.getStringExtra(QUERY);
                    final long startIndex = intent.getLongExtra(START_INDEX, 0);
                    Books.Volumes.List volumesList = books.volumes().list(query).setMaxResults(40L).setStartIndex(startIndex).setPrintType("books");
                    Volumes volumes = volumesList.execute();
                    handleActionSearch(volumes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleActionSearch(Volumes volumes) {
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
            mHandler.post(() -> Toast.makeText(getApplicationContext(), R.string.no_found_alert, Toast.LENGTH_SHORT).show());
            return;
        }
        List<Volume> volumeItems = volumes.getItems();
        ContentValues[] values = new ContentValues[volumeItems.size()];
        for (int i = 0; i < values.length; i++) {
            Volume volume = volumeItems.get(i);
            ContentValues cv = new ContentValues();

            java.util.List<String> authors = volume.getVolumeInfo().getAuthors();
            StringBuilder sb = new StringBuilder();
            if (authors != null && !authors.isEmpty()) {
                for (int j = 0; j < authors.size(); j++) {
                    sb.append(authors.get(j));
                    if (j < authors.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            cv.put(BooksTable.AUTHORS, sb.toString());
            cv.put(BooksTable.TITLE, volume.getVolumeInfo().getTitle());
            Volume.VolumeInfo.ImageLinks imageLinks = volume.getVolumeInfo().getImageLinks();
            if (imageLinks != null) {
                cv.put(BooksTable.SMALL_IMAGE, imageLinks.getSmallThumbnail());
                cv.put(BooksTable.LARGE_IMAGE, imageLinks.getThumbnail());
            } else {
                cv.put(BooksTable.SMALL_IMAGE, "no image");
                cv.put(BooksTable.LARGE_IMAGE, "no image");
            }

            if (volume.getVolumeInfo().getDescription() != null && volume.getVolumeInfo().getDescription().length() > 0) {
                cv.put(BooksTable.DESCRIPTION, volume.getVolumeInfo().getDescription());
            } else
                cv.put(BooksTable.DESCRIPTION, "No description");
            if (volume.getVolumeInfo().getPreviewLink() != null) {
                cv.put(BooksTable.PREVIEW_LINK, volume.getVolumeInfo().getPreviewLink());
            } else
                cv.put(BooksTable.PREVIEW_LINK, "no preview link");

            values[i] = cv;
        }
        getContentResolver().bulkInsert(AVBooksContentProvider.CONTENT_URI, values);
    }
}
