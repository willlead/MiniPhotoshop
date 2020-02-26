package com.example.miniphotoshop;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
//
public class MainActivity extends AppCompatActivity {
    final static int SAVE = 1, LOAD = 2;
    private DrawView mDrawView;
    public static float mStrokeWidth = 5;
    public static int mStrokeColor = Color.BLACK;
    public static int mBackColor = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDrawView = new DrawView(this);

        int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        } else {
//            sdcardProcess();
        }
        setContentView(mDrawView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SAVE, 0, "저장하기");
        menu.add(0, LOAD, 0, "불러오기");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SAVE:
                savePicture();
            case LOAD:
                loadPicture();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPicture() {
        
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, 1);

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = dir + "/my.png";
        Bitmap bm = BitmapFactory.decodeFile(filename).copy(Bitmap.Config.ARGB_8888, true);
        Log.d("canvas", dir + "/my.png");
        try {
            Toast.makeText(this, "불러오기 성공", Toast.LENGTH_SHORT).show();
            mDrawView.draw(new Canvas(bm));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data
        );
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                try{
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap bm = BitmapFactory.decodeStream(in);
                    in.close();
                    String path = data.getDataString();
                    Log.i("canvas",path);
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
//                    String fileName = "/"+System.currentTimeMillis()+".png";
//                    mDrawView.draw(new Canvas(bm));
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void savePicture() {


        mDrawView.setDrawingCacheEnabled(true);
        Bitmap screenshot = Bitmap.createBitmap(mDrawView.getDrawingCache());
        mDrawView.setDrawingCacheEnabled(false);

        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!dir.exists())
            dir.mkdirs();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(dir, "my.png"));
            screenshot.compress(CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "저장 성공", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("canvas", "그림저장오류", e);
            Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private static class DrawView extends View implements View.OnTouchListener {
        float x, y;
        public ArrayList<Point> pointList = new ArrayList<MainActivity.DrawView.Point>();
        public DrawView(Context context) {
            super(context);
            setOnTouchListener(this);
            setFocusableInTouchMode(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(mBackColor);
            Paint paint = new Paint();
            if (pointList.size() < 2) return;
            for (int i = 1; i < pointList.size(); i++) {
                if (pointList.get(i).draw) {
                    paint.setColor(pointList.get(i).mStrokeColor);
                    paint.setStrokeWidth(pointList.get(i).mStrokeWidth);
                    canvas.drawLine(pointList.get(i - 1).x,
                            pointList.get(i - 1).y, pointList.get(i).x,
                            pointList.get(i).y, paint);
                }
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            x = event.getX();
            y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("canvas", "ACTION_DOWN");
                    pointList.add(new Point(x, y, false, mStrokeWidth, mStrokeColor));
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    pointList.add(new Point(x, y, true, mStrokeWidth, mStrokeColor));
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d("canvas", "ACTION_UP");
                default:
            }
            return false;
        }//end class DrawView

        static class Point  {
            float x, y;
            boolean draw;
            float mStrokeWidth;
            int mStrokeColor;
            public Point(float x, float y, boolean draw, float mStrokeWidth, int mStrokeColor) {
                this.x = x;
                this.y = y;
                this.draw = draw;
                this.mStrokeColor = mStrokeColor;
                this.mStrokeWidth = mStrokeWidth;
            }
        }//end class Point
    }
}



