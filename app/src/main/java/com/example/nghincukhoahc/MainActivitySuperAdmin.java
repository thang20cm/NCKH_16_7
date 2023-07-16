package com.example.nghincukhoahc;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nghincukhoahc.activites.ManagerAdmin;
import com.example.nghincukhoahc.activites.ManagerUser;
import com.example.nghincukhoahc.activites.SignIn;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivitySuperAdmin extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapter adapter;
    SearchView searchView;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    CollectionReference collectionReference;
    private String adminClass;
    PreferenceManager preferenceManager;
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    TextView addKhoa,danhsachKhoa;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_super_admin);



        if (!hasPermissions(this, PERMISSIONS)) {
            // Kiểm tra xem hộp thoại yêu cầu cấp quyền đã hiển thị trước đó hay chưa
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[0])) {
                // Hộp thoại yêu cầu cấp quyền đã hiển thị trước đó và người dùng từ chối
                // Hiển thị thông báo giải thích và yêu cầu cấp quyền một lần nữa
                showPermissionExplanationDialog();
            } else {
                // Hiển thị hộp thoại yêu cầu cấp quyền
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE);
            }
        } else {
            // Quyền đã được cấp, thực hiện các hành động khác trong ứng dụng của bạn
            // ...
        }

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();

        // Khi khởi tạo MainActivity, ẩn Button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bangtin);
        ColorStateList iconColors = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        getResources().getColor(R.color.color_upt_yellow),
                        getResources().getColor(R.color.white)
                }
        );


        bottomNavigationView.setItemIconTintList(iconColors);



        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin) {
                return true;
            } else if (item.getItemId() == R.id.fab_add) {
                Intent intent = new Intent(MainActivitySuperAdmin.this, UploadActivity.class);
                startActivity(intent);
            } else if(item.getItemId() == R.id.quanlyAdmin){
                startActivity(new Intent(getApplicationContext(), ManagerAdmin.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            }
            return false;
        });

        addKhoa = findViewById(R.id.addKhoa);
        addKhoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitySuperAdmin.this,AddKhoaActivity.class);
                startActivity(intent);
                finish();
            }
        });



        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));

        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivitySuperAdmin.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivitySuperAdmin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapter(MainActivitySuperAdmin.this, dataList);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Bài Viết");
        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                dialog.dismiss();
                return;
            }
            for (DocumentChange dc : value.getDocumentChanges()) {
                DataClass dataClass = dc.getDocument().toObject(DataClass.class);
                dataClass.setKey(dc.getDocument().getId());
                switch (dc.getType()) {
                    case ADDED:
                        dataList.add(dataClass);
                        break;
                    case MODIFIED:
                        int index = getIndexByKey(dataClass.getKey());
                        if (index != -1) {
                            dataList.set(index, dataClass);
                        }
                        break;
                    case REMOVED:
                        dataList.removeIf(data -> data.getKey().equals(dataClass.getKey()));
                        break;
                }
            }
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });



        AppCompatImageView logoutButton = findViewById(R.id.logoutButton);



    }



    public void searchList(String text) {
        ArrayList<DataClass> searchList = new ArrayList<>();
        for (DataClass dataClass : dataList) {
            if (dataClass.getDataTitle().toLowerCase().contains(text.toLowerCase()) ||
                    dataClass.getDataDesc().toLowerCase().contains(text.toLowerCase()) ||
                    dataClass.getDataLang().toLowerCase().contains(text.toLowerCase())
            ) {
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    private int getIndexByKey(String key) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private void setListeners(){
        AppCompatImageView imageViewSignOut = findViewById(R.id.logoutButton);
        imageViewSignOut.setOnClickListener(v -> singOut());
    }
    private void singOut(){
        showToast("Signing out...");
        startActivity(new Intent(getApplicationContext(), SignIn.class));
        preferenceManager.clear();
//        showToast("Signing out...");
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        DocumentReference documentReference =
//                database.collection(Constants.KEY_COLLECTION_SUPER_ADMIN).document(
//                        preferenceManager.getString(Constants.KEY_USER_ID)
//                );
//        HashMap<String, Object> updates = new HashMap<>();
//        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
//        documentReference.update(updates)
//                .addOnSuccessListener(unused -> {
//                    preferenceManager.clear();
//                    startActivity(new Intent(getApplicationContext(), SignIn.class));
//                    finish();
//                })
//                .addOnFailureListener(e -> showToast("unable to sign out"));
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, thực hiện các hành động khác trong ứng dụng của bạn
                // ...
            } else {
                // Người dùng từ chối cấp quyền, bạn có thể hiển thị thông báo hoặc thực hiện các xử lý phù hợp
                Toast.makeText(this, "Ứng dụng cần quyền truy cập vào bộ nhớ ngoài để tải file.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quyền truy cập bộ nhớ ngoài");
        builder.setMessage("Ứng dụng cần quyền truy cập vào bộ nhớ ngoài để tải file. Vui lòng cấp quyền để tiếp tục.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Hiển thị hộp thoại yêu cầu cấp quyền
            ActivityCompat.requestPermissions(MainActivitySuperAdmin.this, PERMISSIONS, REQUEST_PERMISSION_CODE);
        });
        builder.setCancelable(false);
        builder.show();
    }



}

