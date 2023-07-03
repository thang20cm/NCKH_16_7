package com.example.nghincukhoahc.activites;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.SearchView;

import com.example.nghincukhoahc.MainActivitySuperAdmin;
import com.example.nghincukhoahc.R;
import com.example.nghincukhoahc.UploadActivity;
import com.example.nghincukhoahc.adapters.UserAdapter;
import com.example.nghincukhoahc.databinding.ActivityManagerAdminBinding;
import com.example.nghincukhoahc.listeners.UserListener;
import com.example.nghincukhoahc.listeners.models.User;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManagerAdmin extends BaseActivity implements UserListener {
    private ActivityManagerAdminBinding binding;
    private PreferenceManager preferenceManager;
    private Handler autoReloadHandler = new Handler();
    private Runnable autoReloadRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagerAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        //startAutoReload();




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





        binding.bottomNavigationView.setItemIconTintList(iconColors);
        binding.bottomNavigationView.setSelectedItemId(R.id.quanlyAdmin);

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.bangtin){
                startActivity(new Intent(getApplicationContext(), MainActivitySuperAdmin.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            }
            else if(item.getItemId() == R.id.fab_add){
                startActivity(new Intent(getApplicationContext(), UploadActivity.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            }
            else if(item.getItemId() == R.id.quanlyAdmin){
                return true;
            }
            return false;
        });

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return false;
            }
        });
        EditText searchEditText = binding.search.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));


    }




    private void getUsers() {
        //loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ADMIN)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            User user = queryDocumentSnapshot.toObject(User.class);
                            user.quyentruycap = queryDocumentSnapshot.getString(Constants.KEY_STATUS);
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.password = queryDocumentSnapshot.getString(Constants.KEY_PASSWORD);
                            user.lopqtri = queryDocumentSnapshot.getString(Constants.KEY_CLASS);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                            binding.textErrorMessage.setVisibility(View.GONE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText("Không có lớp trưởng");
        binding.textErrorMessage.setVisibility(View.VISIBLE);
        binding.userRecyclerView.setVisibility(View.GONE);
    }
    private void loading(Boolean isloading){
        if(isloading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), DetailManagerAdmin.class);
        intent.putExtra(Constants.KEY_USER_ID,user.id);
        intent.putExtra(Constants.KEY_USER,user);
        intent.putExtra(Constants.KEY_NAME, user.name);
        intent.putExtra(Constants.KEY_EMAIL, user.email);
        intent.putExtra(Constants.KEY_PASSWORD, user.password);
        intent.putExtra(Constants.KEY_CLASS, user.lopqtri);
        intent.putExtra(Constants.KEY_IMAGE, user.image);
        intent.putExtra(Constants.KEY_STATUS,user.quyentruycap);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStop() {
        super.onStop();
        autoReloadHandler.removeCallbacks(autoReloadRunnable);
    }
    private void startAutoReload() {
        autoReloadRunnable = new Runnable() {
            @Override
            public void run() {
                // Gọi phương thức getUsers() để cập nhật danh sách người dùng
                getUsers();

                // Lặp lại auto reload sau một khoảng thời gian
                autoReloadHandler.postDelayed(this, 500); // Cập nhật sau 5 giây (5000 milliseconds)
            }
        };

        // Bắt đầu auto reload
        autoReloadHandler.postDelayed(autoReloadRunnable, 500); // Cập nhật sau 5 giây (5000 milliseconds)
    }

    private void searchUsers(String searchText) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ADMIN)
                .whereGreaterThanOrEqualTo(Constants.KEY_NAME, searchText)
                .whereLessThanOrEqualTo(Constants.KEY_NAME, searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            User user = queryDocumentSnapshot.toObject(User.class);
                            user.quyentruycap = queryDocumentSnapshot.getString(Constants.KEY_STATUS);
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.password = queryDocumentSnapshot.getString(Constants.KEY_PASSWORD);
                            user.lopqtri = queryDocumentSnapshot.getString(Constants.KEY_CLASS);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();

                            // Check if the name contains the search keyword
                            if (user.name.toLowerCase().contains(searchText.toLowerCase())) {
                                users.add(user);
                            }
                        }

                        // Perform a separate query for class
                        database.collection(Constants.KEY_COLLECTION_ADMIN)
                                .whereEqualTo(Constants.KEY_CLASS, searchText)
                                .get()
                                .addOnCompleteListener(classTask -> {
                                    if (classTask.isSuccessful() && classTask.getResult() != null) {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : classTask.getResult()) {
                                            User user = queryDocumentSnapshot.toObject(User.class);
                                            user.quyentruycap = queryDocumentSnapshot.getString(Constants.KEY_STATUS);
                                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                            user.password = queryDocumentSnapshot.getString(Constants.KEY_PASSWORD);
                                            user.lopqtri = queryDocumentSnapshot.getString(Constants.KEY_CLASS);
                                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                            user.id = queryDocumentSnapshot.getId();
                                            if (!users.contains(user)) {
                                                users.add(user);
                                            }
                                        }
                                    }

                                    // Perform a separate query for status
                                    database.collection(Constants.KEY_COLLECTION_ADMIN)
                                            .whereEqualTo(Constants.KEY_STATUS, searchText)
                                            .get()
                                            .addOnCompleteListener(statusTask -> {
                                                if (statusTask.isSuccessful() && statusTask.getResult() != null) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : statusTask.getResult()) {
                                                        User user = queryDocumentSnapshot.toObject(User.class);
                                                        user.quyentruycap = queryDocumentSnapshot.getString(Constants.KEY_STATUS);
                                                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                                        user.password = queryDocumentSnapshot.getString(Constants.KEY_PASSWORD);
                                                        user.lopqtri = queryDocumentSnapshot.getString(Constants.KEY_CLASS);
                                                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                                        user.id = queryDocumentSnapshot.getId();
                                                        if (!users.contains(user)) {
                                                            users.add(user);
                                                        }
                                                    }
                                                }

                                                if (users.size() > 0) {
                                                    UserAdapter userAdapter = new UserAdapter(users, this);
                                                    binding.userRecyclerView.setAdapter(userAdapter);
                                                    binding.userRecyclerView.setVisibility(View.VISIBLE);
                                                    binding.textErrorMessage.setVisibility(View.GONE);
                                                } else {
                                                    showErrorMessage();
                                                }
                                            });
                                });
                    } else {
                        showErrorMessage();
                    }
                });
    }
    @Override
    public void onBackPressed() {
        // Xử lý hành vi khi nút "Trở về" được bấm
        // Ví dụ: Trở về trang trước đó trong ứng dụng
        // Kiểm tra điều kiện để quyết định hành động cụ thể

        // Gọi super.onBackPressed() để giữ lại hành vi mặc định của nút "Trở về"
        Intent intent = new Intent(ManagerAdmin.this, MainActivitySuperAdmin.class);
        startActivity(intent);
        finish();
    }


}