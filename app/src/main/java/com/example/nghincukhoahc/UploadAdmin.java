package com.example.nghincukhoahc;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nghincukhoahc.activites.DetailManagerAdmin;
import com.example.nghincukhoahc.activites.ManagerAdmin;
import com.example.nghincukhoahc.utilities.Constants;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

public class UploadAdmin extends AppCompatActivity {

    ImageView uploadImage;
    Button saveButton;
    EditText uploadTopic, uploadDesc;
    TextView uploadLang,textViewAddImage;
    String imageURL;
    Uri uri;
    private static final int MAX_TITLE_WORDS = 40;
    ImageView backbutton;
    PreferenceManager preferenceManager;
    String adminClass;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_admin);

        preferenceManager = new PreferenceManager(getApplicationContext());

        backbutton = findViewById(R.id.backButton);

        backbutton.setOnClickListener(v -> {
            onBackPressed();
        });

//        backbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(UploadAdmin.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });

        textViewAddImage = findViewById(R.id.textAddImage);
        uploadImage = findViewById(R.id.uploadImage);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadTopic = findViewById(R.id.uploadTopic);
        uploadLang = findViewById(R.id.uploadLang);
        saveButton = findViewById(R.id.saveButton);

        Intent intent = getIntent();
        String userClass = intent.getStringExtra("class");
        uploadLang.setText(userClass);

        adminClass = preferenceManager.getString(Constants.KEY_CLASS);

        Toast.makeText(UploadAdmin.this,"Class is: "+userClass,Toast.LENGTH_SHORT).show();




        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadImage.setImageURI(uri);
                        } else {
                            Toast.makeText(UploadAdmin.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                textViewAddImage.setVisibility(View.GONE);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid() && isImageSelected()) {
                    saveData();
                }
            }
        });
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String userID = Constants.KEY_USER_ID;
//        db.collection(Constants.KEY_COLLECTION_ADMIN).document(userID)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if(documentSnapshot.exists()){
//                            String userClass = documentSnapshot.getString(Constants.KEY_CLASS);
//                            uploadLang.setText(userClass);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
    }
    private boolean isInputValid(){


        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String lang = uploadLang.getText().toString();
        if(title.isEmpty()||desc.isEmpty()||lang.isEmpty()){
            Toast.makeText(UploadAdmin.this,"Không được để trống",Toast.LENGTH_SHORT).show();
            return false;
        }

        int wordcount = title.length();
        if(wordcount > MAX_TITLE_WORDS){
            Toast.makeText(UploadAdmin.this,"Tiêu đề quá dài,không được quá 40 từ",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean isImageSelected(){
        if(uri == null){
            Toast.makeText(UploadAdmin.this,"Vui lòng chọn hình ảnh",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void saveData(){

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Hình Ảnh")
                .child(uri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadAdmin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                imageURL = urlImage.toString();
                uploadData();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }

    public void uploadData(){
//

//

        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String lang = uploadLang.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();



        DataClass dataClass = new DataClass(title, desc, lang, imageURL,getCurrentDateTime());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        //We are changing the child from title to currentDate,
        // because we will be updating title as well and it may affect child value.



        db.collection("Bài Viết").document(currentDate)
                .set(dataClass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UploadAdmin.this, "Saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadAdmin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private long getCurrentDateTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

}