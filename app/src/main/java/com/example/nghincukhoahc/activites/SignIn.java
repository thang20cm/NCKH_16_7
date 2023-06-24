package com.example.nghincukhoahc.activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nghincukhoahc.ChoXetDuyet;
import com.example.nghincukhoahc.MainActivity;
import com.example.nghincukhoahc.MainActivitySuperAdmin;
import com.example.nghincukhoahc.UserActivity;
import com.example.nghincukhoahc.databinding.ActivitySignInBinding;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            // Đã đăng nhập, chuyển đến trang chính phù hợp
            if (preferenceManager.getBoolean(Constants.KEY_COLLECTION_SUPER_ADMIN)) {
                startActivity(new Intent(getApplicationContext(), MainActivitySuperAdmin.class));
            }else if(preferenceManager.getBoolean(Constants.KEY_COLLECTION_ADMIN)){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
            else{
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
            }
            finish(); // Kết thúc Activity hiện tại
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        // Chọn RadioButton "Sinh Viên" làm mặc định
        binding.radioButtonStudent.setChecked(true);

    }

    private void setListeners(){
        binding.textCreateAccountAdmin.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpAdmin.class)));
        binding.textCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUp.class)));

        binding.buttonSignIn.setOnClickListener(v -> {
            binding.radioButtonStudent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    binding.radioButtonAdmin.setChecked(false);
                    binding.radioButtonSuperAdmin.setChecked(false);
                }
            });
            if (isValidSignInDetails()) {
                int selectedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();
                if (selectedRadioButtonId == binding.radioButtonStudent.getId()) {
                    singIn();
                } else if (selectedRadioButtonId == binding.radioButtonAdmin.getId()) {
                    signInAdmin();
                } else if (selectedRadioButtonId == binding.radioButtonSuperAdmin.getId()) {
                    signInSuperadmin();
                }
            }
        });

    }

    private void signInSuperadmin() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_SUPER_ADMIN)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_SUPER_ADMIN,true);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_USERS,false);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_ADMIN,false);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_CLASS,documentSnapshot.getString(Constants.KEY_CLASS));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivitySuperAdmin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        loading(false);
                        showToast("Unable to SingIn");
                    }
                });

    }


    private void signInAdmin() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ADMIN)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String status = documentSnapshot.getString(Constants.KEY_STATUS);
                        if (status != null && status.equals("Enable")) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putBoolean(Constants.KEY_COLLECTION_USERS,false);
                            preferenceManager.putBoolean(Constants.KEY_COLLECTION_SUPER_ADMIN,false);
                            preferenceManager.putBoolean(Constants.KEY_COLLECTION_ADMIN,true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            preferenceManager.putString(Constants.KEY_CLASS, documentSnapshot.getString(Constants.KEY_CLASS));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            preferenceManager.putString(Constants.KEY_STATUS, documentSnapshot.getString(Constants.KEY_STATUS));
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            preferenceManager.putString(Constants.KEY_CLASS, documentSnapshot.getString(Constants.KEY_CLASS));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            preferenceManager.putString(Constants.KEY_STATUS, documentSnapshot.getString(Constants.KEY_STATUS));
                            startActivity(new Intent(getApplicationContext(), ChoXetDuyet.class));
                            finish();
                        }
                    } else {
                        loading(false);
                        showToast("Unable to SignIn");
                    }
                });
    }
    private void singIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_USERS,true);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_SUPER_ADMIN,false);
                        preferenceManager.putBoolean(Constants.KEY_COLLECTION_ADMIN,false);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_CLASS,documentSnapshot.getString(Constants.KEY_CLASS));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        loading(false);
                        showToast("Unable to SingIn");
                    }
                });

    }
    private void loading(Boolean isloading){
        if(isloading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else {
            return true;
        }
    }
}