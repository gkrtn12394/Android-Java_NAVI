package com.example.gkrtn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class BookMarkActivity extends AppCompatActivity implements View.OnTouchListener {
    private final int ADD_NAME_KEY = 1000, ADD_ADDRESS_KEY = 1001;
    private final int EDIT_NAME_KEY = 1002, EDIT_ADDRESS_KEY = 1003;

    ConstraintLayout bmView;
    Button add;
    Button edit;
    Button del;
    ListView bmListView;

    private ArrayList<String> mResult;
    private String mSelectedString;
    TextToSpeech myTTS;

    int focusedIndex = 0;
    int focusedItem = -1;
    int menuIndex = -1;
    int editFocus = -1;
    int addFocus = -1;

    AlertDialog ad;
    BmAddDialog addDialog;
    BmEditDialog editDialog;

    String[] bookMarkMenu = {"수정", "삭제"};

    BookMarkManagement bmk;
    ArrayList<BookMark> bmList;
    ArrayList<String> names;
    ArrayList<String> dess;

    float sx, ex;

    MyListAdapter adapter;

    Intent intent;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.bookmark);

        initTTS();

        intent = getIntent();
        String name = intent.getStringExtra("name");
        String address = intent.getStringExtra("address");

        bmk = new BookMarkManagement();
        bmList = new ArrayList<>();

        // 파일 로딩
        bmk.loadBookMarks(getFilesDir().toString());
        bmList = bmk.getBookMarkList();

        names = new ArrayList<>();
        dess = new ArrayList<>();

        for (int i = 0; i < bmList.size(); i++) {
            BookMark curBM = bmList.get(i);

            names.add(curBM.getName());
            dess.add(curBM.getDes());
        }

        bmListView = (ListView) findViewById(R.id.bmList);
        adapter = new MyListAdapter(this, R.layout.bm_list, names, dess);
        bmListView.setAdapter(adapter);

        if (name != null && address != null) {
            Toast.makeText(getApplicationContext(), "name : " + name + "address : " + address, Toast.LENGTH_SHORT).show();

            bmk.insertBookMark(name, address);

            names.add(name);
            dess.add(address);

            adapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "등록 성공", Toast.LENGTH_SHORT).show();

            finish();
        }

        bmView = (ConstraintLayout) findViewById(R.id.bmView);
        add = (Button) findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDialog = new BmAddDialog(BookMarkActivity.this);
                addDialog.setTitle("즐겨찾기 등록"); // 다이얼로그 제목.
                addDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                if(addFocus == 0) addDialog.voiceName.callOnClick();
                                else if (addFocus == 1) addDialog.voiceStart.callOnClick();
                            } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                addFocus = addFocus + 1;

                                if(addFocus == 2) {
                                    addDialog.submit.callOnClick();
                                    addFocus = -1;
                                }

                                if(addFocus == 0) myTTS.speak("이름 입력", myTTS.QUEUE_FLUSH, null, null);
                                if(addFocus == 1) myTTS.speak("주소 입력", myTTS.QUEUE_FLUSH, null, null);
                            }
                        }
                        return true;
                    }
                });
                addDialog.setDialogListener(new EditDialogListener() {
                    @Override
                    public void onPositiveClicked(String name, String des) {
                        bmk.insertBookMark(name, des);

                        names.add(name);
                        dess.add(des);

                        adapter.notifyDataSetChanged();

                        Toast.makeText(getApplicationContext(), "등록 성공", Toast.LENGTH_SHORT).show();
                    }
                });
                addDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        addFocus = -1;
                    }
                });
                addDialog.show();
            }
        });

        bmView.setOnTouchListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == ADD_NAME_KEY || requestCode == ADD_ADDRESS_KEY || requestCode == EDIT_NAME_KEY || requestCode == EDIT_ADDRESS_KEY)) {
            showSelectDialog(requestCode, data);
        }
    }

    private void showSelectDialog(final int requestCode, final Intent data) {
        String key = RecognizerIntent.EXTRA_RESULTS;

        mResult = data.getStringArrayListExtra(key);
        final String[] result = new String[mResult.size()];
        mResult.toArray(result);

        ad = new AlertDialog.Builder(this)
                .setTitle("선택하세요.")
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        ListView listView = ad.getListView();

                        if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                if (requestCode == ADD_NAME_KEY) {
                                    addDialog.name.setText(mSelectedString);
                                } else if (requestCode == ADD_ADDRESS_KEY) {
                                    addDialog.des.setText(mSelectedString);
                                } else if (requestCode == EDIT_NAME_KEY) {
                                    editDialog.newName.setText(mSelectedString);
                                } else if (requestCode == EDIT_ADDRESS_KEY) {
                                    editDialog.newDes.setText(mSelectedString);
                                }
                                myTTS.stop();
                                ad.cancel();
                            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                focusedIndex = focusedIndex + 1;

                                if (focusedIndex >= result.length)
                                    focusedIndex = 0;

                                listView.setSelection(focusedIndex);
                                mSelectedString = mResult.get(focusedIndex);
                                myTTS.speak((focusedIndex + 1) + "번 " + result[focusedIndex], myTTS.QUEUE_FLUSH, null, null);
                            }
                        }
                        return true;
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
                        // 리스트 업데이트
                        if (requestCode == ADD_NAME_KEY) {
                            addDialog.name.setText(mSelectedString);
                        } else if (requestCode == ADD_ADDRESS_KEY) {
                            addDialog.des.setText(mSelectedString);
                        } else if (requestCode == EDIT_NAME_KEY) {
                            editDialog.newName.setText(mSelectedString);
                        } else if (requestCode == EDIT_ADDRESS_KEY) {
                            editDialog.newDes.setText(mSelectedString);
                        }
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

        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                myTTS.stop();
            }
        });
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
                        myTTS.speak("즐겨찾기 입니다.", myTTS.QUEUE_FLUSH, null, null);
                        myTTS.speak("1번 즐겨찾기 추가", myTTS.QUEUE_ADD, null, null);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        //파일 저장
        bmk.saveBookMarks(getFilesDir().toString());
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            sx = motionEvent.getX();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            ex = motionEvent.getX();

            if (ex - sx > 50) {
                if (focusedItem == -1) { // 리스트가 선택되지 않았으면
                    focusedIndex = focusedIndex + 1;

                    if (focusedIndex >= bmList.size() + 1)
                        focusedIndex = 0;

                    if (focusedIndex == 0) {
                        myTTS.speak((focusedIndex + 1) + "번 " + "즐겨찾기 추가", myTTS.QUEUE_FLUSH, null, null);
                    } else {
                        myTTS.speak((focusedIndex + 1) + "번 " + names.get(focusedIndex - 1), myTTS.QUEUE_FLUSH, null, null);
                    }
                } else {
                    menuIndex = menuIndex + 1;

                    if (menuIndex >= bookMarkMenu.length)
                        menuIndex = 0;

                    myTTS.speak(names.get(focusedIndex - 1) + bookMarkMenu[menuIndex], myTTS.QUEUE_FLUSH, null, null);
                }
            } else if (ex - sx < -50) {
                if (focusedItem == -1) { // 리스트가 선택되지 않았으면
                    if (focusedIndex > 0) { // 즐겨찾기 추가가 아니면
                        focusedItem = focusedIndex;

                        myTTS.speak(names.get(focusedIndex - 1) + "선택.", myTTS.QUEUE_FLUSH, null, null);
                    } else {
                        add.callOnClick();
                    }
                } else {
                    if (menuIndex == 0) { // 선택된 아이템 수정
                        Button editButton = adapter.lists.get(focusedItem - 1).findViewById(R.id.edit);
                        editButton.callOnClick();
                    }
                    else if (menuIndex == 1) { // 선택된 아이템 삭제
                        Button delButton = adapter.lists.get(focusedItem - 1).findViewById(R.id.del);
                        delButton.callOnClick();
                    }
                }
            }
        }
        return true;
    }

    private class MyListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> names;
        ArrayList<String> dess;
        ArrayList<View> lists;

        public MyListAdapter(Context context, int layoutToBeInflated, ArrayList<String> name, ArrayList<String> des) {
            super(context, layoutToBeInflated, name);
            this.context = context;
            this.names = name;
            this.dess = des;
            lists = new ArrayList<>();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View row = inflater.inflate(R.layout.bm_list, null);

            TextView name = (TextView) row.findViewById(R.id.name);
            TextView des = (TextView) row.findViewById(R.id.address);

            EditText newName = (EditText) row.findViewById(R.id.newName);
            EditText newDes = (EditText) row.findViewById(R.id.newDes);

            edit = (Button) row.findViewById(R.id.edit);
            del = (Button) row.findViewById(R.id.del);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editDialog = new BmEditDialog(BookMarkActivity.this);
                    editDialog.setTitle("즐겨찾기 수정"); // 다이얼로그 제목.
                    editDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                    if(editFocus == 0) editDialog.newNameBtn.callOnClick();
                                    else if (editFocus == 1) editDialog.newDesBtn.callOnClick();
                                } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                    editFocus = editFocus + 1;
                                    if(editFocus == 2) {
                                        editDialog.submit.callOnClick();
                                    }

                                    if(editFocus == 0) myTTS.speak("새 이름 입력", myTTS.QUEUE_FLUSH, null, null);
                                    if(editFocus == 1) myTTS.speak("새 주소 입력", myTTS.QUEUE_FLUSH, null, null);
                                }
                            }
                            return true;
                        }
                    });
                    editDialog.setDialogListener(new EditDialogListener() {
                        @Override
                        public void onPositiveClicked(String name, String des) {
                            boolean result = bmk.editBookMark(bmList, position, name, des);

                            if (result) {
                                names.set(position, name); // 배열에 저장
                                dess.set(position, des);

                                adapter.notifyDataSetChanged();

                                Toast.makeText(getApplicationContext(), "수정 성공", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "수정 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    editDialog.show();

                    editDialog.newName.setText(names.get(position)); // 뷰에 반영
                    editDialog.newDes.setText(dess.get(position));
                }
            });

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder b = new AlertDialog.Builder(BookMarkActivity.this);
                    b.setTitle("삭제 확인");
                    b.setMessage("삭제하시겠습니까?");
                    b.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                                    bmk.deleteBookMark(position);

                                    names.remove(position);
                                    dess.remove(position);

                                    adapter.notifyDataSetChanged();

                                    Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                                }
                            }
                            return true;
                        }
                    });
                    b.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bmk.deleteBookMark(position);

                            names.remove(position);
                            dess.remove(position);

                            adapter.notifyDataSetChanged();

                            Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                        }
                    });
                    b.show();
                    myTTS.speak("삭제하시겠습니까?", myTTS.QUEUE_FLUSH, null, null);
                }
            });

            name.setText(names.get(position));
            des.setText(dess.get(position));

            lists.add(row);

            return row;
        }
    }
}
