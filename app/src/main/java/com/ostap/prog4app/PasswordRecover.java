package com.ostap.prog4app;

import android.content.Intent;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PasswordRecover extends AppCompatActivity {

    private EditText email;
    private TextView question;
    private EditText answer;
    private Button submit;
    private Button changePass;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recover);

        email = findViewById(R.id.et_email);
        question = findViewById(R.id.questionText);
        answer = findViewById(R.id.et_answer);
        submit = findViewById(R.id.bt_submit);
        changePass = findViewById(R.id.bt_change);

        question.setVisibility(View.INVISIBLE);
        answer.setVisibility(View.INVISIBLE);
        changePass.setVisibility(View.INVISIBLE);


        db = FirebaseFirestore.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();
                if(!emailString.isEmpty()){
                    //We first verify if the user exists on our database
                    DocumentReference docRef = db.collection("Users").document(emailString);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            //Change the design of the page to display the security question and allow the user to answer.
                            question.setVisibility(View.VISIBLE);
                            question.setText(user.getSecurityQuestion());
                            answer.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.INVISIBLE);
                            changePass.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String answerString = answer.getText().toString();
                String emailString = email.getText().toString();
                if (!answerString.isEmpty()){
                    //We check on our database if the answer for the security question is correct.
                    DocumentReference docRef = db.collection("Users").document(emailString);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            if (answerString.equals(user.getAnswer())){
                                //If the answer is correct, then the user is taken to another page to set a new password.
                                Intent intent = new Intent(getApplicationContext(),ChangePassword.class);
                                intent.putExtra("user",user);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }
}
