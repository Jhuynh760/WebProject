package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MoviesListActivity extends AppCompatActivity {
    private int offset;
    private int pagenum;
    private String query;

    private String button;

    private JSONArray responseData;
    private ArrayList<Movie> movies;

    private RecyclerView recyclerView;
    private MoviesListAdapter mAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_list);

        Bundle bundle = getIntent().getExtras();
        Toast.makeText(this, "Last activity was " + bundle.get("last_activity") + ".", Toast.LENGTH_LONG).show();

        try{
            responseData = new JSONArray(bundle.get("response").toString());
        } catch (Exception e){
            e.printStackTrace();
        }

        offset = Integer.parseInt((String) bundle.get("offset"));
        pagenum = Integer.parseInt((String) bundle.get("pagenum"));
        query = (String) bundle.get("query");

        Log.d("moviesList.create", responseData.toString());

        movies = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.movieListView) ;
        mAdaptor = new MoviesListAdapter(movies);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new MoviesListOnTouchListener(getApplicationContext(), recyclerView, new MoviesListOnTouchListener.ClickListener(){
            @Override
            public void onClick(View view, int position){
                Movie movie = movies.get(position);

                Intent goToIntent = new Intent(getApplicationContext(), SingleMovieActivity.class);

                goToIntent.putExtra("last_activity", "Movies List Activity");
                goToIntent.putExtra("movieTitle", movie.getMovieTitle());
                goToIntent.putExtra("movieYear", movie.getMovieYear());
                goToIntent.putExtra("movieDir", movie.getMovieDir());
                goToIntent.putExtra("movieStars", movie.getMovieStars());
                goToIntent.putExtra("movieGenres", movie.getMovieGenres());

                startActivity(goToIntent);
            }

            @Override
            public void onLongClick(View view, int position){

            }
        }));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdaptor);

        displayResponse();

        Button prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setEnabled(false);
    }

    public void displayResponse(){
        if (responseData.length() > 0){
            if (responseData.length() == 5){
                Button nextButton = (Button) findViewById(R.id.nextButton);
                nextButton.setEnabled(true);
            }
            if (pagenum > 0){
                Button prevButton = (Button) findViewById(R.id.prevButton);
                prevButton.setEnabled(true);
            } else{
                Button prevButton = (Button) findViewById(R.id.prevButton);
                prevButton.setEnabled(false);
            }
            movies.clear();
            try{
                Log.d("MoviesList.display", "responseData Length: " + responseData.length());
                for (int i = 0; i < responseData.length(); i++){
                    JSONObject movieJson = responseData.getJSONObject(i);
                    String movieId = movieJson.getString("movie_id");
                    String movieTitle = movieJson.getString("movie_title");
                    String movieYear = movieJson.getString("movie_year");
                    String movieDir = movieJson.getString("movie_director");
                    String movieRating = movieJson.getString("movie_rating");
                    JSONArray movieGenres = movieJson.getJSONArray("movie_genres");
                    JSONArray movieStars = movieJson.getJSONArray("movie_stars");
                    Movie movie = new Movie(movieId, movieTitle, movieYear, movieDir, movieRating);
                    for (int g = 0; g < movieGenres.length(); g++){
                        movie.addMovieGenre(movieGenres.getJSONObject(g).getString("genre"));
                    }
                    for (int s = 0; s < movieStars.length(); s++){
                        JSONObject star = movieStars.getJSONObject(s);
                        movie.addMovieStar(star.getString("star_name"), star.getString("star_id"));
                    }
                    movies.add(movie);
                    Log.d("movieList.movie", movie.toString());
                }
                Log.d("moviesList.success", "Success");

            } catch (Exception e){
                e.printStackTrace();
                Log.d("moviesList.error", e.toString());
            }
            mAdaptor.notifyDataSetChanged();

            RecyclerView movieListView = (RecyclerView) findViewById(R.id.movieListView);
            movieListView.smoothScrollToPosition(0);
        } else{
            Toast.makeText(this, "Nothing found or not enough entries.", Toast.LENGTH_LONG).show();
            if (button.equalsIgnoreCase("next")){
                pagenum--;
            }
            Button nextButton = (Button) findViewById(R.id.nextButton);
            nextButton.setEnabled(false);

            if (pagenum > 0){
                Button prevButton = (Button) findViewById(R.id.prevButton);
                prevButton.setEnabled(true);
            }
        }
        Log.d("moviesList.query", "Query: " + query);
        Log.d("moviesList.query", "PageNum: " + pagenum);
        Log.d("moviesList.query", "Offset: " + offset);
    }

    public void searchQueryRequest() {
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(Request.Method.GET,
                "https://ec2-18-225-9-177.us-east-2.compute.amazonaws.com:8443/project4/api/FullTextSearchServlet?search_query=" + query + "&sortby=RatingDesc&pagenum=" + pagenum + "&offset=" + offset,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("moviesList.query.succes", response);
                        try{
                            responseData = new JSONArray(response);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        displayResponse();
                        // Add the request to the RequestQueue.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        error.printStackTrace();
                        Log.d("moviesList.query.error", error.toString());
                    }
                }
        );

        if (!query.isEmpty()){
            // !important: queue.add is where the login request is actually sent
            queue.add(searchRequest);
        }
    }

    public void nextPage(View view){
        pagenum++;
        button = "next";
        searchQueryRequest();
    }

    public void prevPage(View view){
        if (pagenum > 0){
            Button prevButton = (Button) findViewById(R.id.prevButton);
            prevButton.setEnabled(true);
            pagenum--;
            button = "prev";
            searchQueryRequest();
        } else{
            Button prevButton = (Button) findViewById(R.id.prevButton);
            prevButton.setEnabled(false);
        }
    }
}
