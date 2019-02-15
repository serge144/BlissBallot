package projects.blissrecruitment.sbp.blissballot;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {

    private ProgressBar pb;
    private Button retry_button;
    private static String BLISS_HEALTH = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/health";
    private RequestQueue queue;
    private StringRequest checkServerRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pb = findViewById(R.id.progressBar);
        retry_button = findViewById(R.id.retry_button);
        retry_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                retry_button.setVisibility(View.INVISIBLE);
                execCheckServerRequest(checkServerRequest);
            }
        });

        QuestionsListFragment fragment = new QuestionsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.questions_fragment,fragment);
        fragmentTransaction.commit();

        queue = Volley.newRequestQueue(this);
        checkServerRequest = buildCheckServerRequest();
        execCheckServerRequest(checkServerRequest);

    }

    public StringRequest buildCheckServerRequest(){

        StringRequest healthRequest = new StringRequest(Request.Method.GET, BLISS_HEALTH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(response);
                            String status = obj.getString("status");
                            Log.d("APP_DEBUG",status);
                            status = "KO";
                            if(status.equals("OK")){
                                pb.setVisibility(View.INVISIBLE);
                            }else{
                                pb.setVisibility(View.INVISIBLE);
                                retry_button.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.setVisibility(View.INVISIBLE);
                retry_button.setVisibility(View.VISIBLE);
            }
        });

        return healthRequest;
    }

    public void execCheckServerRequest(StringRequest healthRequest){
        queue.add(healthRequest);
    }


}
