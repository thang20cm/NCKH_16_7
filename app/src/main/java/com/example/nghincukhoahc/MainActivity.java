package com.example.nghincukhoahc;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nghincukhoahc.activites.ManagerUser;
import com.example.nghincukhoahc.activites.SignIn;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapterAdmin adapter;
    SearchView searchView;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    CollectionReference collectionReference;
    private String adminClass,adminStatus;
    PreferenceManager preferenceManager;
    private DocumentSnapshot documentSnapshot;
    private TextView textViewAdminClass;
    private Task<QuerySnapshot> task;

    ArrayList<DataClass> dataClasses = new ArrayList<>();
    private boolean showAllPosts = false;
    String userClass,name,userId,email,status,image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        fetchUserDataAndUpdatePreferences();


        userClass = preferenceManager.getString(Constants.KEY_CLASS);
        if (userClass.equals("Tất cả")) {
            showAllPosts = true;
        }


//        email = preferenceManager.getString(Constants.KEY_EMAIL);
//        name = preferenceManager.getString(Constants.KEY_NAME);
//        image = preferenceManager.getString(Constants.KEY_IMAGE);
//        status = preferenceManager.getString(Constants.KEY_STATUS);









        //Toast.makeText(MainActivity.this,"Class is: "+adminClass,Toast.LENGTH_SHORT).show();




        // Khi khởi tạo MainActivity, ẩn Button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bangtin);
        bottomNavigationView.setBackground(null);




        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin) {
                return true;
            }
            else if(item.getItemId() == R.id.managerSV){
                startActivity(new Intent(getApplicationContext(), ManagerUser.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            }
            return false;
        });



        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);

        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapterAdmin(MainActivity.this, dataList);
        recyclerView.setAdapter(adapter);



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Bài Viết");
        collectionReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Xử lý lỗi
                return;
            }

            dataList.clear(); // Xóa danh sách hiện tại để cập nhật dữ liệu mới

            for (DocumentChange dc : value.getDocumentChanges()) {
                DataClass dataClass = dc.getDocument().toObject(DataClass.class);
                dataClass.setKey(dc.getDocument().getId());
                if (dataClass.getDataLang().equals(adminClass) || showAllPosts || dataClass.getDataLang().equals("Tất cả")) {
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadAdmin.class);
                intent.putExtra("class",adminClass);
                startActivity(intent);
            }
        });


        AppCompatImageView logoutButton = findViewById(R.id.logoutButton);



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
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_ADMIN).document(
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
        //preferenceManager.clear();

    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void fetchUserDataAndUpdatePreferences(){
        FirebaseFirestore dbnew = FirebaseFirestore.getInstance();
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        DocumentReference userRef = dbnew.collection(Constants.KEY_COLLECTION_ADMIN).document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot ->{
            if(documentSnapshot.exists()){
                String newName = documentSnapshot.getString(Constants.KEY_NAME);
                String newEmail = documentSnapshot.getString(Constants.KEY_EMAIL);
                String newClass = documentSnapshot.getString(Constants.KEY_CLASS);
                String newStatus = documentSnapshot.getString(Constants.KEY_STATUS);

                preferenceManager.putString(Constants.KEY_NAME,newName);
                preferenceManager.putString(Constants.KEY_EMAIL,newEmail);
                preferenceManager.putString(Constants.KEY_CLASS,newClass);
                preferenceManager.putString(Constants.KEY_STATUS,newStatus);

                textViewAdminClass = findViewById(R.id.adminClass);
                textViewAdminClass.setText(newName);
                Toast.makeText(MainActivity.this,"Name is: "+newStatus,Toast.LENGTH_SHORT).show();

                preferenceManager.apply();
                Toast.makeText(MainActivity.this, "Name is: " + newName, Toast.LENGTH_SHORT).show();
                if (newStatus.equals("Disable")) {
                    startActivity(new Intent(getApplicationContext(), ChoXetDuyet.class));
                    finish();
                }
            }else {
                Toast.makeText(MainActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Error retrieving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}