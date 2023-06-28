package com.example.nghincukhoahc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nghincukhoahc.activites.DetailManagerAdmin;
import com.example.nghincukhoahc.activites.ManagerAdmin;
import com.example.nghincukhoahc.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayOutputStream;

public class UpdateAdminManager extends AppCompatActivity {

    private EditText updateNameadmin, updateEmailadmin, updatePasswordadmin;
    private ImageView updateImgAdmin,backbtn;
    private Spinner updateClassadmin;
    private ArrayAdapter<String> spinnerAdapter,spinnerAdapterQTC;
    private String[] classArray,classArrayQTC;
    private Button updateAdminBtn;
    private String nameAdmin, emailAdmin, passwordAdmin, classAdmin;
    private DocumentReference documentReference;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String userId;
   private Spinner updateQuentruycap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin_manager);



        updateAdminBtn = findViewById(R.id.saveAdminBtn);
        updateImgAdmin = findViewById(R.id.updateImageAdmin);

        classArray = getResources().getStringArray(R.array.upload_class_array);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classArray);

        classArrayQTC = getResources().getStringArray(R.array.quyentruycap_array);
        spinnerAdapterQTC = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classArrayQTC);


        updateQuentruycap = findViewById(R.id.QuyenTruyCap);
        updateNameadmin = findViewById(R.id.updateNameAdmin);
        updateEmailadmin = findViewById(R.id.updateEmailAdmin);
        updatePasswordadmin = findViewById(R.id.updatePWAdmin);
        updateClassadmin = findViewById(R.id.updateClassSpn);

        updateClassadmin.setAdapter(spinnerAdapter);
        updateQuentruycap.setAdapter(spinnerAdapterQTC);

        backbtn = findViewById(R.id.backButton);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateImgAdmin.setDrawingCacheEnabled(true);
                updateImgAdmin.buildDrawingCache();
                Bitmap bitmap = updateImgAdmin.getDrawingCache();

                // Chuyển đổi bitmap thành base64 string
                String base64Image = convertBitmapToBase64(bitmap);

                // Chuyển đổi bitmap thành base64 string
                // Chuyển đổi bitmap thành base64 string
                Intent intent = new Intent(UpdateAdminManager.this,DetailManagerAdmin.class);
                intent.putExtra(Constants.KEY_NAME, updateNameadmin.getText().toString());
                intent.putExtra(Constants.KEY_EMAIL, updateEmailadmin.getText().toString());
                intent.putExtra(Constants.KEY_PASSWORD, updatePasswordadmin.getText().toString());
                intent.putExtra(Constants.KEY_CLASS, updateClassadmin.getSelectedItem().toString());
                intent.putExtra(Constants.KEY_STATUS, updateQuentruycap.getSelectedItem().toString());


                intent.putExtra(Constants.KEY_IMAGE, base64Image);

                setResult(RESULT_OK, intent);
                startActivity(intent);
                finish();
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userId = bundle.getString(Constants.KEY_USER_ID);
            updateNameadmin.setText(bundle.getString(Constants.KEY_NAME));
            updateEmailadmin.setText(bundle.getString(Constants.KEY_EMAIL));
            updatePasswordadmin.setText(bundle.getString(Constants.KEY_PASSWORD));
            String currentDataClass = bundle.getString(Constants.KEY_CLASS);
            String currentDataQTC = bundle.getString(Constants.KEY_STATUS);
            String base64Image = bundle.getString(Constants.KEY_IMAGE);

            Toast.makeText(UpdateAdminManager.this,"userid is: "+userId,Toast.LENGTH_SHORT).show();

            if(base64Image != null && !base64Image.isEmpty()){
                Bitmap imageBitmap = convertBase64ToBitmap(base64Image);
                if (imageBitmap != null) {
                    updateImgAdmin.setImageBitmap(imageBitmap);
                }
            }
            int selectionPosition = spinnerAdapter.getPosition(currentDataClass);
            if (selectionPosition != -1) {
                updateClassadmin.setSelection(selectionPosition);
            }

            int selectionPositionQTC = spinnerAdapterQTC.getPosition(currentDataQTC);
            if(selectionPositionQTC != -1){
                updateQuentruycap.setSelection(selectionPositionQTC);
            }
        }
        updateAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để cập nhật dữ liệu
                updateAdminData();
            }
        });



    }

    private Bitmap convertBase64ToBitmap(String base64Image) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void updateAdminData() {
        String selectedStatus = updateQuentruycap.getSelectedItem().toString();

        nameAdmin = updateNameadmin.getText().toString();
        emailAdmin = updateEmailadmin.getText().toString();
        passwordAdmin = updatePasswordadmin.getText().toString();
        classAdmin = updateClassadmin.getSelectedItem().toString();

        // Kiểm tra các trường dữ liệu có hợp lệ không
        if (nameAdmin.isEmpty() || emailAdmin.isEmpty() || passwordAdmin.isEmpty()) {
            Toast.makeText(UpdateAdminManager.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật dữ liệu vào Firestore
        documentReference = firestore.collection("adminclass").document(userId);
        documentReference.update(
                Constants.KEY_NAME, nameAdmin,
                Constants.KEY_EMAIL, emailAdmin,
                Constants.KEY_PASSWORD, passwordAdmin,
                Constants.KEY_CLASS, classAdmin,
                Constants.KEY_STATUS,selectedStatus
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(UpdateAdminManager.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateAdminManager.this, ManagerAdmin.class);
                startActivity(intent);
                finish(); // Kết thúc màn hình khi cập nhật thành công
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateAdminManager.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
