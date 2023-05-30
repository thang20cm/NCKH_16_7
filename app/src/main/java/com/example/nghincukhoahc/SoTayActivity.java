package com.example.nghincukhoahc;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class SoTayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_so_tay);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.sotay);
        bottomNavigationView.setBackground(null);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.sotay) {

                return true;
            }

            return false;
        });
    }
}