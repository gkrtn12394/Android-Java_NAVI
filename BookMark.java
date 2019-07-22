package com.example.gkrtn;

public class BookMark {
    private String name;
    private String des;

    public BookMark(String name, String des) { this.name = name; this.des = des; }

    public String getName() { return name; }
    public String getDes() { return des; }
    public void setName(String newName) { name = newName; }
    public void setDes(String newDes){ des = newDes; }
}
