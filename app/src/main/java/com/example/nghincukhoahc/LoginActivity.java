package com.example.nghincukhoahc;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword,password_username;
    Button loginButton,loginButtonAdmin;
    CheckBox saveLoginCheckBox;
    TextView signupRedirectText,signupRedirectTextAdmin;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean validateFields() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email cannot be empty");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }

        return true;
    }




    //    @SuppressLint("MissingInflatedId")

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButtonAdmin = findViewById(R.id.login_buttonAdmin);

        //Quên mật khẩu
        TextView quenMatKhau = findViewById(R.id.forgotPassword);
        quenMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, QuenMatKhau.class);
                startActivity(intent);
                finish();
            }
        });

        //Lưu đăng nhập
        sharedPreferences = getSharedPreferences("loginPrefs",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loginEmail = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        saveLoginCheckBox = findViewById(R.id.saveLogin);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false);
        if(isLoggedIn){
            Intent intent = new Intent(LoginActivity.this,UserActivity.class);
            startActivity(intent);
            finish();
        }

        if (sharedPreferences.contains("email")&& sharedPreferences.contains("password")){
            String savedEmail = sharedPreferences.getString("email","");
            String savedPassword = sharedPreferences.getString("password","");
            loginEmail.setText(savedEmail);
            loginPassword.setText(savedPassword);
            saveLoginCheckBox.setChecked(true);
        }




        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //Login for Admin
        loginButtonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();
                 if (email.equals("admin") && password.equals("admin")) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                 else {
                     Toast.makeText(LoginActivity.this,"Tài khoản hoặc mật khẩu không chính xác",Toast.LENGTH_SHORT).show();
                 }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();
                if(validateFields()){
                    if(saveLoginCheckBox.isChecked()){
                        editor.putString("email",email);
                        editor.putString("password", password);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                    }
                    else {
                        editor.remove("email");
                        editor.remove("password");
                        editor.putBoolean("isLoggedIn", false);
                        editor.apply();
                    }

                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String userID = firebaseAuth.getCurrentUser().getUid();
                                Toast.makeText(LoginActivity.this,"Đăng nhập thành công",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                                startActivity(intent);
                                finish();


                            }
                            else {
                                Toast.makeText(LoginActivity.this,"Đăng nhập thất bại" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
        signupRedirectText = findViewById(R.id.signupRedirectText);


        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}

