package com.ostap.prog4app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAccount extends AppCompatActivity {

    private Button logoutBtn;
    private Button backBtn;
    private Button deleteBtn;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        logoutBtn = findViewById(R.id.bt_logout);
        userEmail = findViewById(R.id.tv_emailUser);
        backBtn = findViewById(R.id.backButton);
        deleteBtn = findViewById(R.id.bt_delete);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Getting the references from the internal storage on the device
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref",0);
        final SharedPreferences.Editor editor = pref.edit();

        final String email = pref.getString("email",null);
        final String password = pref.getString("password",null);
        String question = pref.getString("question",null);
        String answer = pref.getString("answer",null);

        userEmail.setText(email);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Deleting the internal storage fields and sign out
                editor.remove("email");
                editor.remove("password");
                editor.remove("question");
                editor.remove("answer");
                editor.commit();
                FirebaseAuth.getInstance().signOut();
                Intent toLogin = new Intent(getApplicationContext(), Login.class);
                startActivity(toLogin);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete the user from the database authentication (One of the databases used by Firebase)
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(email,password);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    //Delete the user from the database
                                    db.collection("Users").document(email).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            editor.remove("email");
                                            editor.remove("password");
                                            editor.remove("question");
                                            editor.remove("answer");
                                            editor.commit();
                                            FirebaseAuth.getInstance().signOut();
                                            Intent toLogin = new Intent(getApplicationContext(), Login.class);
                                            startActivity(toLogin);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });

    }
}
