package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class SearchBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bar);

        Bundle bundle = getIntent().getExtras();
        Toast.makeText(this, "Last activity was " + bundle.get("last_activity") + ".", Toast.LENGTH_LONG).show();
    }
    public void searchQueryRequest(View view) {

        String query = ((EditText) findViewById(R.id.searchText)).getText().toString().trim();
        Log.d("search.query", query);

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest searchRequest = new StringRequest(Request.Method.GET,
                "https://ec2-18-225-9-177.us-east-2.compute.amazonaws.com:8443/project4/api/FullTextSearchServlet?search_query=" + query + "&sortby=RatingDesc&pagenum=0&offset=5",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("search.success", response);
                        goToMoviesList(response);
                        // Add the request to the RequestQueue.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        error.printStackTrace();
                        Log.d("search.error", error.toString());
                    }
                }
        );

        if (!query.isEmpty()){
            // !important: queue.add is where the login request is actually sent
            queue.add(searchRequest);
        }
    }

    public void goToMoviesList(String response){
        String query = ((EditText) findViewById(R.id.searchText)).getText().toString().trim();
        Intent goToIntent = new Intent(this, MoviesListActivity.class);

        goToIntent.putExtra("last_activity", "Search Bar");
        goToIntent.putExtra("response", response);
        goToIntent.putExtra("pagenum", "0");
        goToIntent.putExtra("offset", "5");
        goToIntent.putExtra("query", query);

        startActivity(goToIntent);
    }
}
