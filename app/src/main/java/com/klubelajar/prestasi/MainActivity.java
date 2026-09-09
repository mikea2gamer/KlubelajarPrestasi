package com.klubelajar.prestasi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {
    DBHelper db; int studentId;
    TextView welcome, content;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        studentId = getSharedPreferences("session",0).getInt("user_id",-1);
        welcome=findViewById(R.id.welcome); content=findViewById(R.id.content);
        welcome.setText("Halo, " + getSharedPreferences("session",0).getString("name","Siswa") + "!");

        findViewById(R.id.scheduleBtn).setOnClickListener(v -> showSchedules());
        findViewById(R.id.courseBtn).setOnClickListener(v -> showCourses());
        findViewById(R.id.gradeBtn).setOnClickListener(v -> showGrades());
        findViewById(R.id.messageBtn).setOnClickListener(v -> showMessages());
        findViewById(R.id.zoomBtn).setOnClickListener(v -> showMeetings());
        findViewById(R.id.profileBtn).setOnClickListener(v -> showProfile());
        findViewById(R.id.logoutBtn).setOnClickListener(v -> logout());
    }

    void dialog(String title, String text) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("Tutup",null).show();
    }
    String rows(String sql,String[] args,String format) {
        Cursor c=db.getReadableDatabase().rawQuery(sql,args); StringBuilder s=new StringBuilder();
        while(c.moveToNext()) {
            for(int i=0;i<c.getColumnCount();i++) {
                if(i>0)s.append(" • ");
                s.append(c.getString(i));
            }
            s.append("\n");
        }
        c.close(); return s.length()==0 ? "Belum ada data." : s.toString();
    }
    void showSchedules(){ dialog(" Jadwal Bimbingan", rows("SELECT subject,date,time,teacher FROM schedules WHERE student_id=? ORDER BY date,time",new String[]{""+studentId},"")); }
    void showCourses(){ dialog(" Pelajaran", rows("SELECT name FROM courses WHERE student_id=? ORDER BY name",new String[]{""+studentId},"")); }
    void showGrades(){ dialog(" Nilai", rows("SELECT subject,score FROM grades WHERE student_id=? ORDER BY subject",new String[]{""+studentId},"")); }
    void showMessages(){ dialog(" Pesan Admin", rows("SELECT created_at,message FROM messages WHERE student_id=? ORDER BY id DESC",new String[]{""+studentId},"")); }
    void showMeetings(){
        SQLiteDatabase d=db.getReadableDatabase();
        Cursor c=d.rawQuery("SELECT title,date,link FROM meetings WHERE student_id=? ORDER BY id DESC",new String[]{""+studentId});
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(30,10,30,10);
        while(c.moveToNext()){
            String title=c.getString(0), date=c.getString(1), link=c.getString(2);
            TextView t=new TextView(this); t.setText("🎥 "+title+"\n"+date+"\n"+link); t.setTextSize(16); t.setPadding(0,12,0,12); box.addView(t);
            Button open=new Button(this); open.setText("Buka Zoom"); open.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));}catch(Exception e){Toast.makeText(this,"Link tidak dapat dibuka",Toast.LENGTH_SHORT).show();}}); box.addView(open);
        }
        c.close();
        if(box.getChildCount()==0){TextView t=new TextView(this);t.setText("Belum ada meeting Zoom.");box.addView(t);}
        new AlertDialog.Builder(this).setTitle("🎥 Zoom Meeting").setView(box).setPositiveButton("Tutup",null).show();
    }
    void showProfile(){
        SQLiteDatabase d=db.getReadableDatabase(); Cursor c=d.rawQuery("SELECT name,username,class_name FROM users WHERE id=?",new String[]{""+studentId});
        if(c.moveToFirst()) dialog("👤 Profil","Nama: "+c.getString(0)+"\nUsername: "+c.getString(1)+"\nKelas: "+c.getString(2)); c.close();
    }
    void logout(){ getSharedPreferences("session",0).edit().clear().apply(); startActivity(new Intent(this,LoginActivity.class)); finish(); }
}
