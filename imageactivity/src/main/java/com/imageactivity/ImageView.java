package com.imageactivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

public class ImageView extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private static Bitmap[] bitmap;

    public static void start(Activity activity, View view, Bitmap[] bitmap) {
        Intent intent = new Intent(activity, Image.class);
        ActivityOptionsCompat image = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity
                , view
                , "image");
        ImageView.bitmap = bitmap;
        activity.startActivity(intent, image.toBundle());
    }

    /** @noinspection unused*/
    public static void start(android.widget.ImageView view, Bitmap[] bitmap) {
        ImageView.bitmap = bitmap;
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeThumbnailScaleUpAnimation(view, bitmap[0], (int) view.getX(),
                        (int) view.getY());
        Intent intent = new Intent(view.getContext(), Image.class);
        view.getContext().startActivity(intent, options.toBundle());
    }

    float originalScale;
    android.widget.ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.OnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        aBoolean = true;
                        // 缩放中
                        float scaleFactor = detector.getScaleFactor();
                        matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                        imageView.setImageMatrix(matrix);
                        /*
                        // 处理缩放事件
                        float scaleFactor = detector.getScaleFactor();
                        // 在应用缩放因子之前进行限制
                        if (scaleFactor * imageView.getScaleX() > 100) {
                            scaleFactor = 100 / imageView.getScaleX();
                        }
                        if (scaleFactor * imageView.getScaleX() < 1.0f) {
                            scaleFactor = 0.5f / imageView.getScaleX();
                            // 应用缩放因子
                            post = true;
                            aBoolean = false;
                        }
                        // 应用缩放因子
                        imageView.setScaleX(imageView.getScaleX() * scaleFactor);
                        imageView.setScaleY(imageView.getScaleY() * scaleFactor);*/
                        return true;
                    }

                    @Override
                    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                        // 缩放开始
                        return true;
                    }

                    @Override
                    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                        // 缩放结束
/*                        float currentScale = imageView.getScaleX(); // 获取当前的缩放比例
                        if (currentScale < 1.0f) {
                            handler.postDelayed(() -> {
                                ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", originalScale);
                                ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", originalScale);
                                AnimatorSet scaleDown = new AnimatorSet();
                                scaleDown.play(scaleX).with(scaleY);
                                scaleDown.setDuration(300);
                                scaleDown.start();
                                imageView.setTranslationX(0);
                                imageView.setTranslationY(0);
                                post = false;
                            }, 500);
                        }*/
                    }
                });
        setContentView(R.layout.activity_image);
        // 获取屏幕的宽度和高度
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        k = displayMetrics.widthPixels;
        g = displayMetrics.heightPixels;
//        viewById.setImageBitmap(bitmap[0]);
/*
        imagemaxX = displayMetrics.widthPixels - imageView.getWidth();
        imagemaxY = displayMetrics.heightPixels - imageView.getHeight();
        originalScale = imageView.getScaleX();
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        matrix = imageView.getMatrix();*/
        view = getWindow().getDecorView().getRootView();
    }

    Handler handler = new Handler();
  @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                // 用户停止移动
                // 更新位置
             if (imageView != null && !aBoolean) {
                    imageView.setTranslationX(0);
                    imageView.setTranslationY(0);
                    view.setAlpha(1.0f);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void finish() {
        super.finish();
        bitmap = null;
    }

    View view;
    boolean aBoolean = false, post = false;
    int k, g, imagemaxX, imagemaxY;

    private Matrix matrix = new Matrix();
    private static final float MIN_SCALE_FACTOR = 0.5f; // 最小缩放系数
    private static final float MAX_SCALE_FACTOR = 100.0f; // 最大缩放系数
    // 手势监听器
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            // 处理双击事件
            aBoolean = !aBoolean;
/*            scaleFactor = (scaleFactor == 1.0f) ? 2.0f : 1.0f;
            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);*/
            return super.onDoubleTap(e);
        }

        public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
            // 处理单击事件
            ActivityCompat.finishAfterTransition(ImageView.this);
            return super.onSingleTapConfirmed(motionEvent);
        }
        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            if (aBoolean) {

            } else {
                ActivityCompat.finishAfterTransition(ImageView.this);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(@NonNull MotionEvent e1,
                                @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (post) {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            if (aBoolean) {
                matrix.postTranslate(-distanceX, -distanceY);
                imageView.setImageMatrix(matrix);
            } else {
                // 计算位移
                float translationX = imageView.getTranslationX() - distanceX;
                float translationY = imageView.getTranslationY() - distanceY;
                // 添加顶部的限制
                translationY = Math.max(0, translationY); // 限制 Y 坐标不小于 0
                // 更新位置
                imageView.setTranslationX(translationX);
                imageView.setTranslationY(translationY);
                // 根据移动的 X 和 Y 坐标改变透明度
                float alpha = Math.max(0, 1 - translationY / view.getHeight()); // 根据 Y 坐标计算透明度
                view.setAlpha(alpha);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
