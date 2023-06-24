package com.example.nghincukhoahc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivityUser extends AppCompatActivity {
    TextView detailDesc, detailTitle, detailLang,detailDateTime,detailFileAdmin,textViewFile;
    ImageView detailImage,backbutton;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "",fileUrl="";

    Button downloadButton;
    private static final String ACTION_DOWNLOAD_COMPLETE = "com.example.nghincukhoahc.ACTION_DOWNLOAD_COMPLETE";
    BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_DOWNLOAD_COMPLETE)) {
                Toast.makeText(DetailActivityUser.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_user);

        backbutton = findViewById(R.id.backButton);

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
        detailLang = findViewById(R.id.detailLang);
        detailDateTime = findViewById(R.id.detailTime);

        detailFileAdmin = findViewById(R.id.detailFileAdmin);
        downloadButton = findViewById(R.id.downloadButton);
        textViewFile = findViewById(R.id.textViewFile);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(fileUrl);
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailLang.setText(bundle.getString("Language"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);

            fileUrl = bundle.getString("File");

            if(fileUrl != null && !fileUrl.isEmpty()){
                String fileName = getFileNameFromUrl(fileUrl);
                detailFileAdmin.setText(fileName);
                detailFileAdmin.setVisibility(View.VISIBLE);
                downloadButton.setVisibility(View.VISIBLE);
            }
            else {
                detailFileAdmin.setVisibility(View.GONE);
                downloadButton.setVisibility(View.GONE);
                textViewFile.setVisibility(View.GONE);
            }


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("Bài Viết").document(key);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        DataClass dataClass = documentSnapshot.toObject(DataClass.class);
                        if (dataClass != null) {
                            long datetime = dataClass.getDateTime();
                            String formattedDateTime = convertTimestampToDateTime(datetime);
                            detailDateTime.setText(formattedDateTime);
                        }
                    }
                }

                private String convertTimestampToDateTime(long timestamp) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date(timestamp);
                    return sdf.format(date);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DetailActivityUser.this, "Lỗi Data", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

            LocalBroadcastManager.getInstance(DetailActivityUser.this).sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));


            Toast.makeText(DetailActivityUser.this, "Downloading...", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(DetailActivityUser.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadCompleteReceiver);
    }

}