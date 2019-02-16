package projects.blissrecruitment.sbp.blissballot;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {

    private ProgressBar pb;
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
                                retryDialog();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.setVisibility(View.INVISIBLE);
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
                pb.setVisibility(View.VISIBLE);
                execCheckServerRequest(checkServerRequest);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public void execCheckServerRequest(StringRequest healthRequest){
        queue.add(healthRequest);
    }


}
