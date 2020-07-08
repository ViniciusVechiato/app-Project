package com.ostap.prog4app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private EditText email, password;
    private Button createBtn, btnBack;
    private TextView security;
    private EditText answer;
    private boolean fileCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.newEmail);
        password = findViewById(R.id.newPwd);
        createBtn = findViewById(R.id.reg);
        btnBack = findViewById(R.id.backButtonRegister);
        answer = findViewById(R.id.answer);
        security = findViewById(R.id.securityQuestion);

        //preset security questions
        final String[] questions = {"What's your name?", "What's the name of your first pet?", "What's the name of your best friend?"};
        int random = new Random().nextInt(questions.length);
        final String question = questions[random];
        security.setText(question);
        //Setting the references from the internal storage on the device
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });

        //creation of account begins here
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailString = email.getText().toString();
                final String passwordString = password.getText().toString();
                final String answerSec = answer.getText().toString();
                //checking if fields are set
                if (emailString.isEmpty()) {
                    email.setError("Please enter valid email");
                    email.requestFocus();
                } else if (passwordString.isEmpty()) {
                    password.setError("Please enter your password");
                    password.requestFocus();
                } else if (emailString.isEmpty() && passwordString.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Both fields are empty!", Toast.LENGTH_SHORT);
                } else if (!(emailString.isEmpty() && passwordString.isEmpty())) {
                    mFirebaseAuth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Add the new user to the firebase database
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                CollectionReference newUser = db.collection("Users");
                                User userNew = new User(emailString,passwordString,question,answerSec);
                                newUser.document(emailString).set(userNew);
                                mFirebaseAuth.signInWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Login Error, try again", Toast.LENGTH_SHORT);
                                        } else {
                                            //Adding informations to the internal storage on the device
                                            editor.putString("email",emailString);
                                            editor.putString("password",passwordString);
                                            editor.putString("question",question);
                                            editor.putString("answer",answerSec);
                                            editor.commit();
                                            //userLogged = new User(emailString, passwordString);
                                            Intent toHome = new Intent(getApplicationContext(), HomeActivity.class);
                                            //toHome.putExtra("user", userLogged);
                                            startActivity(toHome);

                                        }
                                    }
                                });

                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Error Occurred", Toast.LENGTH_SHORT);
                }
            }
        });
    }
}