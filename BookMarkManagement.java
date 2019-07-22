package com.example.gkrtn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BookMarkManagement {
    private ArrayList<BookMark> bookMarks;

    public BookMarkManagement() { bookMarks = new ArrayList<>(); }

    public void insertBookMark(String name, String des) {
        // if(!isExist(name)) return -1;
        BookMark newBookMark = new BookMark(name, des);

        bookMarks.add(newBookMark);
    }

    public ArrayList<BookMark> getBookMarkList() { return bookMarks; }

    public boolean editBookMark(ArrayList<BookMark> bmList, int position, String newName, String newDes) {  // 포지션으로 수정하는거라 찾기 필요x
        BookMark editingBM = bookMarks.get(position);

        editingBM.setName(newName);
        editingBM.setDes(newDes);

        editingBM = bmList.get(position);

        editingBM.setName(newName);
        editingBM.setDes(newDes);

        return true;
    }

    public void deleteBookMark(int positon) { bookMarks.remove(positon); } // 포지션으로 삭제하는거라 찾기 필요x

    public void loadBookMarks(String path) {
        try {
            File file = new File(path + "test.txt");

            if(file == null) {
                file.createNewFile();
            }

            BufferedReader br;
            br = new BufferedReader(new FileReader(path + "test.txt"));
            String str = null;
            while (((str = br.readLine()) != null)) {
                int p = str.indexOf('/');

                String name = str.substring(0, p);
                String des = str.substring(p+1, str.length());

                BookMark bm = new BookMark(name, des);
                bookMarks.add(bm);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveBookMarks(String path) {
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "test.txt", false));

            for(int i = 0; i < bookMarks.size(); i++) {
                BookMark curBM = bookMarks.get(i);
                bw.write(curBM.getName() + "/" + curBM.getDes() + "\n");
            }

            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
