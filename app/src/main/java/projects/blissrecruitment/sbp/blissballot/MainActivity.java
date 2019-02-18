package projects.blissrecruitment.sbp.blissballot;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

public class MainActivity extends AppCompatActivity implements QuestionsListFragment.OnFirstResponseListener, ShareDialog.OnShareListener {

    private StringRequest checkServerRequest;
    private BlankFragment loadFragment;
    private QuestionsListFragment listFragment;
    private boolean isDeepLink = false;
    private boolean gotFirstResponse = false; //used for the share btn, wait until get first response and then it can be used
    private Uri deepLinkUri;
    public static final String COMS_ID = "main_activity_id";
    public static final int DEEP_LINK_DIRECT_CODE = 44;
    public static boolean isActive = true; //used to determine if this Activity is active or paused


    /*This broadcast is used to receive messages from the NetworkBroadcastReceiver
    *If the NBR detects no internet connection, then this local receiver receives the broadcast and sets a No-Coms Dialog
    *@url https://trinitytuts.com/pass-data-from-broadcast-receiver-to-activity-without-reopening-activity/
    **/
    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("APP_DEBUG", "[BROADCAST-RECEIVER-MAIN] Received action on Main:"+intent.getAction());
            if(isActive && intent.getAction().equals(NetworkBroadcastReceiver.NO_COMS_BROADCAST)) {
                buildNoComsDialog().show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO change this to the xml
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Questions");

        NetworkBroadcastReceiver nbr = new NetworkBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkBroadcastReceiver.CONNECTIVITY_CHANGE);
        registerReceiver(nbr,filter);
        registerReceiver(broadcastReceiver, new IntentFilter(NetworkBroadcastReceiver.NO_COMS_BROADCAST));

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
        if(BlissApiSingleton.isConnected(getApplicationContext())){
            checkServerRequest = buildCheckServerRequest();
            BlissApiSingleton.getInstance(this).addToRequestQueue(checkServerRequest);
        }
    }

    /*Builds and sends server health request
     * */
    public void reCheckServer(){
        StringRequest checkServerRequest = buildCheckServerRequest();
        BlissApiSingleton.getInstance(this).addToRequestQueue(checkServerRequest);
    }

    /*Builds the request for the check health server request
    * */
    public StringRequest buildCheckServerRequest(){

        StringRequest healthRequest = new StringRequest(Request.Method.GET, BlissApiSingleton.BLISS_HEALTH_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null){
                            JSONObject obj = null;
                            try {
                                obj = new JSONObject(response);
                                String status = obj.getString("status");
                                if(status.equals("OK")){
                                    Log.d("APP_DEBUG","[RESPONSE] OK");
                                    if(isDeepLink) {
                                        processDeepLink();
                                    }else
                                        setListFragment(null); //null means that we dont need to pass anything onto listfragment (no deeplink)
                                }else{
                                    loadFragment.getView().findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);
                                    retryDialog();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),BlissApiSingleton.ERROR_MESSAGE,Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),BlissApiSingleton.ERROR_MESSAGE,Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),BlissApiSingleton.ERROR_MESSAGE,Toast.LENGTH_SHORT).show();
            }
        });

        return healthRequest;
    }

    /*Process both possible Deep links: question_filter and question_id
     * */
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
            startActivityForResult(detailIntent,DEEP_LINK_DIRECT_CODE);
        }
    }

    /*With Deep link the flow is Main -> Detail
      The normal flow however is Main -> List -> Detail
     *So if the user goes back from Detail, we must set list fragment and fetch records
     *  */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == DEEP_LINK_DIRECT_CODE){
            Log.i("APP_DEBUG","[INFO] Returned from Detail DEEP LINK to caller activity, resuming normal flow.");
            setListFragment(null);
        }
    }

    /*
     *Sets the list fragment and passes a @param bundle in case it's a deep link of type FILTER
     * this bundle contains the query to be searched in the list view
      *  */
    public void setListFragment(Bundle bundle){
        listFragment = new QuestionsListFragment();
        if(bundle != null)
            listFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,listFragment);
        fragmentTransaction.commitAllowingStateLoss(); //TODO not the best option as described by:
            //@url https://medium.com/@elye.project/handling-illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-d4ee8b630066
    }

    /*
     *Show dialog if server is not responding
     *  */
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

    /*
     *If theres no internet connection then build a no coms dialog
     *  */
    public AlertDialog buildNoComsDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connectivity");
        builder.setMessage("Please establish an internet connection.");
        builder.setPositiveButton("Retry",null);
        final AlertDialog ad = builder.create();
        ad.setCanceledOnTouchOutside(false);
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(BlissApiSingleton.isConnected(getApplicationContext())){
                            if(loadFragment.isVisible())
                                reCheckServer();
                            ad.dismiss();
                        }else{
                            Log.d("APP_DEBUG", "[CONNECTION] No Internet Connection");
                            Toast.makeText(getApplicationContext(),"Still no Internet Connection",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return ad;
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
                if(gotFirstResponse){ // if already got a response from ListFragment then we can use Share btn
                    String currentFilter = listFragment.getFilter();
                    ShareDialog shareDialog = ShareDialog.newInstance(currentFilter,ShareDialog.SHARE_FILTER_MODE);
                    shareDialog.show(getSupportFragmentManager(),"share_dialog");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*When the ListFragment gets its first records, it sets variable to true
    * */
    @Override
    public void onFirstResponseListener() {
        gotFirstResponse = true;
    }

    /*When the ShareDialog sends success/error message back to activity
     * */
    @Override
    public void onShareListener(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof QuestionsListFragment) {
            QuestionsListFragment questionListFragment = (QuestionsListFragment) fragment;
            questionListFragment.setOnFirstResponseListener(this);
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

}
