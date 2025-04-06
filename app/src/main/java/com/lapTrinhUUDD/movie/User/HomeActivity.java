package com.lapTrinhUUDD.movie.User;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.lapTrinhUUDD.movie.Adapter.MovieShowAdapter;
import com.lapTrinhUUDD.movie.Adapter.SliderPagerAdapterNew;
import com.lapTrinhUUDD.movie.Models.GetVideoDetails;
import com.lapTrinhUUDD.movie.Models.MovieItemClickListenerNew;
import com.lapTrinhUUDD.movie.Models.SliderSide;
import com.lapTrinhUUDD.movie.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements MovieItemClickListenerNew {

    MovieShowAdapter movieShowAdapter;
    DatabaseReference mDatabasereference;
    private List<GetVideoDetails> movies, uploadslistLatests, uploadsListPopular;
    private List<GetVideoDetails> actionsmovies, sportsmovies, comedymovies, romanticmovies, adventuremovies;
    private ViewPager sliderPager;
    private List<SliderSide> uploadsSlider;
    private TabLayout indicator, tabmoviesaction;
    private RecyclerView MoviesRv, moviesRvWeek, tab;
    private ImageView btnSearch, btnInfo;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar);

            btnSearch = findViewById(R.id.btn_search);
            btnInfo = findViewById(R.id.btn_info);

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    intent.putExtra("allMovies", new ArrayList<>(movies));
                    startActivity(intent);
                }
            });

            btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
                }
            });

        }

        progressDialog = new ProgressDialog(this);
        inViews();
        addAllMovies();
    }

    private void addAllMovies(){

        movies = new ArrayList<>();
        uploadslistLatests = new ArrayList<>();
        uploadsListPopular = new ArrayList<>();
        actionsmovies = new ArrayList<>();
        adventuremovies = new ArrayList<>();
        comedymovies = new ArrayList<>();
        sportsmovies = new ArrayList<>();
        romanticmovies = new ArrayList<>();
        uploadsSlider = new ArrayList<>();

        mDatabasereference = FirebaseDatabase.getInstance().getReference("videos");
        progressDialog.setMessage("loading...");
        progressDialog.show();

        mDatabasereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    GetVideoDetails upload = postSnapshot.getValue(GetVideoDetails.class);
                    SliderSide slide = postSnapshot.getValue(SliderSide.class);

                    if(upload.getVideo_type().equals("Latest")){
                        uploadslistLatests.add(upload);
                    }
                    if(upload.getVideo_type().equals("Popular")){
                        uploadsListPopular.add(upload);
                    }
                    if(upload.getVideo_category().equals("Action")){
                        actionsmovies.add(upload);
                    }
                    if(upload.getVideo_category().equals("Adventure")){
                        adventuremovies.add(upload);
                    }
                    if(upload.getVideo_category().equals("Comedy")){
                        comedymovies.add(upload);
                    }
                    if(upload.getVideo_category().equals("Romantic")){
                        romanticmovies.add(upload);
                    }
                    if(upload.getVideo_category().equals("Sport")){
                        sportsmovies.add(upload);
                    }
                    if(upload.getVideo_slide().equals("Slide")){
                        Log.d("Slide check","Co ton tai video slide");
                        uploadsSlider.add(slide);
                    }

                    movies.add(upload);
                }
                iniSlider();
                iniPopularMovies();
                iniWeekMovies();
                moviesViewTab();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void iniSlider() {
        SliderPagerAdapterNew adapterNew = new SliderPagerAdapterNew(this, uploadsSlider,this);
        sliderPager.setAdapter(adapterNew);
        adapterNew.notifyDataSetChanged();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTime(),4000,6000);
        indicator.setupWithViewPager(sliderPager,true);
    }


    private void iniWeekMovies(){
        movieShowAdapter = new MovieShowAdapter(this,uploadslistLatests,this);
        moviesRvWeek.setAdapter(movieShowAdapter);
        moviesRvWeek.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void iniPopularMovies(){
        movieShowAdapter = new MovieShowAdapter(this,uploadsListPopular,this);
        MoviesRv.setAdapter(movieShowAdapter);
        MoviesRv.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private  void moviesViewTab(){
        getActionsMovies();
        tabmoviesaction.addTab(tabmoviesaction.newTab().setText("Action"));
        tabmoviesaction.addTab(tabmoviesaction.newTab().setText("Adventure"));
        tabmoviesaction.addTab(tabmoviesaction.newTab().setText("Comedy"));
        tabmoviesaction.addTab(tabmoviesaction.newTab().setText("Romantic"));
        tabmoviesaction.addTab(tabmoviesaction.newTab().setText("Sport"));
        tabmoviesaction.setTabGravity(TabLayout.GRAVITY_FILL);
        tabmoviesaction.setTabTextColors(ColorStateList.valueOf(Color.WHITE));

        tabmoviesaction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()){
                    case 0:
                        getActionsMovies();
                        break;
                    case 1:
                        getAdventureMovies();
                        break;
                    case 2:
                        getComedyMovies();
                        break;
                    case 3:
                        getRomanticMovies();
                        break;
                    case 4:
                        getSportsMovies();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void inViews(){
        tabmoviesaction = findViewById(R.id.tabActionMovies);
        sliderPager = findViewById(R.id.slider_pager);
        indicator = findViewById(R.id.indicator);
        moviesRvWeek = findViewById(R.id.rv_movies_week);
        MoviesRv = findViewById(R.id.Rv_movies);
        tab = findViewById(R.id.tabrecycler);
    }

    @Override
    public void onMovieClick(GetVideoDetails movie, ImageView imageView) {
        Intent in = new Intent(this, MovieDetailsActivity.class);
        in.putExtra("title",movie.getVideo_name());
        in.putExtra("imgURL",movie.getVideo_thumb());
        in.putExtra("imgCover",movie.getVideo_thumb());
        in.putExtra("movieDetail",movie.getVideo_description());
        in.putExtra("movieUrl",movie.getVideo_url());
        in.putExtra("movieCategory",movie.getVideo_category());
        startActivity(in);
    }

    @Override
    public void onMovieClick(SliderSide movie, ImageView imageView) {
        Intent in = new Intent(this, MovieDetailsActivity.class);
        in.putExtra("title",movie.getVideo_name());
        in.putExtra("imgURL",movie.getVideo_thumb());
        in.putExtra("imgCover",movie.getVideo_thumb());
        in.putExtra("movieDetail",movie.getVideo_description());
        in.putExtra("movieUrl",movie.getVideo_url());
        in.putExtra("movieCategory",movie.getVideo_category());
        startActivity(in);
    }

    public class SliderTime extends TimerTask {
        @Override
        public void run() {
            HomeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(sliderPager.getCurrentItem()<uploadsSlider.size()-1){
                        sliderPager.setCurrentItem(sliderPager.getCurrentItem()+1);
                    }else{
                        sliderPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    private void getActionsMovies(){
        movieShowAdapter = new MovieShowAdapter(this,actionsmovies,this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getSportsMovies(){
        movieShowAdapter = new MovieShowAdapter(this,sportsmovies,this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getRomanticMovies(){
        movieShowAdapter = new MovieShowAdapter(this,romanticmovies,this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getComedyMovies(){
        movieShowAdapter = new MovieShowAdapter(this,comedymovies,this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getAdventureMovies(){
        movieShowAdapter = new MovieShowAdapter(this,adventuremovies,this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        movieShowAdapter.notifyDataSetChanged();
    }
}