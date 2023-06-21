package com.example.nghincukhoahc.activites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nghincukhoahc.MainActivity;
import com.example.nghincukhoahc.UserActivity;
import com.example.nghincukhoahc.databinding.ActivitySignUpChatBinding;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignUp extends AppCompatActivity {
    private ActivitySignUpChatBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSingUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void checkExistingUser(String name, String email) {
        AtomicBoolean isNameExists = new AtomicBoolean(false);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_NAME, name)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Tên người dùng đã tồn tại
                        isNameExists.set(true);
                        binding.inputName.setError("Tên người dùng đã tồn tại");
                    } else {
                        // Xóa thông báo lỗi trên ô inputName nếu không có lỗi
                        binding.inputName.setError(null);

                        // Kiểm tra sự tồn tại của email
                        checkExistingEmail(email, isNameExists);
                    }
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private void checkExistingEmail(String email, AtomicBoolean isNameExists) {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Email đã tồn tại
                        binding.inputEmail.setError("Email đã tồn tại");
                    } else {
                        // Xóa thông báo lỗi trên ô inputEmail nếu không có lỗi
                        binding.inputEmail.setError(null);

                        // Tiếp tục đăng ký nếu cả tên và email đều không tồn tại
                        if (!isNameExists.get()) {
                            signUp();
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }




    private void signUp() {
        loading(true);

        String selectedClass = binding.spinnerClass.getSelectedItem().toString();
        String name = binding.inputName.getText().toString();
        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_PASSWORD, password);
        user.put(Constants.KEY_IMAGE, encodedImage);
        user.put(Constants.KEY_CLASS, selectedClass);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, name);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    preferenceManager.putString(Constants.KEY_CLASS, selectedClass);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputComfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputComfirmPassword.getText().toString())) {
            showToast("Password and confirm password must match");
            return false;
        } else {
            String name = binding.inputName.getText().toString();
            String email = binding.inputEmail.getText().toString();
            checkExistingUser(name, email);
            return false;
        }
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSingUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSingUp.setVisibility(View.VISIBLE);
        }
    }
}
