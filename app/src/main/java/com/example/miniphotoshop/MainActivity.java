package com.example.miniphotoshop;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import static java.lang.System.exit;

//
public class MainActivity extends AppCompatActivity {

    final static int SAVE = 0, LOAD = 1, POST_SUCCESS = 2,  POST_FAILURE = 3;
    private final String SERVER_URL = "http://jang.anymobi.kr/android/myinfo.php";

    private DrawView mDrawView;
    public static float mStrokeWidth = 5;
    public static int mStrokeColor = Color.BLACK;
    public static int mBackColor = Color.WHITE;
    boolean isLoad = false;
    String myName = "윤성렬";
    int myAge = 34;
    String filename;

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
        menu.add(0, SAVE, 0, "Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, LOAD, 0, "Load").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, POST_SUCCESS, 0, "Post_Success");
        menu.add(0, POST_FAILURE, 0, "Post_Failure");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SAVE:
                savePicture();
                break;
            case LOAD:
                loadPicture();
                break;
            case POST_SUCCESS:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postData(true);
                    }
                }).start();
                break;
            case POST_FAILURE:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postData(false);
                    }
                }).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    void postData(boolean isSuccess) {
        String requestPOST;
        if (isSuccess) {
            requestPOST = SERVER_URL + "?name=" + myName + "&" + "age=" + myAge;
        } else {
            requestPOST = SERVER_URL + "?name=" + myName;//+ "&" + "age=" + myAge;}
        }
        URL url = null;
        BufferedReader input = null;
        String line = "";

        try {
            url = new URL(requestPOST);
            InputStreamReader isr = new InputStreamReader(url.openStream());
            input = new BufferedReader(isr);

            while ((line = input.readLine()) != null) {
                System.out.println(line);
                Log.d("Received Data", line);
                final String finalLine = line;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = new JSONObject(finalLine);
                            JSONObject response = jsonObject.getJSONObject("response");
                            String result = response.getString("action_result");
                            String reason = response.getString("action_failure_reason");
                            if (result.equals("failure")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("실패")
                                        .setMessage(reason);
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        exit(0);
//                                        finish();
//                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                                return;
                            } else {
                                JSONObject content = jsonObject.getJSONObject("content");
                                String name = content.getString("name");
                                String age = content.getString("age");
                                Toast.makeText(MainActivity.this, "Name: " + name + " Age: " + age, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadPicture() {
        isLoad = true;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        filename = dir + "/my.png";
        Bitmap bm = BitmapFactory.decodeFile(filename).copy(Bitmap.Config.ARGB_8888, true);
        Log.d("canvas", dir + "/my.png");
        try {
            Toast.makeText(this, "불러오기 성공", Toast.LENGTH_SHORT).show();
            mDrawView.pointList.clear();
            mDrawView.setFilename(filename);
            mDrawView.isLoadBitmap = true;
            mDrawView.invalidate();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data
        );
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap bm = BitmapFactory.decodeStream(in);
                    in.close();
                    String path = data.getDataString();
                    Log.i("canvas", path);
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static class DrawView extends View implements View.OnTouchListener {

        float x, y;
        boolean isLoadBitmap = false;
        public ArrayList<Point> pointList = new ArrayList<MainActivity.DrawView.Point>();
        private String filename;
        Bitmap bm;


        public DrawView(Context context) {
            super(context);
            setOnTouchListener(this);
            setFocusableInTouchMode(true);
        }

        public void setFilename(String filename){
            this.filename = filename;
        }

        void loadBitmap(Canvas canvas){
            try {
                if(filename == null) return;
                Bitmap bm = BitmapFactory.decodeFile(filename);
                if(bm == null) {
                    Log.i("canvas","bm is null"+ filename);
                    return;
                }
                this.bm = bm;
                canvas.drawBitmap(bm, 0, 0, null);
            } catch (Exception e){
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(mBackColor);
            Paint paint = new Paint();

            if(isLoadBitmap){
                loadBitmap(canvas);
                isLoadBitmap = false;
            } else {
                if(bm != null) canvas.drawBitmap(bm,0,0,null);
                if (pointList.size() < 2) {
                    return;
                }
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

        static class Point implements Serializable {
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
        }
    }
}



