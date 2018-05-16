package com.aventica.neronse.avbooks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aventica.neronse.avbooks.data.BooksTable;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SelectDialog.SelectDialogListener {
    private static final String TERM = "LAST_SEARCH_TERM";
    private static final String PREFIX = "PREFIX";
    public static final String LARGE_IMAGE_URL = "LARGE_IMAGE_URL";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String TITLE = "TITLE";
    public static final String AUTHOR = "AUTHOR";
    public static final String PREVIEW = "PREVIEW_LINK";

    private MaterialSearchView mSearchView;
    private CursorAdapter mCursorAdapter;
    private ListView mListView;
    private String term;
    private String prefix;
    private TextView tvTerm;
    private TextView tvPerfix;
    private SharedPreferences sPref;
    private ProgressBar mProgressBar;
    //порог видимых элементов для загрузки новой порции книг
    private static final int threshold = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCursorAdapter = new MyCursorAdapter(this, null, 0);
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mCursorAdapter);
        tvPerfix = findViewById(R.id.prefix);
        tvTerm = findViewById(R.id.term);
        mSearchView = findViewById(R.id.search_view);
        mProgressBar = findViewById(R.id.loading);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadData();
        setTextPrefix(prefix);

        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isNetworkReady() && !TextUtils.isEmpty(query)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    term = query;
                    tvTerm.setText(term);
                    query = prefix + query;
                    GBooksService.startActionSearch(MainActivity.this, query);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_IDLE:
                        if (isNetworkReady() && mListView.getLastVisiblePosition() >= mListView.getCount() - threshold) {
                            long startIndex = mListView.getLastVisiblePosition() + 1;
                            GBooksService.startActionMoreBooks(MainActivity.this, term, startIndex);
                            mProgressBar.setVisibility(View.VISIBLE);
                        }
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Cursor c = mCursorAdapter.getCursor();
            c.moveToPosition(position);
            String largeImageUrl = c.getString(c.getColumnIndex(BooksTable.LARGE_IMAGE));
            String description = c.getString(c.getColumnIndex(BooksTable.DESCRIPTION));
            String title = c.getString(c.getColumnIndex(BooksTable.TITLE));
            String author = c.getString(c.getColumnIndex(BooksTable.AUTHORS));
            String previewLink = c.getString(c.getColumnIndex(BooksTable.PREVIEW_LINK));
            Intent intent = new Intent(MainActivity.this, DetailInfo.class);
            intent.putExtra(LARGE_IMAGE_URL, largeImageUrl);
            intent.putExtra(DESCRIPTION, description);
            intent.putExtra(TITLE, title);
            intent.putExtra(AUTHOR,author);
            intent.putExtra(PREVIEW, previewLink);
            startActivity(intent);
        });

        getSupportLoaderManager().initLoader(111, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this, AVBooksContentProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_settings:
                SelectDialog.newInstance().show(getSupportFragmentManager(), "SelectDialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkReady() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TERM, term);
        outState.putString(PREFIX, prefix);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        term = savedInstanceState.getString(TERM);
        tvTerm.setText(term);
        prefix = savedInstanceState.getString(PREFIX);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSelectType(String type) {
        if(isNetworkReady()) {
            prefix = type;
            setTextPrefix(type);
            if(!TextUtils.isEmpty(term)) {
                String query = prefix + term;
                mProgressBar.setVisibility(View.VISIBLE);
                GBooksService.startActionSearch(this, query);
            }else
                Toast.makeText(this, R.string.search_request_alert, Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this, R.string.internet_alert, Toast.LENGTH_SHORT).show();
    }

    private void setTextPrefix(String prefix) {
        switch (prefix) {
            case "inauthor:":
                tvPerfix.setText(R.string.tv_prefix_text1);
                break;
            case "intitle:":
                tvPerfix.setText(R.string.tv_prefix_text2);
                break;
            case "inpublisher:":
                tvPerfix.setText(R.string.tv_prefix_text3);
                break;
            default:
                tvPerfix.setText(R.string.tv_prefix_text1);
        }
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    private void saveData(){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = sPref.edit();
        edit.putString(TERM, term);
        edit.putString(PREFIX,prefix);
        edit.apply();
    }

    private void loadData(){
        sPref = getPreferences(MODE_PRIVATE);
        term = sPref.getString(TERM, "");
        tvTerm.setText(term);
        prefix = sPref.getString(PREFIX, "inauthor:");
    }
}

