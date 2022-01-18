package com.example.firenotes;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddNote extends AppCompatActivity {

    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;

    FloatingActionButton fab,fab_main,speechToText;
    Float translationYaxis=100f;
    Boolean menuOpen=false;
    OvershootInterpolator interpolator= new OvershootInterpolator();

    private static final int REQUEST_CODE_SPEECH_INPUT=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore=FirebaseFirestore.getInstance();
        noteTitle=findViewById(R.id.addNoteTitle);
        noteContent=findViewById(R.id.addNoteContent);
        progressBarSave=findViewById(R.id.progressBar);
        speechToText=findViewById(R.id.speechToText);
        user= FirebaseAuth.getInstance().getCurrentUser();

        fab=findViewById(R.id.fab);
        fab_main=findViewById(R.id.fab_main);
        speechToText=findViewById(R.id.speechToText);

        fab.setAlpha(0f);
        speechToText.setAlpha(0f);

        fab.setTranslationY(translationYaxis);
        speechToText.setTranslationY(translationYaxis);

        // Data get from ImageToText Activity
        String desk=getIntent().getStringExtra("key");
        noteContent.setText(desk);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menuOpen)
                {
                    closeMenu();
                }
                else
                {
                    openMenu();
                }
            }
        });


        //For speech To Text
        speechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent speechintent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                speechintent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(speechintent, REQUEST_CODE_SPEECH_INPUT);
                }
                catch (Exception e) {
                    Toast.makeText(AddNote.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle=noteTitle.getText().toString();
                String nContent=noteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty())
                {
                    Toast.makeText(AddNote.this, "Can not save notes with empty fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBarSave.setVisibility(View.VISIBLE);


                //Used To save the note
                DocumentReference docref=fStore.collection("notes").document(user.getUid()).collection("userNotes").document();
                Map<String,Object> note=new HashMap<>();
                note.put("title",nTitle);
                note.put("content",nContent);

                docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNote.this, "Note added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNote.this, "Error..Try again", Toast.LENGTH_SHORT).show();
                        progressBarSave.setVisibility(View.INVISIBLE);

                    }
                });
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // For speech To Text
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                noteContent.setText(Objects.requireNonNull(result).get(0).toString());
            }
        }
    }

    private void openMenu() {
        menuOpen =! menuOpen;
        fab_main.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        fab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        speechToText.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void closeMenu() {
        menuOpen =! menuOpen;
        fab_main.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        fab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        speechToText.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close)
        {
            Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}