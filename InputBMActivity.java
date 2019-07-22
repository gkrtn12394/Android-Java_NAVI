package com.example.gkrtn;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InputBMActivity extends AppCompatActivity implements View.OnTouchListener{

    String name;
    String address;

    TextView nameView;

    Button cancel;
    Button markadd;

    Intent intent;
    ConstraintLayout inputView;

    float sx, ex;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.input_bm);

        cancel = (Button) findViewById(R.id.cancel);
        markadd = (Button) findViewById(R.id.markadd);
        inputView = (ConstraintLayout) findViewById(R.id.inputView);
        nameView = (TextView) findViewById(R.id.name);

        intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");

        nameView.setText("'" + name + "'");

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "즐겨찾기 취소", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        markadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BookMarkActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", address);

                startActivity(intent);
            }
        });

        inputView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            sx = motionEvent.getX();
        } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
            ex = motionEvent.getX();

            if(ex - sx > 50) {
                markadd.callOnClick();
            } else if(ex - sx < -50) {
                cancel.callOnClick();
            }
        }
        return true;
    }
}
