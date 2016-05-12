package com.deserteaglefe.maze;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    private static final int mBlockCountHorizontal = 31;
    private static final int mBlockCountVertical = 41;
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private int mBallSize;
    private ImageView mMaze;
    private ImageView mBall;
    private Bitmap mMazeInfo;
    private boolean mIsGaming;
    private int mColorBlue;
    private final float step = 1.0f;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mChannelSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Gravity Maze");
        findViews();
        init();
        setSensor();
    }

    private void setSensor() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private void init() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int blockWidth = screenWidth / mBlockCountHorizontal;
        int blockHeight = screenHeight / mBlockCountVertical;
        mBallSize = blockWidth > blockHeight ? blockHeight : blockWidth;
        mBallSize = ((mBallSize / 2) - 1) * 2;
        mChannelSize = mBallSize + 2;
        int width = mChannelSize * mBlockCountHorizontal;
        int height = mChannelSize * mBlockCountVertical;
        mMaze.setLayoutParams(new RelativeLayout.LayoutParams(width, height));

        // decodeResourceStream方法根据手机屏幕的密度有一个缩放图片的过程，而decodeFile不会自动处理。这里不需要处理
        InputStream inputStream = getResources().openRawResource(R.raw.mini_maze);
        mMazeInfo = BitmapFactory.decodeStream(inputStream);
        mBall.setLayoutParams(new RelativeLayout.LayoutParams(mBallSize, mBallSize));
        Log.i("Ball", "Ball point: ");
        mBall.setX(15f * mChannelSize);
        mBall.setY(0);
        mColorBlue = mMazeInfo.getPixel(0, 0);
    }

    private void findViews() {
        mMaze = (ImageView) findViewById(R.id.maze);
        mBall = (ImageView) findViewById(R.id.ball);
        mMaze.setOnClickListener(this);
    }

    private int getLocX() {
        return (int) ((mBall.getX() + mBallSize / 2)/ mChannelSize);
    }

    private int getLocY() {
        return (int) ((mBall.getY() + mBallSize / 2)/mChannelSize);
    }

    @Override
    public void onClick(View v) {
        int color = mMazeInfo.getPixel(getLocX(), getLocY());
        Log.i("Ball", "miniXY: " + getLocX() + ", " + getLocY());
        Log.i("Ball", "Ball loc color: " + color);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsGaming = false;
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsGaming = true;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    // 按键部分为模拟器测试专用
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当前游戏状态不处于正在游戏中时，屏蔽“返回”实体按键,避免程序进入后台;
        if (mIsGaming) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                moveLeft();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                moveRight();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                moveUp();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                moveDown();
            }
        }
        if (mBall.getY() > mChannelSize * 40) {
            Toast.makeText(MainActivity.this, "You win! Congratulations!", Toast.LENGTH_LONG).show();
            mIsGaming = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveLeft() {
        int next = (int) ((mBall.getX() - step) / mChannelSize);
        if (next >= 0) {
            int nextColor = mMazeInfo.getPixel(next, getLocY());
            if (!isColorSimilar(nextColor, mColorBlue)) {
                mBall.setX(mBall.getX() - step);
            }
        }
    }

    private void moveRight() {
        int next = (int) ((mBall.getX() + mBallSize + step) / mChannelSize);
        if(next <= mBlockCountHorizontal){
            int nextColor = mMazeInfo.getPixel(next, getLocY());
            if (!isColorSimilar(nextColor, mColorBlue)) {
                mBall.setX(mBall.getX() + step);
            } else {
                Log.i("Ball", "miniXY: " + getLocX() + ", " + getLocY());
                Log.i("Ball", "Right Color" + nextColor);
            }
        }
    }

    private void moveUp() {
        int next = (int) ((mBall.getY() - step) / mChannelSize);
        if (next >= 0) {
            int nextColor  = mMazeInfo.getPixel(getLocX(), next);
            if (!isColorSimilar(nextColor, mColorBlue)) {
                mBall.setY(mBall.getY() - step);
            } else {
                Log.i("Ball", "miniXY: " + getLocX() + ", " + getLocY());
                Log.i("Ball", "Up Color" + nextColor);
            }
        }
    }

    private void moveDown() {
        int next = (int) ((mBall.getY() + mBallSize + step) / mChannelSize);
        if(next <= mBlockCountVertical){
            int nextColor = mMazeInfo.getPixel(getLocX(), next);
            if (!isColorSimilar(nextColor, mColorBlue)) {
                mBall.setY(mBall.getY() + step);
            } else {
                Log.i("Ball", "miniXY: " + getLocX() + ", " + getLocY());
                Log.i("Ball", "Down Color" + nextColor);
            }
        }
    }

    private boolean isColorSimilar(int src, int des) {
        // Blue
        if (src == des) {
            return true;
        } else if (Math.abs(src % 0x100 - des % 0x100) < 0x10) {
            // Green
            src = src / 100;
            des = des / 100;
            if (Math.abs(src % 0x100 - des % 0x100) < 0x10) {
                // Red
                src = src / 100;
                des = des / 100;
                if (Math.abs(src % 0x100 - des % 0x100) < 0x10) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mIsGaming) {
            float x = event.values[0];
            float y = event.values[1];

            if (x > 1) {
                moveLeft();
            } else if (x < -1) {
                moveRight();
            }

            if (y > 1) {
                moveDown();
            } else if (y < -1) {
                moveUp();
            }
            if (mBall.getY() > mChannelSize * 40) {
                Toast.makeText(MainActivity.this, "You win! Congratulations!", Toast.LENGTH_LONG).show();
                mIsGaming = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
