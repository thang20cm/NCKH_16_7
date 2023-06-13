package com.example.nghincukhoahc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class QuenMatKhau extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);

        ImageView backbutton = findViewById(R.id.backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuenMatKhau.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        EditText emailEdiText = findViewById(R.id.emailEditText);

        Button quenmatkhauButton = findViewById(R.id.forgotPasswordButton);

                quenmatkhauButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdiText.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(QuenMatKhau.this,"Vui lòng nhập địa chỉ email của bạn",Toast.LENGTH_SHORT).show();
                }
                else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(QuenMatKhau.this,"Một email khôi phục mật khẩu đã được gửi đến địa chỉ email của bạn ",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(QuenMatKhau.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(QuenMatKhau.this,"Gửi email khôi phục mật khâẩu thất bại, vui lòng kiểm tra lại tên email của bạn",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}