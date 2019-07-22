package com.example.gkrtn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class GuideActivity extends AppCompatActivity implements View.OnTouchListener {

    ConstraintLayout guideView;
    Button cancel;
    Button curPo;
    TextView state;

    TextToSpeech myTTS;

    Intent intent;
    String curPosition;
    String dep;
    String des;
    String path;
    double depX, depY;
    double desX, desY;

    float sx, ex;

    String[][] locations;

    double longitude;
    double latitude;
    int range = 1;

    FusedLocationProviderClient mFusedLocationClient;
    Geocoder geocoder = new Geocoder(this, Locale.getDefault());

    PositionEqualListener positionEqualListener;
    int curGuide = 0;
    int guideCount;

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mFusedLocationClient = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initMFusedClient();
    }

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.guide);

        guideView = (ConstraintLayout) findViewById(R.id.guideView);
        cancel = (Button) findViewById(R.id.cancel);
        curPo = (Button) findViewById(R.id.curPo);
        state = (TextView) findViewById(R.id.state);

        intent = getIntent();
        dep = intent.getStringExtra("dep");
        des = intent.getStringExtra("des");
        depX = intent.getDoubleExtra("depX", 0);
        depY = intent.getDoubleExtra("depY", 0);
        desX = intent.getDoubleExtra("desX", 0);
        desY = intent.getDoubleExtra("desY", 0);
        path = intent.getStringExtra("path");

        initTTS();
        initMFusedClient();

        getPathData();

        this.setOnPositionEqualListener(new PositionEqualListener() {
            @Override
            public void onPositionEqual() {
                if(curGuide < guideCount)
                    myTTS.speak(locations[curGuide++][0], myTTS.QUEUE_FLUSH, null, null);
                if (curGuide == guideCount) {
                    Intent intent = new Intent(getApplicationContext(), InputBMActivity.class);
                    intent.putExtra("name", des);
                    intent.putExtra("address", curPosition);

                    Log.e("guide", "name : " + des + " addr : " + curPosition);

                    startActivity(intent);

                    finish();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak("경로안내를 취소합니다.", myTTS.QUEUE_FLUSH, null, null);
                Toast.makeText(getApplicationContext(), "안내 취소", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        curPo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), curPosition, Toast.LENGTH_SHORT).show();
                myTTS.speak("현재위치 : " + curPosition, myTTS.QUEUE_FLUSH, null, null);
            }
        });

        guideView.setOnTouchListener(this);
    }

    private void initTTS() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = myTTS.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(), "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        myTTS.setSpeechRate(0.8f);
                        myTTS.speak(dep + "에서 " + des + "까지의 경로 안내를 시작합니다.", myTTS.QUEUE_FLUSH, null, null);
                    }
                }
            }
        });
    }

    private void getPathData() {
        String resultCount = "";
        locations = new String[50][3];
        PathHttpConnection conn = null;

        try {
            conn = new PathHttpConnection(getApplicationContext(), dep, des, depX, depY, desX, desY, locations);
            resultCount = conn.execute().get();

            guideCount = Integer.parseInt(resultCount);

            if (guideCount == -1) {
                Toast.makeText(getApplicationContext(), "경로 검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                this.finish();
            }

            Toast.makeText(getApplicationContext(), conn.result, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void initMFusedClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // two minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        List<Address> addList = null;
                        try {
                            setLocation(location);

                            addList = geocoder.getFromLocation(latitude, longitude, 10);

                            Address curAddr = addList.get(0);
                            curPosition = curAddr.getAddressLine(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                try {
                    Location location = locationList.get(locationList.size() - 1);

                    setLocation(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void setLocation(Location location) throws IOException {
        longitude = location.getLongitude();    //경도
        latitude = location.getLatitude();         //위도
        float accuracy = location.getAccuracy();        //신뢰도

        int nextLon = (int)(Double.parseDouble(locations[curGuide][1]) * 10000);
        int nextLat = (int)(Double.parseDouble(locations[curGuide][2]) * 10000);

        int curLon = (int)(longitude * 10000);
        int curLat = (int)(latitude * 10000);

        if(nextLon >= curLon - range && nextLon <= curLon + range && nextLat >= curLat - range && nextLat <= curLat + range) {
            positionEqualListener.onPositionEqual();
        }

        List<Address> addList = geocoder.getFromLocation(latitude, longitude, 10);

        Address curAddr = addList.get(1);
        curAddr.getAddressLine(0);

        curPosition = curAddr.getAddressLine(0);

        state.setText("경도" + longitude + "위도" + latitude);
        Log.e("latitude", latitude + "");
        Log.e("longitude", longitude + "");
        Log.e("accuracy", accuracy + "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mFusedLocationClient = null;
        myTTS.stop();
        myTTS.shutdown();

        myTTS = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            sx = motionEvent.getX();
        } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            ex = motionEvent.getX();

            if(ex - sx > 50) {
                curPo.callOnClick();
            } else if(ex - sx < -50) {
                cancel.callOnClick();
            }
        }
        return true;
    }

    public interface PositionEqualListener {
        void onPositionEqual();
    }

    public void setOnPositionEqualListener(PositionEqualListener positionEqualListener) {
        this.positionEqualListener = positionEqualListener;
    }
}

