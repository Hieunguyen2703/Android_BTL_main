package com.lapTrinhUUDD.movie.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lapTrinhUUDD.movie.Models.MovieItemClickListenerNew;
import com.lapTrinhUUDD.movie.Models.SliderSide;
import com.lapTrinhUUDD.movie.R;


import java.util.List;

public class SliderPagerAdapterNew extends PagerAdapter {

    private Context mContext;
    List<SliderSide> mList;

    MovieItemClickListenerNew movieItemClickListenerNew;



    public SliderPagerAdapterNew(Context mcontext, List<SliderSide> mList, MovieItemClickListenerNew movieItemClickListenerNew) {
        this.mContext = mcontext;
        this.mList = mList;
        this.movieItemClickListenerNew = movieItemClickListenerNew;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View slideLayout = inflater.inflate(R.layout.slide_item,null);
        ImageView slideimage = slideLayout.findViewById(R.id.slide_img);
        TextView slidetitle = slideLayout.findViewById(R.id.slide_title);

        Glide.with(mContext).load(mList.get(position).getVideo_thumb()).into(slideimage);

        slidetitle.setText(mList.get(position).getVideo_name());

        slideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieItemClickListenerNew.onMovieClick(mList.get(position),slideimage);
            }
        });

        container.addView(slideLayout);
        return slideLayout;

    }

    @Override
    public int getCount() {

        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}

