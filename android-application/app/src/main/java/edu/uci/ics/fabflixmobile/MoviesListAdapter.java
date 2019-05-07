package edu.uci.ics.fabflixmobile;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class MoviesListAdapter extends RecyclerView.Adapter<MoviesListAdapter.MyViewHolder> {
    private ArrayList<Movie> moviesList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView movieTitle, movieId, movieYear, movieDir, movieGenres, movieStars;
        public MyViewHolder(View v) {
            super(v);
            movieTitle = (TextView) v.findViewById(R.id.movieTitle);
            movieId = (TextView) v.findViewById(R.id.movieId);
            movieYear = (TextView) v.findViewById(R.id.movieYear);
            movieDir = (TextView) v.findViewById(R.id.movieDir);
            movieGenres = (TextView) v.findViewById(R.id.movieGenres);
            movieStars = (TextView) v.findViewById(R.id.movieStars);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MoviesListAdapter(ArrayList<Movie> moviesList) {
        this.moviesList = moviesList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_row, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Movie movie = moviesList.get(position);
        holder.movieTitle.setText(movie.getMovieTitle());
        holder.movieId.setText(movie.getMovieId());
        holder.movieYear.setText(movie.getMovieYear());
        holder.movieDir.setText(movie.getMovieDir());

        if (movie.getMovieGenres().size() > 0){
            String genreString = android.text.TextUtils.join(", ", movie.getMovieGenres());
            holder.movieGenres.setText(genreString);
        }

        HashMap<String, String> stars = movie.getMovieStars();
        if (stars.size() > 0){
            String starString = android.text.TextUtils.join(", ", stars.keySet());
            holder.movieStars.setText(starString);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return this.moviesList.size();
    }
}