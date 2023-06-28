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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nghincukhoahc.activites.SignUp;
import com.example.nghincukhoahc.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

public class UploadActivity extends AppCompatActivity {

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

    Spinner uploadSpinner,spinnerKhoa;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);



        String[] classArray = getResources().getStringArray(R.array.upload_class_array);
        uploadSpinner = findViewById(R.id.uploadLangSpn);
        spinnerKhoa = findViewById(R.id.spinnerKhoa);
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,classArray);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        uploadSpinner.setAdapter(spinnerAdapter);

        spinnerKhoa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedKhoa = parent.getItemAtPosition(position).toString();
//                if(selectedKhoa.equals("Chọn khoa")){
//                    binding.spinnerClass.setEnabled(false);
//                    binding.spinnerClass.setSelection(0);
//                    Toast.makeText(SignUp.this,"Vui lòng chọn khoa",Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    binding.spinnerClass.setEnabled(true);
//                }
                int lopArrayResId = 0;

                if(selectedKhoa.equals("CNTT")){
                    lopArrayResId = R.array.lop_cntt_array;
                }
                else if(selectedKhoa.equals("KS")){
                    lopArrayResId = R.array.lop_ks_array;
                }

                String[] lopArray = getResources().getStringArray(lopArrayResId);

                // Cập nhật danh sách lớp cho Spinner lớp
                ArrayAdapter<String> lopAdapter = new ArrayAdapter<>(UploadActivity.this, android.R.layout.simple_spinner_item, lopArray);
                uploadSpinner.setAdapter(lopAdapter);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        uploadImage = findViewById(R.id.uploadImage);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadTopic = findViewById(R.id.uploadTopic);
        //uploadLang = findViewById(R.id.uploadLang);
        saveButton = findViewById(R.id.saveButton);

        textViewAddFile = findViewById(R.id.selectedFileTextView);
        textViewAddImage = findViewById(R.id.textAddImage);
        uploadFile = findViewById(R.id.uploadFileButton);
        uploadFileIcon = findViewById(R.id.uploadFileIcon);
        boderUploadFile = findViewById(R.id.borderUploadFile);

        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            textViewAddImage.setVisibility(View.GONE);
                            Intent data = result.getData();
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri);
                        } else {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


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

                                    Glide.with(UploadActivity.this)
                                            .load(imageResource)
                                            .into(uploadFileIcon);
                                } else {
                                    uploadFileIcon.setImageResource(R.drawable.add_btn);
                                }
                            } else {
                                textViewAddFile.setText("");
                                uploadFileIcon.setImageResource(R.drawable.add_btn);
                            }
                        } else {
                            Toast.makeText(UploadActivity.this, "No File Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
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
        String lang = uploadSpinner.getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty() || lang.isEmpty()) {
            Toast.makeText(UploadActivity.this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        int wordCount = title.split("\\s+").length;
        if (wordCount > MAX_TITLE_WORDS) {
            Toast.makeText(UploadActivity.this, "Tiêu đề quá dài, không được quá 40 từ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (imageUri == null && fileUri == null) {
            Toast.makeText(UploadActivity.this, "Vui lòng chọn hình ảnh hoặc tệp tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void saveData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
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
                                    Toast.makeText(UploadActivity.this, "Lỗi tải lên hình ảnh", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UploadActivity.this, "Lỗi tải lên hình ảnh", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(UploadActivity.this, "Lỗi tải lên tệp tin", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UploadActivity.this, "Lỗi tải lên tệp tin", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            uploadData(dialog);
        }
    }

    private void uploadData(AlertDialog dialog) {
        String title = uploadTopic.getText().toString();
        String desc = uploadDesc.getText().toString();
        String lang = uploadSpinner.getSelectedItem().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DataClass dataClass = new DataClass(title, desc, lang, imageURL, fileURL, getCurrentDateTime());
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        db.collection("Bài Viết").document(currentDate)
                .set(dataClass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Toast.makeText(UploadActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(UploadActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
