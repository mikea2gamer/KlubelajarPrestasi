package com.klubelajar.prestasi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LoginActivity extends Activity {
    DBHelper db;
    EditText username, password;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        db = new DBHelper(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.loginButton);

        login.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString().trim();
            SQLiteDatabase d = db.getReadableDatabase();
            Cursor c = d.rawQuery("SELECT id,name,role FROM users WHERE username=? AND password=?", new String[]{u,p});
            if(c.moveToFirst()) {
                int id = c.getInt(0);
                String name = c.getString(1);
                String role = c.getString(2);
                getSharedPreferences("session", MODE_PRIVATE).edit().putInt("user_id",id).putString("name",name).putString("role",role).apply();
                if(role.equals("admin")) startActivity(new Intent(this, AdminActivity.class));
                else startActivity(new Intent(this, MainActivity.class));
                finish();
            } else Toast.makeText(this,"Username atau password salah",Toast.LENGTH_SHORT).show();
            c.close();
        });
    }
}
