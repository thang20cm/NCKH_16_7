package com.example.nghincukhoahc.activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.nghincukhoahc.MainActivitySuperAdmin;
import com.example.nghincukhoahc.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagerAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();


        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setSelectedItemId(R.id.chat);

        binding.bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.bangtin){
                startActivity(new Intent(getApplicationContext(), MainActivitySuperAdmin.class));
                overridePendingTransition(R.anim.slider_in_right, R.anim.silde_out_left);
                finish();
                return true;
            }else if(item.getItemId() == R.id.quanlyAdmin){
                return true;
            }
            return false;
        });
    }



    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ADMIN)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
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
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No User available"));
        binding.textErrorMessage.setText(View.VISIBLE);
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

    }
}