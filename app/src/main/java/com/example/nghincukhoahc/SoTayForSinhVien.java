package com.example.nghincukhoahc;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nghincukhoahc.activites.MainActivity;
import com.example.nghincukhoahc.activites.UsersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class SoTayForSinhVien extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so_tay_for_sinh_vien);

        WebView webView = findViewById(R.id.webViewSV);

        String url = "https://upt.edu.vn/stsv2022/?fbclid=IwAR0sl9xAGGHXB_T6iD1ANBnn0C3NJY0XU5PvbaYQfZp94W7ngssnHoNaN8E";
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.sotay_user);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin_user) {
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.sotay_user) {

                return true;
            } else if (item.getItemId() == R.id.xemsau) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.xemdiem) {
                startActivity(new Intent(getApplicationContext(), XemDiem.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;

            }
            return false;
        });


    }
}
