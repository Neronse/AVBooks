package com.aventica.neronse.avbooks;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aventica.neronse.avbooks.data.BooksTable;
import com.squareup.picasso.Picasso;

public class MyCursorAdapter extends CursorAdapter {

    public MyCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.imageView = view.findViewById(R.id.preview_book);
        holder.tvTitle = view.findViewById(R.id.title);
        holder.tvAuthors = view.findViewById(R.id.authors);
        populateView(holder, cursor);
        view.setTag(holder);
        return view;
    }

    private void populateView(ViewHolder holder, Cursor cursor) {
        String url = cursor.getString(cursor.getColumnIndex(BooksTable.SMALL_IMAGE));
        String title = cursor.getString(cursor.getColumnIndex(BooksTable.TITLE));
        String authors = cursor.getString(cursor.getColumnIndex(BooksTable.AUTHORS));
        holder.tvAuthors.setText(authors);
        holder.tvTitle.setText(title);
        Picasso.get().load(url).placeholder(R.drawable.ic_book_black_24dp).into(holder.imageView);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        populateView(holder, cursor);
    }

    public class ViewHolder {
        ImageView imageView;
        TextView tvTitle;
        TextView tvAuthors;
    }
}
