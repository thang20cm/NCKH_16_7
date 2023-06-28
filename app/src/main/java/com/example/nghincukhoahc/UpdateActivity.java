package com.example.nghincukhoahc;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class UpdateActivity extends AppCompatActivity {

    TextView textViewAddAddImage;
    ImageView updateImage,backbtn;
    Button updateButton;
    EditText updateDesc, updateTitle;
    String title, desc, lang;
    String imageUrl,fileUrl;
    String key, oldImageURL;
    Uri uri;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;

    boolean isImageSelected = false;

    private ArrayAdapter<String> spinnerAdapter;
    private String[] classArray;

    private boolean isDataChanged = false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateImage = findViewById(R.id.updateImage);
        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateDesc);
        updateTitle = findViewById(R.id.updateTitle);
        textViewAddAddImage = findViewById(R.id.textAddImage);
        backbtn = findViewById(R.id.backButton);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        classArray = getResources().getStringArray(R.array.upload_class_array);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classArray);

        Spinner updateLangSpinner = findViewById(R.id.updateLangSpn);
        updateLangSpinner.setAdapter(spinnerAdapter);



        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            textViewAddAddImage.setVisibility(View.GONE);
                            Intent data = result.getData();
                            uri = data.getData();
                            Glide.with(UpdateActivity.this).load(uri).into(updateImage);
                            isImageSelected = true;
                        } else {
                            Toast.makeText(UpdateActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Glide.with(UpdateActivity.this).load(bundle.getString("Image")).into(updateImage);
            updateTitle.setText(bundle.getString("Title"));
            updateDesc.setText(bundle.getString("Description"));
            key = bundle.getString("Key");
            oldImageURL = bundle.getString("Image");

            String currentDataLang = bundle.getString("Language");
            int selectionPosition = spinnerAdapter.getPosition(currentDataLang);
            if (selectionPosition != -1) {
                updateLangSpinner.setSelection(selectionPosition);
            }
        }

        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
                isImageSelected = true;
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid()) {
                    saveData();
                }
            }
        });
    }

    private boolean isInputValid() {
        title = updateTitle.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        lang = ((Spinner) findViewById(R.id.updateLangSpn)).getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Kiểm tra xem liệu dữ liệu đã thay đổi hay chưa
        if (title.equals(getIntent().getStringExtra("Title")) &&
                desc.equals(getIntent().getStringExtra("Description")) &&
                lang.equals(getIntent().getStringExtra("Language")) &&
                !isImageSelected) {
            Toast.makeText(UpdateActivity.this, "Không có sự thay đổi trong dữ liệu", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void saveData() {


        documentReference = firestore.collection("Bài Viết").document(key);

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (isImageSelected) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Hình Ảnh").child(uri.getLastPathSegment());
            storageReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    updateData(dialog);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UpdateActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            imageUrl = oldImageURL;
            updateData(dialog);
        }
    }

    public void updateData(AlertDialog dialog) {
        title = updateTitle.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        lang = ((Spinner) findViewById(R.id.updateLangSpn)).getSelectedItem().toString();

        DataClass dataClass = new DataClass(title, desc, lang, imageUrl,fileUrl, getCurrentDateTime());

        documentReference.set(dataClass, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (isImageSelected) {
                                StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL);
                                reference.delete();
                            }
                            Toast.makeText(UpdateActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UpdateActivity.this, MainActivitySuperAdmin.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(UpdateActivity.this, "Update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    private long getCurrentDateTime() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
