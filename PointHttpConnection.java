package com.example.gkrtn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.skt.Tmap.TMapTapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class PointHttpConnection extends AsyncTask<String, String, String[]> {

    String strUrl;
    String urlParams;
    String[] result = new String[3];
    String p;

    double curLat;
    double curLon;

    TMapTapi tMapTapi;

    JSONObject json;

    Context context;

    HttpURLConnection conn = null;

    public PointHttpConnection(Context context, String p, double curLat, double curLon) {
        this.context = context;
        this.curLat = curLat;
        this.curLon = curLon;
        this.p = p;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        try {
            p = URLEncoder.encode(p, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        strUrl = "https://api2.sktelecom.com/tmap/pois?";
        urlParams = "appKey=1f296ad5-405e-4c4e-a81c-ead280a527fe&version=1.55&searchKeyword=" + p + "&radius=1&centerLon=" + curLon + "&centerLat=" + curLat;

        Log.e("PointHttpConnection.onPreExecute", "urlParams: " + urlParams);
    }

    @Override
    protected String[] doInBackground(String...params) {
        InputStream is = null;

        try {
            URL Url = new URL(strUrl + urlParams); // URL화 한다.
            conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.

            int response = conn.getResponseCode();
            Log.e("e", response + "");

            if (response == HttpURLConnection.HTTP_OK) {

                is = conn.getInputStream(); //input스트림 개방

                StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅
                String line;

                while ((line = reader.readLine()) != null) {
                    Log.e("PointHttpConnection.doInBackground", line);
                    builder.append(line + "\n");
                }

                String str = builder.toString();

                json = new JSONObject(str);

                JSONObject searchInfo = (JSONObject) json.get("searchPoiInfo");
                JSONObject pois = (JSONObject) searchInfo.get("pois");
                JSONArray poi = (JSONArray) pois.get("poi");

                JSONObject first = (JSONObject) poi.get(0);

                Log.e("first: ", first.toString());

                result[0] = first.get("name").toString();
                result[1] = first.get("noorLat").toString();
                result[2] = first.get("noorLon").toString();

                Log.e("PointHttpConnection.doInBackground", "result[0]: " + result[0] + "result[1]: " + result[1] + "result[2]: " + result[2]);
            }

            is.close();

        } catch (MalformedURLException | ProtocolException exception) {
            exception.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
            conn = null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
    }
}
