package com.example.firenotes.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firenotes.MainActivity;
import com.example.firenotes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    EditText mForgotPass;
    Button mchangePasswordBtn;
    TextView mloginHere;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mForgotPass=findViewById(R.id.forgotPass);
        mchangePasswordBtn=findViewById(R.id.changePasswordBtn);
        mloginHere=findViewById(R.id.loginHere);

        fAuth=FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Forget Password");

        mloginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgotPassword.this,Login.class));
            }
        });

        mchangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail=mForgotPass.getText().toString().trim();
                if(mail.isEmpty())
                {
                    Toast.makeText(ForgotPassword.this, "Enter your Mail First", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Send password Recover Email
                    fAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ForgotPassword.this, "Mail is sent, you can recover your password using mail.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgotPassword.this, Login.class));
                                finish();
                            }
                            else {
                                Toast.makeText(ForgotPassword.this, "Email is wrong or Account doesn't exist..", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}