package com.christopherhield.geography;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import static java.net.HttpURLConnection.HTTP_OK;

public class RegionLoader implements Runnable {

    private static final String TAG = "RegionLoader";
    private final MainActivity mainActivity;
    private int form;
    private static final String dataURL = "https://newsapi.org/v2/sources?";
    private static String APIKey = "apiKey=";

    public String currentCategory;
    public String currentLanguage;
    public String currentCountry;

    RegionLoader(MainActivity ma, int form, String currentCategory, String currentLanguage, String currentCountry) {
        mainActivity = ma;
        this.form = form;
        this.currentCategory = currentCategory;
        this.currentLanguage = currentLanguage;
        this.currentCountry = currentCountry;
    }

    @Override
    public void run() {
        System.out.println("mainActivity.currentCategory: " + mainActivity.currentCategory);
        String para =
                (currentCategory.equals("all")?"":("&category=" + currentCategory)) +
                (currentLanguage.equals("all")?"":("&language=" + currentLanguage)) +
                (currentCountry.equals("all")?"":("&country=" + currentCountry));
        Uri dataUri = Uri.parse(dataURL + para + (para.equals("")?"":"&") + APIKey + mainActivity.getString(R.string.Api_Key));
        Log.d(TAG, "run: dataUri: " + dataUri);
        String urlToUse = dataUri.toString();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            StringBuilder sb = new StringBuilder();
            String line;

            int respondCode = conn.getResponseCode();
            if (respondCode == HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader((new InputStreamReader(conn.getInputStream())));

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                conn.disconnect();
                HashSet<Source> regionSet = parseJSON(sb.toString());
                if (regionSet != null)
                    mainActivity.runOnUiThread(() -> {
                        if(form == 0)
                            mainActivity.setupCategories(regionSet);
                        else if(form == 1)
                            mainActivity.setupCategoriesLeft(regionSet);
                        else {
                            mainActivity.setupCategories(regionSet);
                            mainActivity.setupCategoriesLeft(regionSet);
                        }
                    });
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


    private HashSet<Source> parseJSON(String s) {

        HashSet<Source> regionSet = new HashSet<>();
        try {
            JSONArray jObjMain = new JSONObject(s).getJSONArray("sources");

            // Here we only want to regions and subregions
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jCountry = (JSONObject) jObjMain.get(i);
                String id = jCountry.getString("id");
                String name = jCountry.getString("name");
                String description = jCountry.getString("description");
                String url = jCountry.getString("url");
                String category = jCountry.getString("category");
                String language = jCountry.getString("language");
                String country = jCountry.getString("country");

                MainActivity.nameToId.put(name, id);

                Source source = new Source(id, name, description, url, category, language, country);
                Log.d(TAG, "parseJSON: ");
//                Log.d(TAG, "parseJSON: source" + source);
//                if (category.isEmpty())
//                    continue;

//                if (subRegion.isEmpty())
//                    subRegion = "Unspecified";

                regionSet.add(source);
            }
            return regionSet;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
