package com.example.firenotes.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firenotes.MainActivity;
import com.example.firenotes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    EditText mUserName,mUserEmail,mUserPass,mUserConfPass;
    Button syncAccount;
    TextView loginAct;
    ProgressBar progressBar;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserName=findViewById(R.id.userName);
        mUserEmail=findViewById(R.id.userEmail);
        mUserPass=findViewById(R.id.password);
        mUserConfPass=findViewById(R.id.confirmPassword);

        syncAccount=findViewById(R.id.createAccount);
        loginAct=findViewById(R.id.login);
        progressBar=findViewById(R.id.progressBar4);

        fAuth=FirebaseAuth.getInstance();

        loginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this,Login.class));
                finish();
            }
        });

        syncAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uUserName=mUserName.getText().toString();
                String uUserEmail=mUserEmail.getText().toString();
                String uUserPass=mUserPass.getText().toString();
                String uUserConfPass=mUserConfPass.getText().toString();

                if(uUserName.isEmpty() || uUserEmail.isEmpty() || uUserPass.isEmpty() || uUserConfPass.isEmpty())
                {
                    Toast.makeText(Register.this, "Filling all fields are compulsary.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!uUserPass.equals(uUserConfPass))
                {
                    mUserConfPass.setError("Password doesn't match");
                    //return;
                }

                progressBar.setVisibility(View.VISIBLE);

                AuthCredential credential= EmailAuthProvider.getCredential(uUserEmail,uUserPass);


                fAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(Register.this, "Registration Successsful", Toast.LENGTH_SHORT).show();
                            sendEmailVerification();

                            //When new User is created
                            //We are saving userName in userProfile object
                            FirebaseUser usr=fAuth.getCurrentUser();
                            UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                                    .setDisplayName(uUserName)
                                    .build();
                            usr.updateProfile(request);
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }
                        else {
                            Toast.makeText(Register.this, "Failed to register Or user have already an account..", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                });


                        /*addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Register.this, "Notes are synced", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Oops, Failed. Try Again.", Toast.LENGTH_SHORT).show();
                    }
                }); */
            }
        });

    }

    private void sendEmailVerification() {
        FirebaseUser user=fAuth.getCurrentUser();
        if(user!=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(Register.this, "Verification Email is sent on you mail ID, verify and login again", Toast.LENGTH_SHORT).show();
                    fAuth.signOut();
                    startActivity(new Intent(Register.this,Login.class));
                    finish();
                }
            });
        }
        else {
            Toast.makeText(this, "Failed to send Verification Email.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}