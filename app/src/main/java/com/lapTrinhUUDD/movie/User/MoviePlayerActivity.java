package com.lapTrinhUUDD.movie.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lapTrinhUUDD.movie.R;
import com.lapTrinhUUDD.movie.Service.FloatingWidgetService;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class MoviePlayerActivity extends AppCompatActivity {
    Uri videoUri;
    PlayerView playerView;
    ExoPlayer exoPlayer;
    ImageView exo_floating_widget;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        playerView = findViewById(R.id.playerView);
        exo_floating_widget = findViewById(R.id.exo_floating_widget);

        Intent intent = getIntent();
        if(intent != null){
            String uri_str = intent.getStringExtra("videoUri");
            videoUri = Uri.parse(uri_str);
        }else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        exo_floating_widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.release();
                Intent serviceIntent = new Intent(MoviePlayerActivity.this, FloatingWidgetService.class);
                serviceIntent.putExtra("videoUri", videoUri.toString());
                startService(serviceIntent);
            }
        });

        exoPlayer = new SimpleExoPlayer.Builder(this).build();

        playVideo();
    }

    private void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//sử dụng FLAG_FULLSCREEN ở cả hai tham số của setFlags là một cách để đảm bảo rằng bạn chỉ thay đổi trạng thái của cờ FLAG_FULLSCREEN, và không ảnh hưởng đến các cờ khác của cửa sổ.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void playVideo() {
        try {
            String playerInfo = Util.getUserAgent(this, "MovieAppClient");
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, playerInfo);//DataSource chịu trách nhiệm cho việc đọc dữ liệu từ URI, URL, hoặc tệp.
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri));//ProgressiveMediaSource.Factory để tạo ra một ProgressiveMediaSource từ MediaItem. ProgressiveMediaSource sẽ xử lý việc tải và phân tích dữ liệu từ MediaItem
            playerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }
}