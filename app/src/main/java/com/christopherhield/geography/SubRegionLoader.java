package com.christopherhield.geography;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.Log;

import com.caverock.androidsvg.SVG;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.net.HttpURLConnection.HTTP_OK;

public class SubRegionLoader implements Runnable {

    private static final String TAG = "SubRegionLoader";
    private final MainActivity mainActivity;
    private final String selectedSubRegion;

    private static final String dataURL = "https://newsapi.org/v2/top-headlines?sources=";

    private static String APIKey = "&apiKey=";

    SubRegionLoader(MainActivity ma, String selectedSubRegion) {
        mainActivity = ma;
        this.selectedSubRegion = selectedSubRegion;
    }

    public void run() {

        Uri dataUri = Uri.parse(dataURL + selectedSubRegion + APIKey + mainActivity.getString(R.string.Api_Key));
        String urlToUse = dataUri.toString();

        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            StringBuilder sb = new StringBuilder();
            String line;

            if (conn.getResponseCode() == HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getInputStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                MainActivity.articles = parseJSON(sb.toString());
                mainActivity.runOnUiThread(() -> mainActivity.setCountries());

            } else {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getErrorStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                Log.d(TAG, "run: " + sb.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Article> parseJSON(String s) {

        ArrayList<Article> countryList = new ArrayList<>();
        try {
            JSONArray jObjMain = new JSONObject(s).getJSONArray("articles");

            // Here we only want to regions and subregions
            for (int i = 0; i < (jObjMain.length()>10?10:jObjMain.length()); i++) {
                JSONObject jCountry = (JSONObject) jObjMain.get(i);
                JSONObject sourceObj = jCountry.getJSONObject("source");
                Source source = new Source(sourceObj.getString("id"), sourceObj.getString("name"));
                String author = jCountry.getString("author");
                String title = jCountry.getString("title");
                String description = jCountry.getString("description");
                String url = jCountry.getString("url");
                String urlToImage = jCountry.getString("urlToImage");
                String publishedAt = jCountry.getString("publishedAt");
                String content = jCountry.getString("content");

                Article article = new Article(source, author, title, description, url, urlToImage, publishedAt, content);
                Log.d(TAG, "parseJSON: article: " + article);
//                if (subRegion.isEmpty())
//                    subRegion = "Unspecified";

//                if (!source.getName().equals(selectedSubRegion))
//                    continue;
                countryList.add(article);
            }
            return countryList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
