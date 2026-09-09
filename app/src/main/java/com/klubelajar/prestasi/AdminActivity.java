package com.klubelajar.prestasi;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminActivity extends Activity {
    DBHelper db;
    int selectedStudentId=-1;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_admin);
        db=new DBHelper(this);
        TextView status=findViewById(R.id.adminStatus);
        status.setText("Login sebagai Admin • Data tersimpan di database lokal SQLite");

        findViewById(R.id.studentsBtn).setOnClickListener(v->manageStudents());
        findViewById(R.id.coursesBtn).setOnClickListener(v->manageCourses());
        findViewById(R.id.gradesBtn).setOnClickListener(v->manageGrades());
        findViewById(R.id.messagesBtn).setOnClickListener(v->sendMessage());
        findViewById(R.id.zoomAdminBtn).setOnClickListener(v->sendZoom());
        findViewById(R.id.scheduleAdminBtn).setOnClickListener(v->manageSchedule());
        findViewById(R.id.logoutAdminBtn).setOnClickListener(v->{getSharedPreferences("session",0).edit().clear().apply();startActivity(new Intent(this,LoginActivity.class));finish();});
    }

    long studentPick(){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,class_name FROM users WHERE role='user' ORDER BY name",null);
        if(!c.moveToFirst()){c.close(); Toast.makeText(this,"Belum ada siswa.",Toast.LENGTH_SHORT).show();return -1;}
        java.util.ArrayList<String> names=new java.util.ArrayList<>(); java.util.ArrayList<Integer> ids=new java.util.ArrayList<>();
        do{ids.add(c.getInt(0));names.add(c.getString(1)+" ("+c.getString(2)+")");}while(c.moveToNext()); c.close();
        final int[] chosen={0};
        new AlertDialog.Builder(this).setTitle("Pilih Siswa").setSingleChoiceItems(names.toArray(new String[0]),0,(di,w)->{chosen[0]=w;})
            .setPositiveButton("Pilih",(di,w)->{selectedStudentId=ids.get(chosen[0]);}).setNegativeButton("Batal",null).show();
        return 0;
    }

    void manageStudents(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(30,10,30,10);
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,username,class_name FROM users WHERE role='user' ORDER BY name",null);
        while(c.moveToNext()){
            int id=c.getInt(0); String text=c.getString(1)+"\nUsername: "+c.getString(2)+" • Kelas: "+c.getString(3);
            Button b=new Button(this);b.setText(text);b.setOnClickListener(v->editStudent(id));box.addView(b);
        } c.close();
        Button add=new Button(this);add.setText("+ Tambah Siswa");add.setOnClickListener(v->addStudent());box.addView(add);
        new AlertDialog.Builder(this).setTitle("👥 Data Siswa").setView(box).setPositiveButton("Tutup",null).show();
    }

    EditText field(String hint){EditText e=new EditText(this);e.setHint(hint);e.setPadding(10,5,10,5);return e;}
    void addStudent(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        EditText n=field("Nama");n.setMinLines(2);EditText u=field("Username");u.setMinLines(2);EditText p=field("Password");p.setMinLines(2);EditText k=field("Kelas");k.setMinLines(2);
        box.addView(n);box.addView(u);box.addView(p);box.addView(k);
        new AlertDialog.Builder(this).setTitle("Tambah Siswa").setView(box).setPositiveButton("Simpan",(d,w)->{
            ContentValues v=new ContentValues();v.put("name",n.getText().toString());v.put("username",u.getText().toString());v.put("password",p.getText().toString());v.put("class_name",k.getText().toString());v.put("role","user");
            db.getWritableDatabase().insert("users",null,v);Toast.makeText(this,"Siswa ditambahkan",Toast.LENGTH_SHORT).show();
        }).setNegativeButton("Batal",null).show();
    }
    void editStudent(int id){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT name,username,class_name FROM users WHERE id=?",new String[]{""+id});
        if(!c.moveToFirst()){c.close();return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        EditText n=field("Nama");n.setMinLines(2);n.setText(c.getString(0));EditText u=field("Username");u.setMinLines(2);u.setText(c.getString(1));EditText k=field("Kelas");k.setMinLines(2);k.setText(c.getString(2));box.addView(n);box.addView(u);box.addView(k);c.close();
        new AlertDialog.Builder(this).setTitle("Edit Siswa").setView(box).setPositiveButton("Simpan",(d,w)->{
            ContentValues v=new ContentValues();v.put("name",n.getText().toString());v.put("username",u.getText().toString());v.put("class_name",k.getText().toString());
            db.getWritableDatabase().update("users",v,"id=?",new String[]{""+id});Toast.makeText(this,"Data siswa diperbarui",Toast.LENGTH_SHORT).show();
        }).setNegativeButton("Batal",null).show();
    }

    void chooseThen(Runnable action){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM users WHERE role='user' ORDER BY name",null);
        java.util.ArrayList<String> n=new java.util.ArrayList<>();java.util.ArrayList<Integer> ids=new java.util.ArrayList<>();
        while(c.moveToNext()){ids.add(c.getInt(0));n.add(c.getString(1));}c.close();
        if(n.isEmpty()){Toast.makeText(this,"Belum ada siswa.",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("Pilih Siswa").setItems(n.toArray(new String[0]),(d,w)->{selectedStudentId=ids.get(w);action.run();}).show();
    }

    void manageCourses(){
        chooseThen(()->{
            LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
            Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name FROM courses WHERE student_id=? ORDER BY name",new String[]{""+selectedStudentId});
            while(c.moveToNext()){int id=c.getInt(0);Button b=new Button(this);b.setText("Edit/Hapus: "+c.getString(1));b.setOnClickListener(v->editCourse(id));box.addView(b);}c.close();
            Button add=new Button(this);add.setText("+ Tambah Pelajaran");add.setOnClickListener(v->addCourse());box.addView(add);
            new AlertDialog.Builder(this).setTitle(" Kursus Siswa").setView(box).setPositiveButton("Tutup",null).show();
        });
    }
    void addCourse(){EditText e=field("Nama pelajaran");e.setMinLines(2);new AlertDialog.Builder(this).setTitle("Tambah Pelajaran").setView(e).setPositiveButton("Simpan",(d,w)->{ContentValues v=new ContentValues();v.put("student_id",selectedStudentId);v.put("name",e.getText().toString());db.getWritableDatabase().insert("courses",null,v);Toast.makeText(this,"Pelajaran ditambahkan",Toast.LENGTH_SHORT).show();}).setNegativeButton("Batal",null).show();}
    void editCourse(int id){
        EditText e=field("Nama pelajaran");e.setMinLines(2);;Cursor c=db.getReadableDatabase().rawQuery("SELECT name FROM courses WHERE id=?",new String[]{""+id});if(c.moveToFirst())e.setText(c.getString(0));c.close();
        new AlertDialog.Builder(this).setTitle("Edit Pelajaran").setView(e).setPositiveButton("Simpan",(d,w)->{ContentValues v=new ContentValues();v.put("name",e.getText().toString());db.getWritableDatabase().update("courses",v,"id=?",new String[]{""+id});}).setNeutralButton("Hapus",(d,w)->db.getWritableDatabase().delete("courses","id=?",new String[]{""+id})).setNegativeButton("Batal",null).show();
    }

    void manageGrades(){
        chooseThen(()->{
            EditText sub=field("Mata pelajaran");sub.setMinLines(2);EditText score=field("Nilai (0-100)");score.setMinLines(2);score.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            new AlertDialog.Builder(this).setTitle(" Beri / Tambah Nilai").setView(makeBox(sub,score)).setPositiveButton("Simpan",(d,w)->{
                ContentValues v=new ContentValues();v.put("student_id",selectedStudentId);v.put("subject",sub.getText().toString());v.put("score",score.getText().toString());
                db.getWritableDatabase().insert("grades",null,v);Toast.makeText(this,"Nilai tersimpan",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Batal",null).show();
        });
    }

    void sendMessage(){
        chooseThen(()->{
            EditText e=field("Tulis pesan untuk siswa");e.setMinLines(2);
            new AlertDialog.Builder(this).setTitle(" Kirim Pesan").setView(e).setPositiveButton("Kirim",(d,w)->{
                ContentValues v=new ContentValues();v.put("student_id",selectedStudentId);v.put("message",e.getText().toString());v.put("created_at",new SimpleDateFormat("dd-MM-yyyy HH:mm",Locale.getDefault()).format(new Date()));
                db.getWritableDatabase().insert("messages",null,v);Toast.makeText(this,"Pesan dikirim",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Batal",null).show();
        });
    }

    void sendZoom(){
        chooseThen(()->{
            LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
            EditText title=field("Judul meeting");title.setMinLines(2);EditText date=field("Tanggal & jam, contoh 12-09-2026 16:00");date.setMinLines(2);EditText link=field("Link Zoom");link.setMinLines(2);box.addView(title);box.addView(date);box.addView(link);
            new AlertDialog.Builder(this).setTitle(" Kirim Zoom").setView(box).setPositiveButton("Kirim",(d,w)->{
                ContentValues v=new ContentValues();v.put("student_id",selectedStudentId);v.put("title",title.getText().toString());v.put("date",date.getText().toString());v.put("link",link.getText().toString());
                db.getWritableDatabase().insert("meetings",null,v);Toast.makeText(this,"Link Zoom dikirim",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Batal",null).show();
        });
    }

    void manageSchedule(){
        chooseThen(()->{
            LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
            EditText subject=field("Pelajaran");subject.setMinLines(2);EditText date=field("Tanggal, contoh 12-09-2026");date.setMinLines(2);EditText time=field("Jam, contoh 16:00");time.setMinLines(2);EditText teacher=field("Nama pengajar");teacher.setMinLines(2);
            box.addView(subject);box.addView(date);box.addView(time);box.addView(teacher);
            new AlertDialog.Builder(this).setTitle(" Tambah Jadwal").setView(box).setPositiveButton("Simpan",(d,w)->{
                ContentValues v=new ContentValues();v.put("student_id",selectedStudentId);v.put("subject",subject.getText().toString());v.put("date",date.getText().toString());v.put("time",time.getText().toString());v.put("teacher",teacher.getText().toString());
                db.getWritableDatabase().insert("schedules",null,v);Toast.makeText(this,"Jadwal ditambahkan",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Batal",null).show();
        });
    }

    LinearLayout makeBox(EditText... es){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);for(EditText e:es)l.addView(e);return l;}
}
