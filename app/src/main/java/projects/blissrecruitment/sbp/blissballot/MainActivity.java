package projects.blissrecruitment.sbp.blissballot;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity  {

    private StringRequest checkServerRequest;
    private BlankFragment loadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Questions");

        //set initial load screen fragment
        loadFragment = new BlankFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,loadFragment,"load_fragment");
        fragmentTransaction.commit();

        //make request to health check
        checkServerRequest = buildCheckServerRequest();
        BlissApiSingleton.getInstance(this).addToRequestQueue(checkServerRequest);
    }

    public StringRequest buildCheckServerRequest(){

        StringRequest healthRequest = new StringRequest(Request.Method.GET, BlissApiSingleton.BLISS_HEALTH_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(response);
                            String status = obj.getString("status");
                            Log.d("APP_DEBUG",status);
                            if(status.equals("OK")){
                                QuestionsListFragment fragment = new QuestionsListFragment();
                                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container,fragment);
                                fragmentTransaction.commit();
                            }else{
                                loadFragment.getView().findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                                retryDialog();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return healthRequest;
    }

    public void retryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Retry connection").setTitle("Server Connectivity");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadFragment.getView().findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
                BlissApiSingleton.getInstance(getApplicationContext()).addToRequestQueue(checkServerRequest);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
