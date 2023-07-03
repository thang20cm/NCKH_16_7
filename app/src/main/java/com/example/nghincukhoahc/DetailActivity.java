package com.example.nghincukhoahc;


import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle, detailLang,detailDateTime,detailFileAdmin,textViewFile;
    ImageView detailImage,backbutton,uploadFileIcon;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "",fileUrl="";
    Button downloadButton;
    LinearLayout borderUploadFile;

    private static final String ACTION_DOWNLOAD_COMPLETE = "com.example.nghincukhoahc.ACTION_DOWNLOAD_COMPLETE";
    BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_DOWNLOAD_COMPLETE)) {
                Toast.makeText(DetailActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };





    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        IntentFilter downloadCompleteIntentFilter = new IntentFilter(ACTION_DOWNLOAD_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);



        backbutton = findViewById(R.id.backButton);
    backbutton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    });

        detailDesc = findViewById(R.id.detailDesc);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        detailLang = findViewById(R.id.detailLang);
        detailDateTime = findViewById(R.id.detailTime);
        textViewFile = findViewById(R.id.textViewFile);

        detailFileAdmin = findViewById(R.id.detailFileAdmin);
        downloadButton = findViewById(R.id.downloadButton);

        uploadFileIcon = findViewById(R.id.uploadFileIcon);
        borderUploadFile = findViewById(R.id.borderUploadFile);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(fileUrl);
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailLang.setText(bundle.getString("Language"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);

            fileUrl = bundle.getString("File");
            if (fileUrl != null && !fileUrl.isEmpty()) {
                String fileName = getFileNameFromUrl(fileUrl);
                detailFileAdmin.setText(fileName);
                detailFileAdmin.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.VISIBLE);

                String fileExtension = getFileExtension(fileName);
                int fileIconResId = getFileIconResourceId(fileExtension);
                if (fileIconResId != 0) {
                    uploadFileIcon.setImageResource(fileIconResId);
                }
                uploadFileIcon.setVisibility(View.VISIBLE);
                borderUploadFile.setVisibility(View.VISIBLE);
            } else {
                detailFileAdmin.setVisibility(View.GONE);
                downloadButton.setVisibility(View.GONE);
                textViewFile.setVisibility(View.GONE);
                uploadFileIcon.setVisibility(View.GONE);
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("Bài Viết").document(key);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        DataClass dataClass = documentSnapshot.toObject(DataClass.class);
                        if(dataClass != null){
                            long datetime = dataClass.getDateTime();
                            String formattedDateTime = convertTimestampToDateTime(datetime);
                            detailDateTime.setText(formattedDateTime);
                        }
                    }
                }
                private String convertTimestampToDateTime(long timestamp){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date(timestamp);
                    return sdf.format(date);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DetailActivity.this, "Lỗi Data", Toast.LENGTH_SHORT).show();
                }
            });
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               FirebaseFirestore db = FirebaseFirestore.getInstance();
               db.collection("Bài Viết").document(key)
                       .delete()
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void unused) {
                               Intent intent = new Intent(DetailActivity.this, MainActivitySuperAdmin.class);
                               startActivity(intent);
                               finish();
                               deleteImageAndFinish();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(DetailActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                           }
                       });
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//
//                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
//                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        reference.child(key).removeValue();
//                        Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        finish();
//                    }
//                });
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                        .putExtra("Title", detailTitle.getText().toString())
                        .putExtra("Description", detailDesc.getText().toString())
                        .putExtra("Language", detailLang.getText().toString())
                        .putExtra("Image", imageUrl)
                        .putExtra("Key", key);
                startActivity(intent);
            }
        });
    }
    private void deleteImageAndFinish() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileNameFromUrl(String url) {
        String decodedUrl = Uri.decode(url);
        int startIndex = decodedUrl.lastIndexOf("/") + 1;
        int endIndex = decodedUrl.indexOf("?alt=media");
        if (endIndex == -1) {
            endIndex = decodedUrl.length();
        }
        String fileNameWithExtension = decodedUrl.substring(startIndex, endIndex);
        int extensionIndex = fileNameWithExtension.lastIndexOf(".");
        String fileName;
        if (extensionIndex != -1) {
            fileName = fileNameWithExtension.substring(0, extensionIndex);
        } else {
            fileName = fileNameWithExtension;
        }

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (fileExtension != null) {
            return fileName + "." + fileExtension;
        } else {
            return fileName;
        }
    }

    private void downloadFile(String fileUrl) {
        try {
            Uri uri = Uri.parse(fileUrl);
            String fileName = getFileNameFromUrl(fileUrl);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            LocalBroadcastManager.getInstance(DetailActivity.this).sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));


            Toast.makeText(DetailActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(DetailActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadCompleteReceiver);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private int getFileIconResourceId(String fileExtension) {
        switch (fileExtension) {
            case "pdf":
                return R.drawable.pdf;
            case "doc":
            case "docx":
                return R.drawable.word;
            case "xls":
            case "xlsx":
                return R.drawable.xls;
            case "ppt":
            case "pptx":
                return R.drawable.ppt;
            case "txt":
                return R.drawable.txt;
            // Thêm các loại tệp tin khác tương ứng ở đây
            default:
                return R.drawable.txt;
        }
    }




}
