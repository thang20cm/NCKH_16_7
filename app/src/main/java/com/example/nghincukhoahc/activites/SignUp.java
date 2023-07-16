package com.example.nghincukhoahc.activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nghincukhoahc.ChoXetDuyetUser;
import com.example.nghincukhoahc.MainActivity;
import com.example.nghincukhoahc.R;
import com.example.nghincukhoahc.SQLite.DatabaseKhoaHelper;
import com.example.nghincukhoahc.databinding.ActivitySignUpUserBinding;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignUp extends AppCompatActivity {
    private ActivitySignUpUserBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseKhoaHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        dbHelper = new DatabaseKhoaHelper(this);

        ArrayAdapter<String> khoaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getKhoaData());
        khoaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerKhoa.setAdapter(khoaAdapter);


        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        boolean isSignedIn = preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN);
        String adminStatus = preferenceManager.getString(Constants.KEY_STATUS);
        if(isSignedIn && adminStatus != null) {
            if (adminStatus.equals("Enable")) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), ChoXetDuyetUser.class);
                startActivity(intent);
                finish();
            }
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
        binding.spinnerKhoa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedKhoa = parent.getItemAtPosition(position).toString();
                loadClassesByKhoa(selectedKhoa);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì khi không có mục nào được chọn
            }
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

        String selectedKhoa = binding.spinnerKhoa.getSelectedItem().toString();
        String selectedClass = binding.spinnerClass.getSelectedItem().toString();
        String name = binding.inputName.getText().toString();
        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_STATUS, "Disable");
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_PASSWORD, password);
        user.put(Constants.KEY_IMAGE, encodedImage);
        user.put(Constants.KEY_CLASS, selectedClass);
        user.put(Constants.KEY_KHOA, selectedKhoa);

        db.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, name);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), ChoXetDuyetUser.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    String adminId = documentReference.getId();
                    checkAdminStatus(adminId);
                    preferenceManager.putString(Constants.KEY_CLASS, selectedClass);
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
            showToast("Hãy chọn hình ảnh");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Nhập tên");
            return false;
//        } else if (Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches() || binding.inputEmail.getText().toString().trim().isEmpty()) {
//            showToast("Hãy nhập đúng email");
//            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Nhập mật khẩu");
            return false;
        } else if (binding.inputComfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Nhập lại mật khẩu");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputComfirmPassword.getText().toString())) {
            showToast("Mật khẩu và nhập lại mật khẩu phải giống nhau");
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


    private void checkAdminStatus(String adminId) {
        db.collection(Constants.KEY_COLLECTION_ADMIN)
                .document(adminId)
                .collection(Constants.KEY_SUBCOLLECTION_ADMIN)
                .document(Constants.KEY_STATUS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String status = documentSnapshot.getString(Constants.KEY_STATUS);
                            if (status != null && status.equals("Enable")) {
                                // Điều hướng giao diện MainActivity
                                preferenceManager.putString(Constants.KEY_STATUS, status); // Lưu trạng thái admin
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                preferenceManager.putString(Constants.KEY_STATUS, status);
                                // Điều hướng giao diện chờ xét duyệt (WaitingApprovalActivity)
                                Intent intent = new Intent(getApplicationContext(), ChoXetDuyetUser.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi

                    }
                });

    }
    private List<String> getKhoaData() {
        List<String> khoaList = new ArrayList<>();

        // Mở cơ sở dữ liệu để đọc
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Truy vấn dữ liệu từ bảng tableKhoa
        Cursor cursor = db.rawQuery("SELECT nameKhoa FROM tableKhoa", null);

        // Lặp qua các hàng và thêm dữ liệu vào danh sách khoa
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String khoa = cursor.getString(cursor.getColumnIndex("nameKhoa"));
                khoaList.add(khoa);
            } while (cursor.moveToNext());
        }

        // Đóng cursor và cơ sở dữ liệu
        cursor.close();
        db.close();

        return khoaList;
    }

    private void loadClassesByKhoa(String khoa) {
        long khoaId = dbHelper.getKhoaId(khoa);
        List<String> classList = new ArrayList<>();

        if (khoaId != -1) {
            Cursor cursor = dbHelper.getClassesByKhoa(khoaId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String className = cursor.getString(cursor.getColumnIndex("className"));
                    classList.add(className);
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        binding.spinnerClass.setAdapter(classAdapter);
    }

}

