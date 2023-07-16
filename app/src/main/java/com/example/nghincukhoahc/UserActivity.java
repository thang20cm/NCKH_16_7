package com.example.nghincukhoahc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nghincukhoahc.activites.ChatActivity;
import com.example.nghincukhoahc.activites.MainActivity;
import com.example.nghincukhoahc.activites.SignIn;
import com.example.nghincukhoahc.activites.UsersActivity;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapterUser adapter;
    SearchView searchView;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    private PreferenceManager preferenceManager;
    CollectionReference collectionReference;

    private boolean showAllPosts = false;
    String userId;
    TextView textViewAdminClass;
    ImageView imageUser;

    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        fetchUserDataAndUpdatePreferences();

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

        String userClass = preferenceManager.getString(Constants.KEY_CLASS);
        String userKhoa = preferenceManager.getString(Constants.KEY_KHOA);
        if (userClass.equals("Tất cả")) {
            showAllPosts = true;
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bangtin_user);
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
            if (item.getItemId() == R.id.bangtin_user) {
                return true;
            } else if (item.getItemId() == R.id.sotay_user) {
                startActivity(new Intent(getApplicationContext(), SoTayForSinhVien.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.chat) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.xemdiem) {
                Intent intent = new Intent(getApplicationContext(), MonHocUser.class);
                intent.putExtra(Constants.KEY_CLASS, preferenceManager.getString(Constants.KEY_CLASS)); // Gửi giá trị "lop" qua Intent
                startActivity(intent);
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;

            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerView);

        searchView = findViewById(R.id.search);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));

        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(UserActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();
        adapter = new MyAdapterUser(UserActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        imageUser = findViewById(R.id.imageUser);
        imageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileUser.class);
                intent.putExtra(Constants.KEY_USER_ID, getIntent().getStringExtra(Constants.KEY_USER_ID));
                intent.putExtra(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                intent.putExtra(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
                intent.putExtra(Constants.KEY_CLASS, preferenceManager.getString(Constants.KEY_CLASS));
                intent.putExtra(Constants.KEY_KHOA, preferenceManager.getString(Constants.KEY_KHOA));
                intent.putExtra(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                startActivity(intent);
                finish();
            }
        });

        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        imageUser.setImageBitmap(bitmap);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Bài Viết");

        collectionReference.addSnapshotListener((value, error) -> {
            if(error != null){
                dialog.dismiss();
                return;
            }
            dataList.clear();
            for (QueryDocumentSnapshot document : value) {
                DataClass dataClass = document.toObject(DataClass.class);
                if(showAllPosts||dataClass.getDataLang().equals("Tất cả")||dataClass.getDataLang().equals(userClass)||dataClass.getDataLang().equals(userKhoa)){
                    dataClass.setKey(document.getId());
                    dataList.add(dataClass);
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
    }


    public void searchList(String text){
        ArrayList<DataClass> searchList = new ArrayList<>();
        for (DataClass dataClass: dataList){
            if (dataClass.getDataTitle().toLowerCase().contains(text.toLowerCase())||
                    dataClass.getDataDesc().toLowerCase().contains(text.toLowerCase())||
                    dataClass.getDataLang().toLowerCase().contains(text.toLowerCase())
            ){
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
//
    }



    private void setListeners(){
        AppCompatImageView imageViewSignOut = findViewById(R.id.logoutButton);
        imageViewSignOut.setOnClickListener(v -> singOut());
    }


    private void singOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("unable to sign out"));
    }
    @Override
    protected void onStop() {
        super.onStop();
        //preferenceManager.clear(); // Xóa hết dữ liệu của tài khoản hiện tại khi thoát ứng dụng
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void fetchUserDataAndUpdatePreferences(){
        FirebaseFirestore dbnew = FirebaseFirestore.getInstance();
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        DocumentReference userRef = dbnew.collection(Constants.KEY_COLLECTION_USERS).document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot ->{
            if(documentSnapshot.exists()){
                String newName = documentSnapshot.getString(Constants.KEY_NAME);
                String newEmail = documentSnapshot.getString(Constants.KEY_EMAIL);
                String newClass = documentSnapshot.getString(Constants.KEY_CLASS);
                String newKhoa = documentSnapshot.getString(Constants.KEY_KHOA);
                String newStatus = documentSnapshot.getString(Constants.KEY_STATUS);

                preferenceManager.putString(Constants.KEY_NAME,newName);
                preferenceManager.putString(Constants.KEY_EMAIL,newEmail);
                preferenceManager.putString(Constants.KEY_CLASS,newClass);
                preferenceManager.putString(Constants.KEY_KHOA,newKhoa);
                preferenceManager.putString(Constants.KEY_STATUS,newStatus);

                textViewAdminClass = findViewById(R.id.adminClass);
                textViewAdminClass.setText(newName);

                preferenceManager.apply();
                if (newStatus.equals("Disable")) {
                    startActivity(new Intent(getApplicationContext(), ChoXetDuyetUser.class));
                    finish();
                }
            }else {
                Toast.makeText(com.example.nghincukhoahc.UserActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(com.example.nghincukhoahc.UserActivity.this, "Error retrieving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
            ActivityCompat.requestPermissions(UserActivity.this, PERMISSIONS, REQUEST_PERMISSION_CODE);
        });
        builder.setCancelable(false);
        builder.show();
    }



}
