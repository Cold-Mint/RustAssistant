package com.imageactivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.imageactivity.databinding.ActivityImageBinding;

public class Image extends AppCompatActivity {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private int screenWidth;
    private int screenHeight;
    private static Bitmap[] bitmap;

    public static void start(Activity activity, View view, Bitmap[] bitmap) {
        Intent intent = new Intent(activity, Image.class);
        ActivityOptionsCompat image = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity
                , view
                , "image");
        Image.bitmap = bitmap;
        activity.startActivity(intent, image.toBundle());
    }
    public static void start(Activity activity, View view, Bitmap bitmap) {
        Intent intent = new Intent(activity, Image.class);
        ActivityOptionsCompat image = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity
                , view
                , "image");
        Image.bitmap = new Bitmap[]{bitmap};
        activity.startActivity(intent, image.toBundle());
    }

    ActivityImageBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        // 初始化手势检测器
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        binding.image.setImageBitmap(bitmap[0]);
        binding.image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 处理缩放手势
//                scaleGestureDetector.onTouchEvent(event);
                // 处理拖动手势
//                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理缩放手势
        scaleGestureDetector.onTouchEvent(event);
        // 处理拖动手势
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                // 用户停止移动
                // 更新位置
                if (binding != null && !aBoolean) {
                    binding.image.setTranslationX(0);
                    binding.image.setTranslationY(0);
                    binding.getRoot().getRootView().setAlpha(1.0f);
                }
                break;
        }
        return true;
    }

    boolean post = false, aBoolean = false;

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            aBoolean = true;
            // 处理缩放事件
            float scaleFactor = detector.getScaleFactor();
            // 在应用缩放因子之前进行限制
            if (scaleFactor * binding.image.getScaleX() > 100) {
                scaleFactor = 100 / binding.image.getScaleX();
            }
            if (scaleFactor * binding.image.getScaleX() < 1.0f) {
                scaleFactor = 0.5f / binding.image.getScaleX();
                // 应用缩放因子
                post = true;
                aBoolean = false;
            }
            // 应用缩放因子
            binding.image.setScaleX(binding.image.getScaleX() * scaleFactor);
            binding.image.setScaleY(binding.image.getScaleY() * scaleFactor);
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            // 缩放结束
            float currentScale = binding.image.getScaleX(); // 获取当前的缩放比例
            if (currentScale < 1.0f) {
                handler.postDelayed(() -> {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.image
                            , "scaleX", 1.0f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.image
                            , "scaleY", 1.0f);
                    AnimatorSet scaleDown = new AnimatorSet();
                    scaleDown.play(scaleX).with(scaleY);
                    scaleDown.setDuration(300);
                    scaleDown.start();
                    binding.image.setTranslationX(0);
                    binding.image.setTranslationY(0);
                    post = false;
                }, 500);
            }
        }
    }

    Handler handler = new Handler();
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            // 拖动
            if (post) {
                return false;
            }
            float newX = binding.image.getTranslationX() - distanceX;
            float newY = binding.image.getTranslationY() - distanceY;
            if (aBoolean) {
                // 限制 ImageView 不超出界面宽高范围
/*                if (newX < 0) {
                    newX = 0;
                } else if (newX + binding.image.getWidth() > screenWidth) {
                    newX = screenWidth - binding.image.getWidth();
                }

                if (newY < 0) {
                    newY = 0;
                } else if (newY + binding.image.getHeight() > screenHeight) {
                    newY = screenHeight - binding.image.getHeight();
                }*/
                binding.image.setTranslationX(newX);
                binding.image.setTranslationY(newY);
            } else {
                // 计算位移
                // 添加顶部的限制
                newY = Math.max(0, newY); // 限制 Y 坐标不小于 0
                // 更新位置
                binding.image.setTranslationX(newX);
                binding.image.setTranslationY(newY);
                // 根据移动的 X 和 Y 坐标改变透明度
                float alpha = Math.max(0, 1 - newY / binding.getRoot().getRootView()
                        .getHeight()); // 根据 Y 坐标计算透明度
                binding.getRoot().getRootView().setAlpha(alpha);
            }
            return true;
        }
        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            if (aBoolean) {

            } else {
                ActivityCompat.finishAfterTransition(Image.this);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
        public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
            // 处理单击事件
            ActivityCompat.finishAfterTransition(Image.this);
            return super.onSingleTapConfirmed(motionEvent);
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent event) {
            // 双击时重置缩放和平移
            aBoolean = !aBoolean;
            Scale = (Scale == 1.0f) ? 2.0f : 1.0f;
            binding.image.setScaleX(Scale);
            binding.image.setScaleY(Scale);
            return true;
        }
    }

    private float Scale = 1.0f;

    private Context getContext() {
        return this;
    }
}