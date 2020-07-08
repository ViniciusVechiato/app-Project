package com.ostap.prog4app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePassword extends AppCompatActivity {

    private EditText newPassword;
    private Button setPass;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPassword = findViewById(R.id.et_password);
        setPass = findViewById(R.id.bt_setpass);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        db = FirebaseFirestore.getInstance();
        final User user = (User) getIntent().getExtras().getSerializable("user");

        setPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pass = newPassword.getText().toString();
                if (!pass.isEmpty()){
                    //First finds the user on the database, then updates the password
                    DocumentReference documentReference = db.collection("Users").document(user.getEmail());
                    documentReference.update("password",pass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Updates the internal storage information
                            editor.putString("email",user.getEmail());
                            editor.putString("password",pass);
                            editor.commit();
                            //userLogged = new User(emailString, passwordString);
                            Intent toHome = new Intent(getApplicationContext(), HomeActivity.class);
                            //toHome.putExtra("user", userLogged);
                            startActivity(toHome);
                        }
                    });
                }
            }
        });

    }
}
