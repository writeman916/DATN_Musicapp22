package com.framgia.music_22.screen.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.framgia.music_22.data.model.User;
import com.framgia.music_22.screen.main.MainActivity;
import com.framgia.vnnht.music_22.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextView txtEmail, txtPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

    }

    public void initView(){
        txtEmail = findViewById(R.id.editEmail);
        txtPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFunc();

            }
        });
        mAuth = FirebaseAuth.getInstance();

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void LoginFunc(){
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,"Đăng nhập thành công", Toast.LENGTH_LONG).show();
                            User curuser = parseFireBaseUserTOUser(task.getResult().getUser());

                            SharedPreferences share = getSharedPreferences("MyShare", MODE_PRIVATE);
                            SharedPreferences.Editor editor = share.edit();

                            editor.putString("URName", curuser.getUserName());
                            editor.putString("UREmail", curuser.getEmail());
                            editor.commit();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("UserInfo", curuser);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,"Đăng nhập thất bại", Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }
    private User parseFireBaseUserTOUser(FirebaseUser firebaseUser){
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setUserName(firebaseUser.getDisplayName());
        user.setEmail(firebaseUser.getEmail());

        return user;
    }
}
