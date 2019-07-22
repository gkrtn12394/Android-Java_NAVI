package com.example.gkrtn;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private final int DEP_KEY = 1000, DES_KEY = 1001;
    private ArrayList<String> mResult;
    private String mSelectedString;
    TextToSpeech myTTS;

    private static final String FINE_LOCATION_PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION_PERMS = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int PERMS_REQUEST = 3;

    String[] mainMenu = {"출발지 설정", "도착지 설정", "안내 시작", "즐겨찾기"};

    ConstraintLayout mainView;
    Button guideStart;
    Button bookmark;
    Button startPoint;
    Button endPoint;
    TextView s, e;

    String dep, des;
    double curLon, curLat;
    double depX, depY;
    double desX, desY;

    float sx, ex = 0; // 드래그 이벤트용 변수

    int focusedIndex = 0;
    int adFocusedIndex = 0;
    AlertDialog ad;

    FusedLocationProviderClient mFusedLocationClient;
    Geocoder geocoder = new Geocoder(this, Locale.getDefault());

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guideStart = (Button) findViewById(R.id.guideStart);
        bookmark = (Button) findViewById(R.id.bookmark);
        startPoint = (Button) findViewById(R.id.startPoint);
        endPoint = (Button) findViewById(R.id.endPoint);

        mainView = (ConstraintLayout) findViewById(R.id.navi);

        s = (TextView) findViewById(R.id.s);
        e = (TextView) findViewById(R.id.e);

        getPermission();

        initTTS();
        initMFusedClient();

        mainView.setOnTouchListener(this);
        guideStart.setOnClickListener(this);
        bookmark.setOnClickListener(this);
        startPoint.setOnClickListener(this);
        endPoint.setOnClickListener(this);
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
                        myTTS.setSpeechRate(0.78f);
                        myTTS.speak("내비앱을 시작합니다.", myTTS.QUEUE_FLUSH, null, null);
                        myTTS.speak("초기 선택 메뉴" + (focusedIndex + 1) + "번 " + mainMenu[focusedIndex], myTTS.QUEUE_ADD, null, null);
                    }
                }
            }
        });
    }

    private void initMFusedClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500); // two minute interval
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        setLocation(location);

                        List<Address> addList = null;
                        try {
                            addList = geocoder.getFromLocation(curLat, curLon, 10);

                            Address curAddr = addList.get(1);
                            curAddr.getAddressLine(0);

                            Toast.makeText(getApplicationContext(), curAddr.getAddressLine(0), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myTTS != null) {
            myTTS.shutdown();
            myTTS = null;
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.guideStart:
                if (dep != null && des != null) {
                    intent = new Intent(getApplicationContext(), GuideActivity.class);
                    intent.putExtra("dep", dep);
                    intent.putExtra("des", des);
                    intent.putExtra("depX", depX);
                    intent.putExtra("depY", depY);
                    intent.putExtra("desX", desX);
                    intent.putExtra("desY", desY);
                    intent.putExtra("path", getFilesDir().toString());

                    startActivity(intent);

                    s.setText("");
                    e.setText("");
                    des = "";
                    dep = "";
                } else {
                    Toast.makeText(this, "출발지와 도착지를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bookmark:
                intent = new Intent(getApplicationContext(), BookMarkActivity.class);
                startActivity(intent);
                break;
            case R.id.startPoint:
                myTTS.speak("출발지 입력.", myTTS.QUEUE_FLUSH, null, null);

                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "출발지를 말씀해주세요.");

                startActivityForResult(intent, DEP_KEY);
                break;
            case R.id.endPoint:
                myTTS.speak("도착지 입력.", myTTS.QUEUE_FLUSH, null, null);

                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "도착지를 말씀해주세요.");

                startActivityForResult(intent, DES_KEY);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == DEP_KEY || requestCode == DES_KEY)) {
            showSelectDialog(requestCode, data);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                setLocation(location);
            }
        }
    };

    public void setLocation(Location location) {
        curLon = location.getLongitude();    //경도
        curLat = location.getLatitude();         //위도
        float accuracy = location.getAccuracy();        //신뢰도

        Log.e("latitude", curLat + "");
        Log.e("longitude", curLon + "");
        Log.e("accuracy", accuracy + "");
    }

    private void showSelectDialog(final int requestCode, final Intent data) {
        String key = RecognizerIntent.EXTRA_RESULTS;

        mResult = data.getStringArrayListExtra(key);
        final String[] result = new String[mResult.size()];
        mResult.toArray(result);
        adFocusedIndex = 0;

        myTTS.speak((focusedIndex + 1) + "번 " + result[0], myTTS.QUEUE_FLUSH, null, null);

        ad = new AlertDialog.Builder(this)
                .setTitle("선택하세요.")
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                PointHttpConnection conn = null;

                                if (requestCode == DEP_KEY && mSelectedString != null) {
                                    dep = mSelectedString;
                                    Log.e("MainActivity.showSelectDialog", "dep1: " + dep);

                                    try {
                                        conn = new PointHttpConnection(getApplicationContext(), dep, curLat, curLon);

                                        String[] result = conn.execute().get();
                                        dep = result[0];
                                        depX = Double.valueOf(result[2]);
                                        depY = Double.valueOf(result[1]);

                                        Log.e("MainActivity.showSelectDialog", "dep2: " + dep);
                                        s.setText(dep);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    } catch (ExecutionException e1) {
                                        e1.printStackTrace();
                                    }

                                } else if (requestCode == DES_KEY && mSelectedString != null) {
                                    des = mSelectedString;
                                    Log.e("MainActivity.showSelectDialog", "des1: " + des);
                                    try {
                                        conn = new PointHttpConnection(getApplicationContext(), des, curLat, curLon);

                                        String[] result = conn.execute().get();
                                        des = result[0];
                                        desX = Double.valueOf(result[2]);
                                        desY = Double.valueOf(result[1]);

                                        Log.e("MainActivity.showSelectDialog", "des2: " + des);
                                        e.setText(des);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    } catch (ExecutionException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                myTTS.stop();

                                ad.cancel();
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                adFocusedIndex = adFocusedIndex + 1;

                                if (adFocusedIndex >= result.length)
                                    adFocusedIndex = 0;

                                mSelectedString = mResult.get(adFocusedIndex);
                                myTTS.speak((adFocusedIndex + 1) + "번 " + result[adFocusedIndex], myTTS.QUEUE_FLUSH, null, null);
                            }
                        }
                        return true;
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        myTTS.stop();
                    }
                })
                .setSingleChoiceItems(result, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedString = mResult.get(which);
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PointHttpConnection conn = null;

                        if (requestCode == DEP_KEY && mSelectedString != null) {
                            dep = mSelectedString;
                            Log.e("MainActivity.showSelectDialog", "dep1: " + dep);

                            try {
                                conn = new PointHttpConnection(getApplicationContext(), dep, curLat, curLon);

                                String[] result = conn.execute().get();
                                dep = result[0];
                                depX = Double.valueOf(result[2]);
                                depY = Double.valueOf(result[1]);

                                Log.e("MainActivity.showSelectDialog", "dep2: " + dep);
                                s.setText(dep);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            } catch (ExecutionException e1) {
                                e1.printStackTrace();
                            }

                        } else if (requestCode == DES_KEY && mSelectedString != null) {
                            des = mSelectedString;
                            Log.e("MainActivity.showSelectDialog", "des1: " + des);
                            try {
                                conn = new PointHttpConnection(getApplicationContext(), des, curLat, curLon);

                                String[] result = conn.execute().get();
                                des = result[0];
                                desX = Double.valueOf(result[2]);
                                desY = Double.valueOf(result[1]);

                                Log.e("MainActivity.showSelectDialog", "des2: " + des);
                                e.setText(des);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            } catch (ExecutionException e1) {
                                e1.printStackTrace();
                            }
                        }

                        myTTS.stop();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedString = null;
                        myTTS.stop();
                    }
                }).create();
        ad.show();
        mSelectedString = mResult.get(0); // 0번째 원소로 초기값 설정
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            sx = motionEvent.getX();
        } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            ex = motionEvent.getX();

            if(ex - sx > 50) {
                focusedIndex = focusedIndex + 1;

                if (focusedIndex >= mainMenu.length)
                    focusedIndex = 0;

                myTTS.speak((focusedIndex + 1) + "번 " + mainMenu[focusedIndex], myTTS.QUEUE_FLUSH, null, null);
            } else if(ex - sx < -50) {
                if(focusedIndex == 0) startPoint.callOnClick();
                if(focusedIndex == 1) endPoint.callOnClick();
                if(focusedIndex == 2) guideStart.callOnClick();
                if(focusedIndex == 3) bookmark.callOnClick();
            }
        }
        return true;
    }

    public void getPermission() {
        int finePermissionCheck = ContextCompat.checkSelfPermission(this, FINE_LOCATION_PERMS);
        int coursePermissionCheck = ContextCompat.checkSelfPermission(this, COURSE_LOCATION_PERMS);

        if (finePermissionCheck != PackageManager.PERMISSION_GRANTED && coursePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{FINE_LOCATION_PERMS, COURSE_LOCATION_PERMS}, PERMS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION_PERMS) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, COURSE_LOCATION_PERMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, FINE_LOCATION_PERMS)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, COURSE_LOCATION_PERMS)) {
                Toast.makeText(getApplicationContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{FINE_LOCATION_PERMS, COURSE_LOCATION_PERMS}, PERMS_REQUEST);
            }
        }
    }
}
