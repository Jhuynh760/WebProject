package edu.uci.ics.fabflixmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SingleMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        Bundle bundle = getIntent().getExtras();
        Toast.makeText(this, "Last activity was " + bundle.get("last_activity") + ".", Toast.LENGTH_LONG).show();

        String movieTitle = (String) bundle.get("movieTitle");
        String movieYear = (String) bundle.get("movieYear");
        String movieDir = (String) bundle.get("movieDir");
        ArrayList<String> movieGenres = ( ArrayList<String>) bundle.get("movieGenres");
        HashMap<String, String> movieStars = (HashMap<String, String>) bundle.get("movieStars");

        TextView movieTitleView = (TextView) findViewById(R.id.movieTitle);
        movieTitleView.setText(movieTitle);

        TextView movieYearView = (TextView) findViewById(R.id.movieYear);
        movieYearView.setText(movieYear);

        TextView movieDirView = (TextView) findViewById(R.id.movieDir);
        movieDirView.setText(movieDir);

        TextView movieGenresView = (TextView) findViewById(R.id.movieGenres);
        movieGenresView.setText(android.text.TextUtils.join("\n", movieGenres));

        TextView movieStarsView = (TextView) findViewById(R.id.movieStars);
        movieStarsView.setText(android.text.TextUtils.join("\n", movieStars.keySet()));
    }
}
