package com.christopherhield.geography;

import android.content.res.Resources;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Random;

public class Utilities {



    public static int getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(192);
        int g = random.nextInt(192);
        int b = random.nextInt(192);
        return Color.rgb(r,g,b);
    }

    public static String codeToName(Resources res, String code, int num, String key) {
        JSONArray ja = readRaw(res, num, key);
        String language = "en";
        try {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject obj = ja.getJSONObject(i);
                if (obj.getString("code").equals(code.toUpperCase()))
                    language = obj.getString("name");
            }
        } catch(Exception e) {e.printStackTrace();}
        return language;
    }

    public static String nameToCode(Resources res, String name, int num, String key) {
        JSONArray ja = readRaw(res, num, key);
        String language = "English";
        try {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject obj = ja.getJSONObject(i);
                if (obj.getString("name").equals(name))
                    language = obj.getString("code");
            }
        } catch(Exception e) {e.printStackTrace();}
        return language.toLowerCase();
    }

    public static JSONArray readRaw(Resources res, int num, String key) {
        InputStream is = res.openRawResource(num);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        JSONArray ja = null;
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
            String jsonString = writer.toString();
            JSONObject obj = new JSONObject(jsonString);
            ja = obj.getJSONArray(key);
        } catch(Exception e) {e.printStackTrace();}

        return ja;
    }


}
