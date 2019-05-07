package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("last_activity") != null) {
                Toast.makeText(this, "Last activity was " + bundle.get("last_activity") + ".", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_red, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectToTomcat(View view) {

        // no user is logged in, so we must connect to the server

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        final StringRequest loginRequest = new StringRequest(Request.Method.POST, "https://ec2-18-225-9-177.us-east-2.compute.amazonaws.com:8443/project4/api/login",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("login.success", response);
                        try{
                            JSONObject resp = new JSONObject(response);
                            String message = resp.getString("message");
                            if (message.trim().equalsIgnoreCase("success")){
                                goToSearchBar();
                            } else{
                                ((TextView) findViewById(R.id.http_response)).setText(message);
                            }
                        } catch (Exception e){
                            Log.d("login.error", e.getMessage().toString());
                        }
                        // Add the request to the RequestQueue.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                String username = ((EditText) findViewById(R.id.username)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();
                // Post request form data
                final Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                params.put("edashboard", "false");

                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }

    public void goToSearchBar(){
        Intent goToIntent = new Intent(this, SearchBarActivity.class);

        goToIntent.putExtra("last_activity", "Login");

        startActivity(goToIntent);
    }
}
