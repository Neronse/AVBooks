package com.aventica.neronse.avbooks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

public class DetailInfo extends AppCompatActivity {
    private ImageView imageView;
    private TextView tvDescription;
    private TextView tvTitle;
    private TextView tvAuthor;
    private String previewLink;
    private Button linkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info);
        imageView = findViewById(R.id.book_image);
        tvDescription = findViewById(R.id.description);
        tvAuthor = findViewById(R.id.authorDetail);
        tvTitle = findViewById(R.id.titleDetail);
        linkBtn = findViewById(R.id.preview_btn);

        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.LARGE_IMAGE_URL)) {
            String url = intent.getStringExtra(MainActivity.LARGE_IMAGE_URL);
            Picasso.get().load(url).placeholder(R.drawable.ic_book_black_24dp).into(imageView);
        }
        if (intent.hasExtra(MainActivity.TITLE)) {
            String title = intent.getStringExtra(MainActivity.TITLE);
            tvTitle.setText(title);
        } else
            tvTitle.setText(R.string.detail_no_title);

        if (intent.hasExtra(MainActivity.AUTHOR)) {
            String author = intent.getStringExtra(MainActivity.AUTHOR);
            tvAuthor.setText(author);
        }
        if (intent.hasExtra(MainActivity.DESCRIPTION)) {
            String description = intent.getStringExtra(MainActivity.DESCRIPTION);
            tvDescription.setText(description);
        } else
            tvDescription.setText(R.string.detail_no_description);
        if (intent.hasExtra(MainActivity.PREVIEW)) {
            previewLink = intent.getStringExtra(MainActivity.PREVIEW);
        }

        linkBtn.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(previewLink) && !previewLink.equals("no preview link")) {
                try {
                    new URL(previewLink);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(previewLink));
                    startActivity(i);
                } catch (MalformedURLException e) {
                    Toast.makeText(DetailInfo.this, R.string.preview_link_alert, Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(DetailInfo.this, R.string.preview_link_alert, Toast.LENGTH_SHORT).show();
        });

    }
}
