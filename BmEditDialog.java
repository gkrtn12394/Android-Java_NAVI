package com.example.gkrtn;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class BmEditDialog extends Dialog {
    private final int EDIT_NAME_KEY = 1002, EDIT_ADDRESS_KEY = 1003;

    EditText newName;
    EditText newDes;
    String sNewName;
    String sNewDes;

    Button newNameBtn;
    Button newDesBtn;
    Button submit;

    Context context;

    Intent intent;
    EditDialogListener edl;

    public BmEditDialog(Context context) {
        super(context);
        this.context = context;
    }

    public void setDialogListener(EditDialogListener edl) { this.edl = edl; }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bm_edit_dialog);

        newName = (EditText) findViewById(R.id.newName);
        newDes = (EditText) findViewById(R.id.newDes);
        newNameBtn = (Button) findViewById(R.id.VoiceNewNamebtn);
        newDesBtn = (Button) findViewById(R.id.VoiceNewStartbtn);

        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sNewName = newName.getText().toString();
                sNewDes = newDes.getText().toString();
                edl.onPositiveClicked(sNewName, sNewDes);

                dismiss();
            }
        });

        newNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "수정할 이름을 말씀해주세요.");

                ((Activity) context).startActivityForResult(intent, EDIT_NAME_KEY);
            }
        });

        newDesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "수정할 주소을 말씀해주세요.");

                ((Activity) context).startActivityForResult(intent, EDIT_ADDRESS_KEY);
            }
        });
    }
}
