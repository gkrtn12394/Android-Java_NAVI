package com.example.gkrtn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;


public class BmAddDialog extends Dialog {
    private final int ADD_NAME_KEY = 1000, ADD_ADDRESS_KEY = 1001;
    private ArrayList<String> mResult;
    private String mSelectedString;
    TextToSpeech myTTS;

    int focusedIndex = 0;
    AlertDialog ad;

    Context context;

    EditText name;
    EditText des;
    String sName;
    String sDes;

    Button voiceName;
    Button voiceStart;
    Button submit;
    EditDialogListener edl;

    Intent intent;

    public BmAddDialog(Context context) {
        super(context);
        this.context = context;
    }

    public void setDialogListener(EditDialogListener edl) {
        this.edl = edl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bm_add_dialog);

        name = (EditText) findViewById(R.id.name);
        des = (EditText) findViewById(R.id.des);
        submit = (Button) findViewById(R.id.submit);
        voiceName = (Button) findViewById(R.id.VoiceNamebtn);
        voiceStart = (Button) findViewById(R.id.VoiceStartbtn);

        initTTS();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sName = name.getText().toString();
                sDes = des.getText().toString();
                edl.onPositiveClicked(sName, sDes);

                dismiss();
            }
        });

        voiceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "등록할 이름을 말씀해주세요.");

                ((Activity) context).startActivityForResult(intent, ADD_NAME_KEY);
            }
        });

        voiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "등록할 주소를 말씀해주세요.");

                ((Activity) context).startActivityForResult(intent, ADD_ADDRESS_KEY);
            }
        });
    }

    private void initTTS() {
        myTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //사용할 언어를 설정
                    int result = myTTS.setLanguage(Locale.KOREA);
                    //언어 데이터가 없거나 혹은 언어가 지원하지 않으면...
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        myTTS.setSpeechRate(0.78f);
                    }
                }
            }
        });
    }
}
