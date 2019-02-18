package projects.blissrecruitment.sbp.blissballot;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements QuestionsListFragment.OnFirstResponseListener {

    private StringRequest checkServerRequest;
    private BlankFragment loadFragment;
    private QuestionsListFragment listFragment;
    private boolean isDeepLink = false;
    private boolean gotFirstResponse = false;
    private Uri deepLinkUri;
    public static final String COMS_ID = "main_activity_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO change this to the xml
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Questions");

        //set initial load screen fragment
        loadFragment = new BlankFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,loadFragment,"load_fragment");
        fragmentTransaction.commit();

        //Parse Uri data if it was from a deeplink
        //@url https://code.tutsplus.com/tutorials/how-to-enable-deep-links-on-android--cms-26317
        deepLinkUri = this.getIntent().getData();
        if(deepLinkUri != null && deepLinkUri.isHierarchical())
            isDeepLink = true;

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
                            if(status.equals("OK")){
                                Log.d("APP_DEBUG","[RESPONSE] OK");
                                if(isDeepLink) {
                                    processDeepLink();
                                }else
                                    setListFragment(null);
                            }else{
                                loadFragment.getView().findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                                retryDialog();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace(); //TODO handle this json error
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO handle this volley error
            }
        });

        return healthRequest;
    }

    public void processDeepLink(){

        if(deepLinkUri.getQueryParameterNames().contains("question_filter")){
            String questionFilterParam = deepLinkUri.getQueryParameter("question_filter");
            Bundle bundle = new Bundle();
            Log.i("APP_DEBUG","[DEEP-LINK] Query parameter: "+ questionFilterParam);
            bundle.putString("question_filter",questionFilterParam);
            setListFragment(bundle);
        }else if(deepLinkUri.getQueryParameterNames().contains("question_id")){
            int questionId = Integer.parseInt(deepLinkUri.getQueryParameter("question_id"));
            Log.i("APP_DEBUG","[DEEP-LINK] Query parameter: "+ questionId);
            Intent detailIntent = new Intent(getApplicationContext(),DetailScreen.class);
            detailIntent.putExtra(BlissApiSingleton.ACTIVITY_CALLER , COMS_ID);
            detailIntent.putExtra("question_id",questionId);
            startActivity(detailIntent);

        }
    }

    public void setListFragment(Bundle bundle){
        listFragment = new QuestionsListFragment();
        if(bundle != null)
            listFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,listFragment);
        fragmentTransaction.commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_share:
                if(gotFirstResponse){
                    String currentFilter = listFragment.getFilter();
                    ShareDialog shareDialog = ShareDialog.newInstance(currentFilter);
                    shareDialog.show(getSupportFragmentManager(),"share_dialog");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFirstResponseListener() {
        gotFirstResponse = true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof QuestionsListFragment) {
            QuestionsListFragment questionListFragment = (QuestionsListFragment) fragment;
            questionListFragment.setOnFirstResponseListener(this);
        }
        super.onAttachFragment(fragment);
    }
}
