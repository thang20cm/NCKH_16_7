package com.example.nghincukhoahc;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nghincukhoahc.activites.AdminChat;
import com.example.nghincukhoahc.activites.ManagerAdmin;
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

public class MainActivity extends AppCompatActivity {
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

   private TextView textViewAdminClass;

   ArrayList<DataClass> dataClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();

        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        String email = preferenceManager.getString(Constants.KEY_EMAIL);
        String name = preferenceManager.getString(Constants.KEY_NAME);
        String image = preferenceManager.getString(Constants.KEY_IMAGE);

        textViewAdminClass = findViewById(R.id.adminClass);
        textViewAdminClass.setText(name);

        adminClass = preferenceManager.getString(Constants.KEY_CLASS);
        Toast.makeText(MainActivity.this,"Class is: "+adminClass,Toast.LENGTH_SHORT).show();




        // Khi khởi tạo MainActivity, ẩn Button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bangtin);
        bottomNavigationView.setBackground(null);




        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bangtin) {
                return true;
            }
            else if(item.getItemId() == R.id.chatadmin){
                startActivity(new Intent(getApplicationContext(), AdminChat.class));
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

        adapter = new MyAdapter(MainActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Bài Viết");
        collectionReference.addSnapshotListener((value, error) -> {
            if(error != null){
                dialog.dismiss();
                return;
            }
//            dataList.clear();
            for(DocumentChange dc : value.getDocumentChanges()){
                DataClass dataClass = dc.getDocument().toObject(DataClass.class);
                dataClass.setKey(dc.getDocument().getId());
                if(dataClass.getDataLang().equals(adminClass)){
                    switch (dc.getType()){
                        case ADDED:
                            dataList.add(dataClass);
                            break;
                        case MODIFIED:
                            int index = getIndexByKey(dataClass.getKey());
                            if(index != -1){
                                dataList.set(index,dataClass);
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

//        logoutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Create an AlertDialog.Builder object
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//
//                // Set the message for the AlertDialog
//                builder.setMessage("Bạn có chắc chắn muốn đăng xuất?");
//
//                // Set the positive button for the AlertDialog
//                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // User clicked the "Có" button
//                        // Start the LoginActivity
//                        Intent intent = new Intent(MainActivity.this, SignIn.class);
//                        startActivity(intent);
//                        finish(); // Close MainActivity
//                    }
//                });
//
//                // Set the negative button for the AlertDialog
//                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // User clicked the "Không" button
//                        // Do nothing and dismiss the AlertDialog
//                    }
//                });
//
//                // Create and show the AlertDialog
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        });


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

        //preferenceManager.clear(); // Xóa hết dữ liệu của tài khoản hiện tại khi thoát ứng dụng
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}