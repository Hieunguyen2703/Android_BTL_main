package com.lapTrinhUUDD.movie.Service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.lapTrinhUUDD.movie.R;
import com.lapTrinhUUDD.movie.User.MoviePlayerActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class FloatingWidgetService extends Service {//Service là một thành phần trong Android được thiết kế để thực hiện các tác vụ nền (background tasks) mà không có giao diện người dùng

    public FloatingWidgetService() {
    }
    WindowManager mWindowManager;
    View mFloatingWidget;
    Uri videoUri;
    SimpleExoPlayer exoPlayer;
    PlayerView playerView;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String uriString = intent.getStringExtra("videoUri");
            videoUri = Uri.parse(uriString);

            if (mWindowManager != null && mFloatingWidget.isShown() && exoPlayer != null) {
                mWindowManager.removeView(mFloatingWidget);
                mFloatingWidget = null;
                mWindowManager = null;
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.release();
                exoPlayer = null;
            }
            final WindowManager.LayoutParams params;//là một lớp trong Android được sử dụng để mô tả các thuộc tính và hành vi của một cửa sổ trong hệ thống
            mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.custom_pop_up_window, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,//Từ Android O (API 26) trở lên
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 200;
            params.y = 200;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);//lấy về một thể hiện của WindowManager, là dịch vụ hệ thống quản lý các cửa sổ.
            mWindowManager.addView(mFloatingWidget, params);

            exoPlayer = new SimpleExoPlayer.Builder(this).build();
            playerView = mFloatingWidget.findViewById(R.id.playerView);
            ImageView imageViewclose = mFloatingWidget.findViewById(R.id.imageViewDismiss);
            ImageView imageViewMaxmize = mFloatingWidget.findViewById(R.id.imageViewMaximize);
            imageViewMaxmize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWindowManager != null && mFloatingWidget.isShown() && exoPlayer != null) {
                        mWindowManager.removeView(mFloatingWidget);
                        mFloatingWidget = null;
                        mWindowManager = null;
                        exoPlayer.setPlayWhenReady(false);
                        exoPlayer.release();
                        exoPlayer = null;
                        stopSelf();

                        Intent openActivityIntent = new Intent(FloatingWidgetService.this, MoviePlayerActivity.class);
                        openActivityIntent.putExtra("videoUri", videoUri.toString());
                        openActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(openActivityIntent);
                    }
                }
            });

            imageViewclose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mWindowManager != null && mFloatingWidget.isShown() && exoPlayer != null) {
                        mWindowManager.removeView(mFloatingWidget);
                        mFloatingWidget = null;
                        mWindowManager = null;
                        exoPlayer.setPlayWhenReady(false);
                        exoPlayer.release();
                        exoPlayer = null;
                        stopSelf();
                    }
                }
            });
            playVideos();



            playerView.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private boolean isClick = true;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            isClick = true;
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (isClick) {
                                v.performClick();
                            }
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            if (params.x != initialX || params.y != initialY) {
                                isClick = false;
                            }
                            mWindowManager.updateViewLayout(mFloatingWidget, params);
                            return true;
                    }
                    return false;
                }
            });

        }
            return super.onStartCommand(intent, flags, startId);
    }

        public void playVideos(){

            try {
                exoPlayer = new SimpleExoPlayer.Builder(this).build();
                String playInfo = Util.getUserAgent(this, "VideoPlayer");
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, playInfo);
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri));
                playerView.setPlayer(exoPlayer);
                exoPlayer.prepare(mediaSource);
                exoPlayer.setPlayWhenReady(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (mFloatingWidget != null) {
                mWindowManager.removeView(mFloatingWidget);
            }
        }
}
