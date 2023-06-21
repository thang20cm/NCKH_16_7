package com.example.nghincukhoahc.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nghincukhoahc.DataClass;
import com.example.nghincukhoahc.R;
import com.example.nghincukhoahc.UpdateAdminManager;
import com.example.nghincukhoahc.utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

public class DetailManagerAdmin extends AppCompatActivity {
    private TextView nameAdmin, emailAdmin, passwordAdmin,classTV,quentruycap;
    private ImageView detaiImageAdmin;
    ImageView backbutton;

    String imageUrl = "";
    Button updateAdminBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_manager_admin);

        backbutton = findViewById(R.id.backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailManagerAdmin.this, ManagerAdmin.class);

                startActivity(intent);
                finish();
            }
        });

        updateAdminBtn = findViewById(R.id.EditAdminBtn);
        updateAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detaiImageAdmin.setDrawingCacheEnabled(true);
                detaiImageAdmin.buildDrawingCache();
                Bitmap bitmap = detaiImageAdmin.getDrawingCache();

                // Chuyển đổi bitmap thành base64 string
                String base64Image = convertBitmapToBase64(bitmap);

                Intent intent = new Intent(DetailManagerAdmin.this,UpdateAdminManager.class);
                intent.putExtra(Constants.KEY_NAME, nameAdmin.getText().toString());
                intent.putExtra(Constants.KEY_EMAIL, emailAdmin.getText().toString());
                intent.putExtra(Constants.KEY_PASSWORD, passwordAdmin.getText().toString());
                intent.putExtra(Constants.KEY_CLASS, classTV.getText().toString());
                intent.putExtra(Constants.KEY_USER_ID, getIntent().getStringExtra(Constants.KEY_USER_ID));
                intent.putExtra(Constants.KEY_IMAGE, base64Image);
                intent.putExtra(Constants.KEY_STATUS,quentruycap.getText().toString());
                startActivity(intent);
                finish();
            }
        });

        quentruycap = findViewById(R.id.QuyenTruyCap);
        nameAdmin = findViewById(R.id.nameAdmin);
        emailAdmin = findViewById(R.id.emailAdmin);
        passwordAdmin = findViewById(R.id.passwordAdmin);
        detaiImageAdmin = findViewById(R.id.detailImageAdmin);
        classTV = findViewById(R.id.classSpn);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            quentruycap.setText(bundle.getString(Constants.KEY_STATUS));
            nameAdmin.setText(bundle.getString(Constants.KEY_NAME));
            emailAdmin.setText(bundle.getString(Constants.KEY_EMAIL));
            passwordAdmin.setText(bundle.getString(Constants.KEY_PASSWORD));
            classTV.setText(bundle.getString(Constants.KEY_CLASS));


            String base64Image = bundle.getString(Constants.KEY_IMAGE);
            if(base64Image != null && !base64Image.isEmpty()){
                Bitmap imageBitmap = convertBase64ToBitmap(base64Image);
                if (imageBitmap != null) {
                    detaiImageAdmin.setImageBitmap(imageBitmap);
                }
            }


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("adminclass").document();
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        DataClass dataClass = documentSnapshot.toObject(DataClass.class);
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DetailManagerAdmin.this, "Lỗi Data", Toast.LENGTH_SHORT).show();
                }
            });
        }





    }
    private int getSpinnerPosition(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Giá trị mặc định nếu không tìm thấy
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



}