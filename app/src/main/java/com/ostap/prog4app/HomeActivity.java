package com.ostap.prog4app;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1beta1.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity{
    private Button logoutBtn;
    private Button uploadBtn;
    private FirebaseAuth mFirebaseAuth;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseFirestore db;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Button config;


    private ArrayList<File> dataset = new ArrayList<File>();


    User userLogged;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult (int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        //Verify if the request for the file is correct
        if (requestCode == 1 && resultCode == RESULT_OK){
            //If statement to chek if the user selected multiple files
            if(data.getClipData() != null){
                int total = data.getClipData().getItemCount();
                for(int i = 0; i < total; i++){
                    //Get the uri (specific path) for the selected file
                    final Uri fileSelected = data.getClipData().getItemAt(i).getUri();
                    fileUpload(fileSelected);

                }
            }
            else{
                Uri fileSelected = data.getData();
                fileUpload(fileSelected);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Get the information on the internal storage of the device
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref",0);
        final SharedPreferences.Editor editor = pref.edit();

        String email = pref.getString("email",null);
        String password = pref.getString("password",null);
        String question = pref.getString("question",null);
        String answer = pref.getString("answer",null);


        uploadBtn = findViewById(R.id.bt_upload);
        config = findViewById(R.id.bt_config);
        //test = findViewById(R.id.tv_test);

        db = FirebaseFirestore.getInstance();

        //Intent home = getIntent();
        userLogged = new User(email,password,question,answer);

        //Query to get all files on the database that belongs to the logged user
        db.collection("Users").document(email).collection("Files").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        //DocumentSnapshot is a snapshot of the document with all informations about it
                        File newFile = documentSnapshot.toObject(File.class);
                        Log.d(null, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                        dataset.add(newFile);
                    }
                    populateRecyclerView();
                }
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goes to the folder setted
                Intent intent = new Intent();
                intent.setType("*/*");#
                //Allow the selection of multiple files
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
            }
        });


        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),UserAccount.class);
                startActivity(intent);
            }
        });
    }
    public void populateRecyclerView(){
        //Code for recyclerView to display the files
        recyclerView = findViewById(R.id.allFilesDisplay);
        //reset view by deleting all children, then repopulating with all dataset info
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        //populate with data from firebase database
        //need to retrieve data from firebase to do this

        adapter = new listAdapter(dataset);
        recyclerView.setAdapter(adapter);

    }

    public void fileUpload(final Uri fileSelected){
        final String path = fileSelected.getPath();
        final String fileName = path.substring(path.lastIndexOf('/')+1);
        final String[] url = new String[1];
        final long[] size = new long[1];
        final String[] date = new String[1];

        //First query to check if the file is already uploaded, using the file name
        CollectionReference database = db.collection("Users").document(userLogged.getEmail()).collection("Files");
        final Query query = database.whereEqualTo("file_name",fileName);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()) {
                    //If the result is null, then we can save the file on the database
                    //First we save the file on the Storage to get the download URL
                    StorageReference folder = FirebaseStorage.getInstance().getReference();
                    final StorageReference file = folder.child(userLogged.getEmail()).child(fileName);
                    file.putFile(fileSelected).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            file.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(final StorageMetadata storageMetadata) {
                                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //Second, we save the file information on the database (Firestore)
                                            url[0] = uri.toString();
                                            size[0] = storageMetadata.getSizeBytes();
                                            long millis = storageMetadata.getUpdatedTimeMillis();
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTimeInMillis(millis);
                                            date[0] = dateFormat.format(calendar.getTime());
                                            //getApplicationContext().getSharedPreferences("UserPref", 0).getString("email", null) gets the email of the logged user
                                            File fileToUpload = new File(fileName, url[0], getApplicationContext().getSharedPreferences("UserPref", 0).getString("email", null), size[0], date[0]);
                                            dataset.add(fileToUpload); //copies the newly uploaded file to the dataset
                                            CollectionReference files = db.collection("Users").document(userLogged.getEmail()).collection("Files");
                                            files.document(fileName).set(fileToUpload);
                                            Toast.makeText(HomeActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                            //add code to update recyclerView on successful file upload to display new file in the dataset
                                            populateRecyclerView();
                                        }
                                    });
                                }
                            });

                        }
                    });
                }

                else{
                    /*AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
                    alert.setTitle("File Already Uploaded");
                    alert.setMessage("This file is already uploaded");

                    alert.create();
                    alert.show();*/
                    Toast.makeText(HomeActivity.this, "File Already Uploaded", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}