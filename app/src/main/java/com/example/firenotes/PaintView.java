package com.example.firenotes;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {

    public int BRUSH_SIZE=10;
    public static final int COLOR_PEN= Color.BLACK;
    public static final int COLOR_ERASER= Color.WHITE;
    public static final int DEFAULT_BG_COLOR= Color.WHITE;
    public static final float TOUCH_TOLERANCE=4;

    private float mX,mY;
    private Path mpath;
    private Paint mPaint;
    private int currentColor;
    private ArrayList<FingerPath> path=new ArrayList<>();

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint=new Paint(Paint.DITHER_FLAG);

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint= new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(COLOR_PEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }

    /*
    public String saveAsImage(Context context){
    //public String saveAsImage(Context context){
            ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        //File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString());
            // Use the compress method on the BitMap object to write image to the OutputStream
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
    */

    public void init(DisplayMetrics metrics)
    {
        int height=metrics.heightPixels;
        int width=metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        mCanvas= new Canvas(mBitmap);

        currentColor = COLOR_PEN;
    }

    public void pen()
    {
        currentColor = COLOR_PEN;
    }

    public void erase()
    {
        currentColor = COLOR_ERASER;
    }

    public void clear()
    {
        path.clear();
        pen();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(DEFAULT_BG_COLOR);

        for(FingerPath fp:path)
        {
            mPaint.setColor(fp.getColor());
            mPaint.setStrokeWidth(fp.getStrokeWidth());
            mPaint.setMaskFilter(null);

            mCanvas.drawPath(fp.getPath(),mPaint);
        }

        canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x,float y)
    {
        mpath=new Path();
        FingerPath fp=new FingerPath(currentColor,BRUSH_SIZE,mpath);
        path.add(fp);

        mpath.reset();
        mpath.moveTo(x,y);
        mX=x;
        mY=y;
    }

    private void touchMove(float x, float y)
    {
        float dx=Math.abs(x-mX);
        float dy=Math.abs(x-mY);

        if(dx >= TOUCH_TOLERANCE || dy>= TOUCH_TOLERANCE)
        {
            mpath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
        }
        mX=x;
        mY=y;
    }

    private void touchUp()
    {
        mpath.lineTo(mX,mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x=event.getX();
        float y=event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}
