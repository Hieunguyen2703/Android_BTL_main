package com.lapTrinhUUDD.movie.Adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lapTrinhUUDD.movie.Models.GetVideoDetails;
import com.lapTrinhUUDD.movie.Models.MovieItemClickListenerNew;
import com.lapTrinhUUDD.movie.R;

import java.util.ArrayList;
import java.util.List;

public class MovieShowAdapter extends RecyclerView.Adapter<MovieShowAdapter.MyViewHolder> implements Filterable {

    private Context mContext;
    private List<GetVideoDetails> uploads;
    private List<GetVideoDetails> uploadsOld;
    MovieItemClickListenerNew movieItemClickListenerNew;


    public MovieShowAdapter(Context mContext, List<GetVideoDetails> uploads, MovieItemClickListenerNew movieItemClickListenerNew) {
        this.mContext = mContext;
        this.uploads = uploads;
        this.movieItemClickListenerNew = movieItemClickListenerNew;
        this.uploadsOld = uploads;
    }

    @NonNull
    @Override
    public MovieShowAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.movie_item_new,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieShowAdapter.MyViewHolder holder, int position) {
        GetVideoDetails getVideoDetails = uploads.get(position);
        holder.tvTitle.setText(getVideoDetails.getVideo_name());
        Glide.with(mContext).load(getVideoDetails.getVideo_thumb()).into(holder.ImgMovie);
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView ImgMovie;
        ConstraintLayout container;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_movie_title);
            ImgMovie = itemView.findViewById(R.id.item_movies_img);
            container = itemView.findViewById(R.id.container);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieItemClickListenerNew.onMovieClick(uploads.get(getAdapterPosition()),ImgMovie);
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    uploads = uploadsOld;

                }else {
                    List<GetVideoDetails> filteredList = new ArrayList<>();
                    for (GetVideoDetails row : uploadsOld) {
                        if (row.getVideo_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    uploads = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = uploads;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                uploads = (List<GetVideoDetails>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}

