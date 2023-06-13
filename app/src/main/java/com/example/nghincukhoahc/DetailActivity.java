package com.example.nghincukhoahc;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    TextView detailDesc, detailTitle, detailLang,detailDateTime;
    ImageView detailImage,backbutton;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



//        backButton = findViewById(R.id.backButton);
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DetailActivity.this,MainActivity.class);
//                startActivity(intent);
//                 // Đóng hoạt động hiện tại và quay lại MainActivity
//            }
//        });


//        button = findViewById(R.id.back);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(DetailActivity.this,MainActivity.class));
//            }
//        });

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("Description"));
            detailTitle.setText(bundle.getString("Title"));
            detailLang.setText(bundle.getString("Language"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("Image");
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Bài Viết").child(key);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        DataClass dataClass = dataSnapshot.getValue(DataClass.class);
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

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailActivity.this, "Lỗi Data", Toast.LENGTH_SHORT).show();
                }
            });
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Bài Viết");
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            DataClass dataClass = dataSnapshot.getValue(DataClass.class);
//                            if (dataClass != null) {
//                                long dateTime = dataClass.getDateTime();
//                                String formattedDateTime = convertTimestampToDateTime(dateTime);
//                                detailDateTime.setText(formattedDateTime);
//                            }
//                        }
//                    }
//                    private String convertTimestampToDateTime(long timestamp) {
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                        Date date = new Date(timestamp);
//
//                        return sdf.format(date);
//                    }
//
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        Toast.makeText(DetailActivity.this, "Lổi Data", Toast.LENGTH_SHORT).show();
//                    }
//                });



                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
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
}