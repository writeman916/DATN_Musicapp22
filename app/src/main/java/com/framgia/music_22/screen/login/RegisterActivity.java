package com.framgia.music_22.screen.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.framgia.vnnht.music_22.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import io.opencensus.tags.Tag;

public class RegisterActivity extends AppCompatActivity {


    private EditText txtEmail, txtPassword, txtPasswordConfim, txtUsername;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    public void initView(){
        txtEmail = findViewById(R.id.editEmail);
        txtUsername = findViewById(R.id.editUsername);
        txtPassword = findViewById(R.id.editPassword);
        txtPasswordConfim = findViewById(R.id.editPasswordConfirm);

        btnRegister = findViewById(R.id.btnRegister2);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });


        mAuth = FirebaseAuth.getInstance();
    }
    public void Register(){
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String passwordConfirm = txtPasswordConfim.getText().toString();
        if(password.equals(passwordConfirm)){

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this,"Sign Up Success", Toast.LENGTH_LONG).show();
                                addUserNameToUser(task.getResult().getUser());
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(RegisterActivity.this,"Registration failed!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }else
        {
            Toast.makeText(this, "Password confirm not match, Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserNameToUser(FirebaseUser user) {
        String username = txtUsername.getText().toString();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });
    }

}
