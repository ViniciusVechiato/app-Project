package com.ostap.prog4app;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.mbms.MbmsErrors;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class viewFile extends AppCompatActivity {

    private WebView webview;
    private Button downloadButton, backButton, deleteButton;
    private TextView fileName;
    private TextView fileSize;
    private TextView fileDate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        //Getting the informations saved on the internal storage of the device
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref",0);
        final SharedPreferences.Editor editor = pref.edit();

        final String email = pref.getString("email",null);
        final File file = (File) getIntent().getSerializableExtra("Clicked");

        //Can use a Webview to directly view the URL of the Firebase Files, saves us a lot of time and effort
        downloadButton = findViewById(R.id.btnDownloadFile);
        backButton = findViewById(R.id.backButtonFileView);
        deleteButton = findViewById(R.id.btnDeleteFile);
        webview = findViewById(R.id.urlView);
        fileName = findViewById(R.id.file_name);
        fileSize = findViewById(R.id.file_size);
        fileDate = findViewById(R.id.file_date);
        webview.loadUrl(file.getUrl()); //check load URL for files

        setMetadata(file);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //retrieve file from firebase to downloads folder
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference storage = FirebaseStorage.getInstance().getReference();
                StorageReference ref = storage.child(email).child(file.getFile_name());
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadFile(file.getFile_name(),file.getUrl(),DIRECTORY_DOCUMENTS);
                    }
                });
            }
        });

        //delete the file and its data from firebase
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                StorageReference storage = FirebaseStorage.getInstance().getReference();
                final StorageReference ref = storage.child(email).child(file.getFile_name());
                String fileObjectName = file.getFile_name();
                db.collection("Users").document(email).collection("Files").document(fileObjectName)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // File deleted successfully
                                        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        });

            }
        });

    }

    public void setMetadata(File file) {

        fileName.setText("File Name: " + file.getFile_name());
        double size = file.getSize();
        double sizeInKB = size/1000;
        double sizeInMB = size/1000000;
        //format file size for text view
        if (sizeInKB < 1) {
            fileSize.setText("Size: " + size + " B");
        }
        else if (sizeInMB < 1){
            fileSize.setText("Size: " + sizeInKB + " Kb");
        }
        else {
            fileSize.setText("Size: " + sizeInMB + " Mb");
        }

        fileDate.setText("Uploaded: " + file.getDate());

    }

    public void downloadFile(String fileName,String url,String destination){
        //Setting the download manager to allow us to download the file on the download folder on the device.
        DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(getApplicationContext().DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        //Doing a request and sending the file to the download folder
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(getApplicationContext(),destination,fileName);
        downloadManager.enqueue(request);
    }
}
