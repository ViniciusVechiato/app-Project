package com.ostap.prog4app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.Map;

public class Login extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseAuth mFirebaseAuth;
    EditText email, password;
    Button signinBtn, registerBtn;
    private FirebaseFirestore db;
    private TextView forgot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        //We first check if there is any information on the internal storage.
        //If there is, we automatically log the user
        if(pref.contains("email") && pref.contains("password")){
            Intent toHome = new Intent(Login.this, HomeActivity.class);
            startActivity(toHome);
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signinBtn = findViewById(R.id.signinBtn);

        db = FirebaseFirestore.getInstance();

        registerBtn = findViewById(R.id.registerBtn);
        forgot = findViewById(R.id.bt_forgot);


        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PasswordRecover.class);
                startActivity(intent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, RegisterActivity.class));
            }
        });


        //Old function to check if the user is logged using the Firebase Authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null){
                    Toast.makeText(Login.this, "You are logged in", Toast.LENGTH_SHORT);
                    Intent i = new Intent(Login.this,HomeActivity.class);
                    startActivity(i);
                }
                else{
                    Toast.makeText(Login.this, "Please log in", Toast.LENGTH_SHORT);
                }
            }
        };

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailString = email.getText().toString();
                final String passwordString = password.getText().toString();

                //ensures fields are set
                if(emailString.isEmpty()){
                    email.setError("Please enter valid email");
                    email.requestFocus();
                }
                else if(passwordString.isEmpty()){
                    password.setError("Please enter your password");
                    password.requestFocus();
                }
                else if(emailString.isEmpty() && passwordString.isEmpty()){
                    Toast.makeText(Login.this,"Both fields are empty!",Toast.LENGTH_SHORT);
                }
                else if(!(emailString.isEmpty() && passwordString.isEmpty())){
                    mFirebaseAuth.signInWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login Error, try again", Toast.LENGTH_SHORT);
                            } else {
                                //Set the information on the internal storage
                                editor.putString("email",emailString);
                                editor.putString("password",passwordString);
                                editor.commit();
                                //userLogged = new User(emailString, passwordString);
                                Intent toHome = new Intent(getApplicationContext(), HomeActivity.class);
                                //toHome.putExtra("user", userLogged);
                                startActivity(toHome);

                            }
                        }
                    });
                }

            }
        });
    }

    /*@Override
    protected void onStart(){
        super.onStart();
    }*/
}
