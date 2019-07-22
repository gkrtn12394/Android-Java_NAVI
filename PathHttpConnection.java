package com.example.gkrtn;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.Response;
import com.skt.Tmap.TMapTapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class PathHttpConnection extends AsyncTask<String, String, String> {

    String dep, des;
    double depX, depY;
    double desX, desY;
    String strUrl;
    String urlParams;
    String result;

    JSONObject json;
    String[][] resultArr;
    int count;
    String sCount;

    Context context;

    HttpURLConnection conn = null;

    public PathHttpConnection(Context context, String dep, String des, double depX, double depY, double desX, double desY, String[][] result) {
        this.context = context;
        this.dep = dep;
        this.des = des;
        this.depX = depX;
        this.depY = depY;
        this.desX = desX;
        this.desY = desY;
        resultArr = result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        try {
            dep = URLEncoder.encode(dep, "UTF-8");
            des = URLEncoder.encode(des, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        strUrl = "https://api2.sktelecom.com/tmap/routes/pedestrian?"; //탐색하고 싶은 URL이다.
        //urlParams = "version=1.55&startX=128.3924686&startY=36.1456008&endX=128.3924686&endY=36.1456008&" +
        //"startName=금오공과대학교&endName=금오공과대학교&appKey=1f296ad5-405e-4c4e-a81c-ead280a527fe";

        urlParams = "appKey=1f296ad5-405e-4c4e-a81c-ead280a527fe&startX=" + depX + "&startY=" + depY + "&endX=" + desX + "&endY=" + desY + "&startName=" + dep + "&endName=" + des;
        //urlParams = "appKey=1f296ad5-405e-4c4e-a81c-ead280a527fe&startX=126.9823439963945&startY=37.56461982743129&angle=1&speed=60&endPoiId=334852&endRpFlag=8&endX=126.98031634883303&endY=37.57007473965354&passList=126.98506595175428,37.56674182109044,334857,16&reqCoordType=WGS84GEO&gpsTime=15000&startName=%EC%B6%9C%EB%B0%9C&endName=%EB%B3%B8%EC%82%AC&searchOption=0&resCoordType=WGS84GEO";

        Log.e("urlParams: ", urlParams);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL Url = new URL(strUrl + urlParams); // URL화 한다.
            conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.

            int response = conn.getResponseCode();
            Log.e("e", response + "");

            if (response == HttpURLConnection.HTTP_OK) {

                InputStream is = conn.getInputStream(); //input스트림 개방

                StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                String str = builder.toString();

                json = new JSONObject(str);

                JSONArray features = (JSONArray) json.get("features");

                for (int i = 0; i < features.length(); i++) {
                    JSONObject temp = features.getJSONObject(i);
                    resultArr[i][0] = temp.getJSONObject("properties").getString("description");

                    JSONObject geometry = temp.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    resultArr[i][1] = Double.toString(coordinates.getDouble(0));
                    resultArr[i][2] = Double.toString(coordinates.getDouble(1));

                    Log.e("description : ", resultArr[i][0] + " 위도 : " + resultArr[i][1] + " 경도 : " + resultArr[i][2]);
                    count = count + 1;
                }

                sCount = Integer.toString(count);

                is.close();
            }
            else{
                sCount = "-1";
            }
            return sCount;

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

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
