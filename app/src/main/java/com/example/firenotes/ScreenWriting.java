package com.example.firenotes;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class ScreenWriting extends AppCompatActivity {

    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_writing);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        paintView=findViewById(R.id.paint_view);
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.paint_option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.pen:
                paintView.pen();
                Toast.makeText(this, "Pen Active", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.eraser:
                paintView.erase();
                Toast.makeText(this, "Eraser Active", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.clear:
                paintView.clear();
                Toast.makeText(this, "Data is clear.. Canvas is empty", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.save_canvas:

                saveImage();

                //saveCanvasToGallery();
                //String imagePath = paintView.saveAsImage(getApplicationContext());
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {
        Bitmap bitmap = getBitmapFromView(paintView);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);

        File direct = new File(Environment.getExternalStorageDirectory() + "/MyPaintApp");


        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/MyPaintApp/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/sdcard/MyPaintApp/", "harsh.png");
        if (file.exists()) {
            file.delete();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            //     File file=new File(this.getExternalCacheDir(),y);
            FileOutputStream fout = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            String url = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                    "Wallpaper.jpg", null);
            Toast.makeText(this, "Image saved in gallery...", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("ResourceAsColor")
    private Bitmap getBitmapFromView(View view) {
        Bitmap returnBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(android.R.color.white);
        }
        view.draw(canvas);
        return returnBitmap;
    }

    /*
    private void saveCanvasToGallery() {
        AlertDialog.Builder saveDialog= new AlertDialog.Builder(this);
        saveDialog.setTitle("Save?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                paintView.saveHierarchyState();
            }
        });
        saveDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        saveDialog.show();
    }

     */


}