package com.example.nghincukhoahc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.nghincukhoahc.activites.SignIn;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;

import javax.annotation.Nullable;

public class ChoXetDuyet extends AppCompatActivity {

    private ListenerRegistration statusListener;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cho_xet_duyet);

        preferenceManager = new PreferenceManager(getApplicationContext());

        boolean fromMainActivity = getIntent().getBooleanExtra(Constants.KEY_FROM_MAIN_ACTIVITY, false);

        if (fromMainActivity) {
            // Nếu được khởi chạy từ MainActivity, không cần kiểm tra trạng thái admin
            // Chuyển hướng ngay sang MainActivity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Nếu không được khởi chạy từ MainActivity, tiếp tục kiểm tra trạng thái admin
            // Lấy ID của admin từ PreferenceManager
            String adminId = preferenceManager.getString(Constants.KEY_USER_ID);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            statusListener = db.collection(Constants.KEY_COLLECTION_ADMIN)
                    .document(adminId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                            // ...
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Lấy ID của admin từ PreferenceManager
        String adminId = preferenceManager.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        statusListener = db.collection(Constants.KEY_COLLECTION_ADMIN)
                .document(adminId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String status = documentSnapshot.getString(Constants.KEY_STATUS);
                            if (status != null) {
                                if (status.equals("Enable")) {
                                    // Cập nhật trạng thái trong PreferenceManager
                                    preferenceManager.putString(Constants.KEY_STATUS, "Enable");

                                    // Kiểm tra xem đã từ MainActivity chuyển đến ChoXetDuyet chưa
                                    boolean fromMainActivity = getIntent().getBooleanExtra(Constants.KEY_FROM_MAIN_ACTIVITY, false);
                                    if (!fromMainActivity) {
                                        // Chuyển hướng sang MainActivity
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Hiển thị thông báo trạng thái đã chuyển sang "Enable"
                                        Toast.makeText(ChoXetDuyet.this, "Trạng thái đã chuyển sang Enable", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Hiển thị thông báo trạng thái chưa chuyển sang "Enable"
                                    Toast.makeText(ChoXetDuyet.this, "Trạng thái Disable", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        preferenceManager.clear(); // Xóa hết dữ liệu của tài khoản hiện tại khi thoát ứng dụng
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) {
            statusListener.remove();
        }
    }
}
