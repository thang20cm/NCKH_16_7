package com.example.nghincukhoahc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.nghincukhoahc.activites.SignIn;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(1000);
                    startActivity(new Intent(SplashScreen.this, SignIn.class));
                }
                catch (Exception e){

                }
            }
        };thread.start();
    }
}