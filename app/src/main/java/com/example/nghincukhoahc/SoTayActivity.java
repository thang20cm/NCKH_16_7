package com.example.nghincukhoahc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class SoTayActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so_tay);

        WebView webView = findViewById(R.id.webView);

        String url = "https://upt.edu.vn/stsv2022/?fbclid=IwAR0sl9xAGGHXB_T6iD1ANBnn0C3NJY0XU5PvbaYQfZp94W7ngssnHoNaN8E";
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.sotay_other);
        bottomNavigationView.setBackground(null);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin_other) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.sotay_other) {

                return true;
            }

            return false;
        });

    }
}
