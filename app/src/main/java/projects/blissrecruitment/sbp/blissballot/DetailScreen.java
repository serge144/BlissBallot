package projects.blissrecruitment.sbp.blissballot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailScreen extends AppCompatActivity {

    private Question question;
    private LinearLayout linearLayout;
    private ArrayList<CheckBox> choicesCheckboxs = new ArrayList<CheckBox>();

    private ImageView image;
    private Button voteBtn;
    private ImageButton shareBtn;
    private TextView questionText,dateText;

    /*This broadcast is used to receive messages from the NetworkBroadcastReceiver
     *If the NBR detects no internet connection, then this local receiver receives the broadcast and sets a No-Coms Dialog
     *@url https://trinitytuts.com/pass-data-from-broadcast-receiver-to-activity-without-reopening-activity/
     **/
    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("APP_DEBUG", "[BROADCAST-RECEIVER-MAIN] Received action on Main:"+intent.getAction());
            if(intent.getAction().equals(NetworkBroadcastReceiver.NO_COMS_BROADCAST)) {
                buildNoComsDialog().show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_screen);

        //get UI refs
        image = findViewById(R.id.image_detail);
        voteBtn = findViewById(R.id.vote_button);
        questionText = findViewById(R.id.question_text_detail);
        dateText = findViewById(R.id.date_text);
        linearLayout = findViewById(R.id.linear_section);
        shareBtn = findViewById(R.id.share_button_detail);
        setVoteButtonListener();

        registerReceiver(broadcastReceiver, new IntentFilter(NetworkBroadcastReceiver.NO_COMS_BROADCAST));

        //A) This Activity may either be called by the MainActivity ( if its a deeplink with question_id)
        //B) or may be called by QuestionsListFragment
        //if A) then we must request question data, if B) we already have that question data, no need request
        Intent intent = getIntent();
        String callerId = intent.getExtras().getString(BlissApiSingleton.ACTIVITY_CALLER);
        switch (callerId) {
            case QuestionsListFragment.COMS_ID:
                Bundle b  = intent.getBundleExtra("bundle");
                question = b.getParcelable("question");
                buildDetailView();
                break;
            case MainActivity.COMS_ID:
                int questionId = intent.getExtras().getInt("question_id");
                JsonObjectRequest questionRequest = buildGetQuestionRequest(questionId);
                BlissApiSingleton.getInstance(this).addToRequestQueue(questionRequest);
                break;
        }

        //change actionbar title and set a return button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Question Detail");
        actionBar.setDisplayHomeAsUpEnabled(true); //return button

    }

    /*
     *  Builds some components of the DetailView
     *  Also used Picasso library to load image.
     * */
    public void buildDetailView(){

        //insert the choices in RadioGroup
        buildRadioGroup(question.getChoices());

        //Request 600x400 image using picasso and update the rest of the UI elems
        Log.d("APP_DEBUG","[REQUEST-PICASSO] Url: "+ question.getImgUrl());
        Picasso.get().load(question.getImgUrl()).transform(new CircleTransformation(45,0)).into(image);
        questionText.setText(question.getText());
        dateText.setText(question.getDisplayDateString(question.getCalendar()));

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareDialog shareDialog = ShareDialog.newInstance(question.getId(),ShareDialog.SHARE_QUESTION_MODE);
                shareDialog.show(getSupportFragmentManager(),"share_dialog");
            }
        });
    }

    /*
     *  This request is used in case a DEEPLINK was used and we dont have the question data from previous activity,
     *  so we must fetch
     * */
    public JsonObjectRequest buildGetQuestionRequest(final int questionId){
        String baseUrl = BlissApiSingleton.BLISS_BASE_ALL_QUESTIONS_REQUEST;
        String url = baseUrl + "/" + questionId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        question = Question.processJSONObject(response);
                        buildDetailView();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });
        return jsonObjectRequest;
    }

    /*
     *  When this button is pressed, updates the vote count of selected choice, builds the put request, and requests
     * */
    private void setVoteButtonListener(){
        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox choice = someChoiceIsChecked();
                if(choice == null){
                    Log.d("APP_DEBUG","[INFO] No choice selected yet");
                    Toast.makeText(getApplicationContext(),"A choice must be selected to vote",Toast.LENGTH_SHORT).show();
                }else{
                    //send update request to apropriate question
                    updateChoiceVoteCount(choice);
                    JsonObjectRequest updateRequest = buildPutQuestionRequest();
                    BlissApiSingleton.getInstance(getApplicationContext()).addToRequestQueue(updateRequest);
                }
            }
        });
    }

    /*
     *  Verifies if some checkbox is currently checked
     * */
    private CheckBox someChoiceIsChecked(){
        for(CheckBox cb : choicesCheckboxs)
            if(cb.isChecked())
                return cb;
        return null;
    }

    /*  Updates the vote count of the choice
     *
     * */
    private void updateChoiceVoteCount(CheckBox cb){
        String name = cb.getText().toString();
        question.vote(name);
    }

    /*  Builds the request to update the corresponding question currently on this DetailView via API
     *
     * */
    private JsonObjectRequest buildPutQuestionRequest(){

        String baseUrl = BlissApiSingleton.BLISS_BASE_ALL_QUESTIONS_REQUEST;
        String url = baseUrl +"/" + question.getId();
        JSONObject quesObject = question.toJSON();
        JsonObjectRequest jrequest = new JsonObjectRequest(Request.Method.PUT, url, quesObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("APP_DEBUG","[RESPONSE]" + response.toString());
                if(response != null){
                    Toast.makeText(getApplicationContext(),"Vote updated.",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO Handle error
            }
        });
        return jrequest;
    }

    /*  Builds the CheckBoxes for each Choice
    *   Not actually a radio group, but functions the same way, but with CheckBoxes
    * */
    private void buildRadioGroup(JSONArray choices){

        //radioView.setOrientation(RadioGroup.VERTICAL);
        int maxVotes = question.getMaxVotes();
        LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0 ; i < choices.length() ; i++){
            try {
                JSONObject choice = (JSONObject) choices.get(i);
                String name = choice.getString("choice");
                int votes = choice.getInt("votes");
                View v = li.inflate(R.layout.item_choice,null);
                ((TextView)v.findViewById(R.id.count_value)).setText(Integer.toString(votes));
                ProgressBar pb = v.findViewById(R.id.count_progress);
                float progress = 100*((float)votes/maxVotes);
                pb.setProgress(Math.round(progress));
                CheckBox cb = v.findViewById(R.id.vote_checkbox);
                cb.setTag(name + "-" + i);
                cb.setText(name);
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox clickedCB = (CheckBox)v.findViewById(R.id.vote_checkbox);
                        if(clickedCB.isChecked())
                            cleanSelectionCheckBox(clickedCB.getTag().toString());
                    }
                });

                choicesCheckboxs.add(cb);
                linearLayout.addView(v);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*  Works as a RadioGroup, deselect the other checkboxes
    * */
    private void cleanSelectionCheckBox(String tag) {
        for (CheckBox cb : choicesCheckboxs) {
            if (!cb.getTag().equals(tag)) {
                cb.setChecked(false);
            }
        }
    }

    /*  Build the Dialog for this activity to when there is no Internet Connection
    *
    * */
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

    /**Go back arrow, goes back to List fragment to list all question
     *
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
