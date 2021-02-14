package com.christopherhield.geography;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;


public class ArticleFragment extends Fragment implements View.OnClickListener {

    public static MainActivity mainActivity;

    public TextView titleTV;
    public TextView dateTV;
    public TextView authorTV;
    public ImageView imageTV;
    public TextView descriptionTV;
    public TextView countTV;

    public Article article;



    public static final ArticleFragment newInstance(MainActivity ma, Article article, int index, int max) {
        mainActivity = ma;
        ArticleFragment fra = new ArticleFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("article", article);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL_COUNT", max);
        fra.setArguments(bdl);
        return fra;
    }

    @Override
    public void onClick(View view) {

    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        System.out.println("ArticleFragment: mark_3");
        View view = inflater.inflate(R.layout.fragment_article,container,false);


        if (savedInstanceState == null) {
            System.out.println("savedInstanceState == null");
            article = (Article) getArguments().getSerializable("article");

        }
        else{
            System.out.println("savedInstanceState != null");
            article = (Article) savedInstanceState.getSerializable("article");
        }

        int index = getArguments().getInt("INDEX");
        int max = getArguments().getInt("TOTAL_COUNT");

        titleTV = view.findViewById(R.id.title);
        dateTV = view.findViewById(R.id.date);
        authorTV = view.findViewById(R.id.author);
        imageTV = view.findViewById(R.id.image);
        descriptionTV = view.findViewById(R.id.description);
        countTV = view.findViewById(R.id.count);

        titleTV.setOnClickListener(this);
        imageTV.setOnClickListener(this);
        descriptionTV.setOnClickListener(this);


        titleTV.setText(article.getTitle());
        authorTV.setText(article.getAuthor());
        dateTV.setText(article.getPublishedAt());
        descriptionTV.setText(article.getDescription());
        countTV.setText((index) + " of " + max);

        descriptionTV.setMovementMethod(new ScrollingMovementMethod());
        imageTV.setImageResource(R.drawable.loading);

        System.out.println("ArticleFragment: mark_1");
        if(checkNetwork()){
            if(article.getUrlToImage() != null){
                final String photoUrl = (article.getUrlToImage());
                Picasso picasso = new Picasso.Builder(mainActivity).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        // Here we try https if the http image attempt failed
                        final String changedUrl = photoUrl.replace("http:", "https:");
                        picasso.load(changedUrl)
                                .error(R.drawable.brokenimage)
                                .placeholder(R.drawable.loading)
                                .into(imageTV);

                    }
                }).build();

                picasso.load(photoUrl)
                        .error(R.drawable.brokenimage)
                        .placeholder(R.drawable.loading)
                        .into(imageTV);

            }
        }
        System.out.println("ArticleFragment: mark_2");

        if(article.getUrl() != null){
            imageTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            titleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });

            descriptionTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
        return view;
    }


    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("article", article);
        super.onSaveInstanceState(outState);
    }

}
