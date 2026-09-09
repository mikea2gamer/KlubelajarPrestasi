package com.klubelajar.prestasi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "klubelajar.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, name TEXT, role TEXT, class_name TEXT)");
        db.execSQL("CREATE TABLE courses(id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, name TEXT)");
        db.execSQL("CREATE TABLE grades(id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, subject TEXT, score TEXT)");
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, message TEXT, created_at TEXT)");
        db.execSQL("CREATE TABLE meetings(id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, title TEXT, date TEXT, link TEXT)");
        db.execSQL("CREATE TABLE schedules(id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER, subject TEXT, date TEXT, time TEXT, teacher TEXT)");

        ContentValues a = new ContentValues();
        a.put("username","admin"); a.put("password","admin123"); a.put("name","Administrator"); a.put("role","admin"); a.put("class_name","Admin");
        db.insert("users", null, a);

        ContentValues u = new ContentValues();
        u.put("username","siswa"); u.put("password","siswa123"); u.put("name","Siswa Demo"); u.put("role","user"); u.put("class_name","PPLG XI");
        long studentId = db.insert("users", null, u);

        addCourse(db, studentId, "Matematika");
        addCourse(db, studentId, "Bahasa Inggris");
        addCourse(db, studentId, "Pemrograman Web");

        addGrade(db, studentId, "Matematika", "88");
        addGrade(db, studentId, "Bahasa Inggris", "91");
        addGrade(db, studentId, "Pemrograman Web", "95");

        addMessage(db, studentId, "Selamat datang di Klubelajar Prestasi! Jangan lupa mengikuti jadwal bimbingan.", "09-09-2026");

        addMeeting(db, studentId, "Bimbingan Pemrograman Web", "10-09-2026 16:00", "https://zoom.us/");
        addSchedule(db, studentId, "Pemrograman Web", "10-09-2026", "16:00", "Kak Mentor");
    }

    private void addCourse(SQLiteDatabase db,long sid,String name){ ContentValues v=new ContentValues(); v.put("student_id",sid); v.put("name",name); db.insert("courses",null,v); }
    private void addGrade(SQLiteDatabase db,long sid,String subject,String score){ ContentValues v=new ContentValues(); v.put("student_id",sid); v.put("subject",subject); v.put("score",score); db.insert("grades",null,v); }
    private void addMessage(SQLiteDatabase db,long sid,String msg,String date){ ContentValues v=new ContentValues(); v.put("student_id",sid); v.put("message",msg); v.put("created_at",date); db.insert("messages",null,v); }
    private void addMeeting(SQLiteDatabase db,long sid,String title,String date,String link){ ContentValues v=new ContentValues(); v.put("student_id",sid); v.put("title",title); v.put("date",date); v.put("link",link); db.insert("meetings",null,v); }
    private void addSchedule(SQLiteDatabase db,long sid,String subject,String date,String time,String teacher){ ContentValues v=new ContentValues(); v.put("student_id",sid); v.put("subject",subject); v.put("date",date); v.put("time",time); v.put("teacher",teacher); db.insert("schedules",null,v); }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { }
}
