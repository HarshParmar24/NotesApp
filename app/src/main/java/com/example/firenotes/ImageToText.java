package com.example.firenotes;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class ImageToText extends AppCompatActivity {

    private Button captureImgBtn,detectTextBtn;
    private ImageView imageView;
    private TextView textView;
    Bitmap imageBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_text);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image To Text");

        captureImgBtn=findViewById(R.id.capture_image_btn);
        detectTextBtn=findViewById(R.id.detect_text_image_btn);
        imageView=findViewById(R.id.image_view);
        textView=findViewById(R.id.text_display);


        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                textView.setText("");
            }
        });

        detectTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTextFromImage();
            }
        });

    }




    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            //takePictureIntent will call onActivityResult internally
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    //This method will get captured image as a small Bitmap named as imageBitmap under key "data" and after that it will display photo on imageView
    //We have given our imageView name as "imageView"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            //will take picture into imageBitmap
            imageBitmap = (Bitmap) extras.get("data");
            //it will display photo on imageView , our imageView name is also "imageView"
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void detectTextFromImage()
    {
        FirebaseVisionImage firebaseVisionImage= FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector= FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageToText.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error ", e.getMessage());
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText)
    {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if(blockList.size() == 0)
        {
            Toast.makeText(this, "No text found in the image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(FirebaseVisionText.Block block: firebaseVisionText.getBlocks())
            {
                String text=block.getText();
                textView.setText(text);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.save_from_image,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        if(item.getItemId() == R.id.save_from_image_menu)
        {
            Intent intent=new Intent(ImageToText.this,AddNote.class);
            intent.putExtra("key",textView.getText().toString());
            startActivity(intent);
            finish();
            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}