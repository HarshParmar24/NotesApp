package com.example.firenotes.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import com.example.firenotes.Splash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText mEmail,mPassword;
    TextView mforgotPass,mcreateAccount;
    Button loginBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail=findViewById(R.id.email);
        mPassword=findViewById(R.id.lPassword);
        loginBtn=findViewById(R.id.loginBtn);
        mforgotPass=findViewById(R.id.forgotPassword);
        mcreateAccount=findViewById(R.id.createAccount);
        spinner=findViewById(R.id.progressBar3);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        user= fAuth.getCurrentUser();


        /*if(user!=null)
        {
            finish();
            startActivity(new Intent(Login.this,MainActivity.class));
        }*/

        showWarning();

        mforgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,ForgotPassword.class));
                finish();
            }
        });

        mcreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,Register.class));
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail=mEmail.getText().toString().trim();
                String pass=mPassword.getText().toString().trim();

                if(mail.isEmpty() || pass.isEmpty())
                {
                    Toast.makeText(Login.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(getApplicationContext(),Login.class));
                    finish();
                    return;
                }

                //spinner.setVisibility(View.VISIBLE);

                //Delete the notes first
                if(fAuth.getCurrentUser() != null && fAuth.getCurrentUser().isAnonymous())
                {
                    FirebaseUser user=fAuth.getCurrentUser();

                    fStore.collection("notes").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "All temporary notes are deleted.", Toast.LENGTH_SHORT).show();

                            //Delete Temporary User Also
                            user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Login.this, "Temporary User Deleted", Toast.LENGTH_SHORT).show();
                                    loginWithCredentials(mail, pass);
                                }
                            });
                        }
                    });

                    //Delete Temporary User Also
                    /*user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "Temporary User Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });*/
                }else{
                    loginWithCredentials(mail, pass);
                }




                /*fAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            checkMailVerification();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Account doesn't exist.", Toast.LENGTH_SHORT).show();
                            spinner.setVisibility(View.GONE);
                        }
                    }
                });*/

            }
        });

    }

    private void loginWithCredentials(String mail, String pass){
        fAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    checkMailVerification();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Account doesn't exist. OR Wrong password entered.", Toast.LENGTH_SHORT).show();
                    spinner.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showWarning() {
        AlertDialog.Builder warning=new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Hey User, Linking the Existing Account will delete all the temporary notes. Create the new Account to save them.")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Its Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // We will do nothing
                    }
                });
        warning.show();
    }

    private void checkMailVerification()
    {
        FirebaseUser firebaseUser=fAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()==true){
            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }
        else
        {
            Toast.makeText(this, "Verify your mail first.", Toast.LENGTH_SHORT).show();
            fAuth.signOut();
            //finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}