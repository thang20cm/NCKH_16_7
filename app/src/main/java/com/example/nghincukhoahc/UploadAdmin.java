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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class UploadAdmin extends AppCompatActivity {

    ImageView uploadImage,uploadFileIcon;
    Button uploadFile;
    Button saveButton;
    EditText uploadTopic, uploadDesc;
    TextView uploadLang, textViewAddImage, textViewAddFile;
    String imageURL;
    String fileURL;
    Uri imageUri;
    Uri fileUri;
    LinearLayout boderUploadFile;
    private static final int MAX_TITLE_WORDS = 40;
    ImageView backbutton;
    PreferenceManager preferenceManager;
    String adminClass;

    List<Uri> fileUris = new ArrayList<>();

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

        textViewAddImage = findViewById(R.id.textAddImage);
        uploadImage = findViewById(R.id.uploadImage);

        textViewAddFile = findViewById(R.id.selectedFileTextView);
        uploadFile = findViewById(R.id.uploadFileButton);

        uploadDesc = findViewById(R.id.uploadDesc);
        uploadTopic = findViewById(R.id.uploadTopic);
        uploadLang = findViewById(R.id.uploadLang);
        saveButton = findViewById(R.id.saveButton);

        uploadFileIcon = findViewById(R.id.uploadFileIcon);
        boderUploadFile = findViewById(R.id.borderUploadFile);




        Intent intent = getIntent();
        String userClass = intent.getStringExtra("class");
        uploadLang.setText(userClass);

        adminClass = preferenceManager.getString(Constants.KEY_CLASS);

        Toast.makeText(UploadAdmin.this, "Class is: " + userClass, Toast.LENGTH_SHORT).show();

        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri);
                        } else {
                            Toast.makeText(UploadAdmin.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

//        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            Intent data = result.getData();
//                            fileUri = data.getData();
//                            if (fileUri != null) {
//                                String fileName = getFileNameFromUri(fileUri);
//                                textViewAddFile.setText(fileName);
//                            } else {
//                                textViewAddFile.setText("");
//                            }
//                        } else {
//                            Toast.makeText(UploadAdmin.this, "No File Selected", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//        );
        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            fileUri = data.getData();
                            if (fileUri != null) {

                                String fileName = getFileNameFromUri(fileUri);
                                textViewAddFile.setText(fileName);

                                // Hiển thị biểu tượng hình ảnh tương ứng với loại file
                                String extension = getFileExtension(fileName);
                                int imageResource = getFileImageResource(extension);
                                if (imageResource != 0) {

                                    Glide.with(UploadAdmin.this)
                                            .load(imageResource)
                                            .centerInside()
                                            .into(uploadFileIcon);
                                } else {
                                    uploadFileIcon.setImageResource(R.drawable.every_file);
                                }
                            } else {
                                textViewAddFile.setText("");
                                uploadFileIcon.setImageResource(R.drawable.every_file);
                            }
                        } else {
                            Toast.makeText(UploadAdmin.this, "No File Selected", Toast.LENGTH_SHORT).show();
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
                imagePickerLauncher.launch(photoPicker);
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
                filePicker.setType("*/*");
                filePickerLauncher.launch(filePicker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInputValid()) {
                    saveData();
                }
            }
        });
    }

    private boolean isInputValid() {
        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String lang = uploadLang.getText().toString();

        if (title.isEmpty() || desc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(UploadAdmin.this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        int wordCount = title.split("\\s+").length;
        if (wordCount > MAX_TITLE_WORDS) {
            Toast.makeText(UploadAdmin.this, "Tiêu đề quá dài, không được quá 40 từ", Toast.LENGTH_SHORT).show();
            return false;
        }

//        if (imageUri == null && fileUri == null) {
//            Toast.makeText(UploadAdmin.this, "Vui lòng chọn hình ảnh hoặc tệp tin", Toast.LENGTH_SHORT).show();
//            return false;
//        }

        return true;
    }

    public void saveData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadAdmin.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        uploadImage(dialog);
    }

    private void uploadImage(AlertDialog dialog) {
        if (imageUri != null) {
            StorageReference imageStorageReference = FirebaseStorage.getInstance().getReference().child("Hình Ảnh")
                    .child(imageUri.getLastPathSegment());

            imageStorageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> imageUriTask = taskSnapshot.getStorage().getDownloadUrl();
                            imageUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageURL = uri.toString();
                                    uploadFile(dialog);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(UploadAdmin.this, "Lỗi tải lên hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UploadAdmin.this, "Lỗi tải lên hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            uploadFile(dialog);
        }
    }

    private void uploadFile(AlertDialog dialog) {
        if (fileUri != null) {

            String fileName = getFileNameFromUri(fileUri);
            StorageReference fileStorageReference = FirebaseStorage.getInstance().getReference().child("Tệp Tin").child(fileName);


            fileStorageReference.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> fileUriTask = taskSnapshot.getStorage().getDownloadUrl();
                            fileUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    fileURL = uri.toString();
                                    uploadData(dialog);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(UploadAdmin.this, "Lỗi tải lên tệp tin", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UploadAdmin.this, "Lỗi tải lên tệp tin", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            uploadData(dialog);
        }
    }

    private void uploadData(AlertDialog dialog) {
        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String lang = uploadLang.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DataClass dataClass = new DataClass(title, desc, lang, imageURL, fileURL, getCurrentDateTime());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        db.collection("Bài Viết").document(currentDate)
                .set(dataClass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Toast.makeText(UploadAdmin.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(UploadAdmin.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private long getCurrentDateTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }
        return fileName;
    }
    private String getFileExtension(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }
        return extension;
    }

    private int getFileImageResource(String extension) {
        int imageResource = 0;
        switch (extension) {
            case "pdf":
                imageResource = R.drawable.pdf;
                break;
            case "doc":
            case "docx":
                imageResource = R.drawable.word;
                break;
            case "xls":
            case "xlsx":
                imageResource = R.drawable.xls;
                break;
            case "ppt":
            case "pptx":
                imageResource = R.drawable.ppt;
                break;
            case "txt":
                imageResource = R.drawable.txt;
                break;
            // Thêm các loại file khác và biểu tượng hình ảnh tương ứng ở đây
            default:
                break;
        }
        return imageResource;
    }



}