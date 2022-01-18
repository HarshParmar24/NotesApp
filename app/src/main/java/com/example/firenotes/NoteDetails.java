package com.example.firenotes;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class NoteDetails extends AppCompatActivity {
    Intent data;

    FloatingActionButton fab,fab_main,textToSpeak,downloadPdf;
    Float translationYaxis=100f;
    Boolean menuOpen=false;
    OvershootInterpolator interpolator= new OvershootInterpolator();

    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Back button on NoteDetails
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab_main=findViewById(R.id.fab_main);
        fab=findViewById(R.id.fab2);
        textToSpeak=findViewById(R.id.textToSpeak);
        downloadPdf=findViewById(R.id.downloadPdf);

        fab.setAlpha(0f);
        textToSpeak.setAlpha(0f);
        downloadPdf.setAlpha(0f);

        fab.setTranslationY(translationYaxis);
        textToSpeak.setTranslationY(translationYaxis);
        downloadPdf.setTranslationY(translationYaxis);

        // Get data from MainActivity as Intent
        data=getIntent();

        TextView title=findViewById(R.id.noteDetailsTitle);
        TextView content=findViewById(R.id.noteDetailsContent);
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));

        //set background color as same as random color
        content.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));


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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(view.getContext(),EditNote.class);
                i.putExtra("title",data.getStringExtra("title"));
                i.putExtra("content",data.getStringExtra("content"));
                i.putExtra("noteId",data.getStringExtra("noteId"));
                startActivity(i);
                finish();
            }
        });


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS)
                {
                    //Selecting language
                    int language = textToSpeech.setLanguage(Locale.ENGLISH);

                }
            }
        });
        textToSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string= content.getText().toString();
                Toast.makeText(NoteDetails.this, "Text to speech is running perfectly.Just listen the sounds play..", Toast.LENGTH_SHORT).show();
                //Text will be converted into speech
                int speech=textToSpeech.speak(string,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

        downloadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleT=title.getText().toString();
                String contentT=content.getText().toString();

                String path= getExternalFilesDir(null).toString() + "/" + titleT + ".pdf";
                File file=new File(path);
                if(!file.exists())
                {
                    try{
                        file.createNewFile();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    Document document=new Document(PageSize.A4);
                    try {
                        PdfWriter.getInstance(document,new FileOutputStream(file.getAbsoluteFile()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    document.open();
                    try {
                        document.add(new Paragraph(titleT));
                        document.add(new Paragraph("\n"));
                        document.add(new Paragraph(contentT));
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(NoteDetails.this, "Exported Successfully", Toast.LENGTH_SHORT).show();
                    document.close();

                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.share_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Use to go home page while clicking Back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }

        if(item.getItemId() == R.id.share_menu)
        {
            // Get the data
            TextView title=findViewById(R.id.noteDetailsTitle);
            TextView content=findViewById(R.id.noteDetailsContent);

            content.setText(data.getStringExtra("content"));
            title.setText(data.getStringExtra("title"));

            String titleT=title.getText().toString();
            String contentT=content.getText().toString();

            //send the data to perticular application
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "*"+ titleT + "*"+ "\n" + contentT);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void openMenu() {
        menuOpen =! menuOpen;
        fab_main.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        fab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        downloadPdf.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        textToSpeak.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
    }

    private void closeMenu() {
        menuOpen =! menuOpen;
        fab_main.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        fab.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        downloadPdf.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        textToSpeak.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }
}