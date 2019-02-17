package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_screen);

        //get the Question from the list activity
        Bundle b  = getIntent().getBundleExtra("bundle");
        question = b.getParcelable("question");
        Log.d("APP_DEBUG","[INFO] Detail view, question id:" + question.getId());

        //change actionbar title and set a return button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Question Detail");
        actionBar.setDisplayHomeAsUpEnabled(true); //return button

        //get UI refs
        ImageView image = findViewById(R.id.image_detail);
        Button voteBtn = findViewById(R.id.vote_button);
        TextView questionText = findViewById(R.id.question_text_detail);
        setVoteButtonListener(voteBtn);
        linearLayout = findViewById(R.id.linear_section);

        //insert the choices in RadioGroup
        buildRadioGroup(question.getChoices());

        //Request 600x400 image using picasso and update the rest of the UI elems
        Log.d("APP_DEBUG","[REQUEST-PICASSO] Url: "+ question.getImgUrl());
        Picasso.get().load(question.getImgUrl()).transform(new CircleTransformation(45,0)).into(image);
        questionText.setText(question.getText());



    }

    private void setVoteButtonListener(Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
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

    private CheckBox someChoiceIsChecked(){
        for(CheckBox cb : choicesCheckboxs)
            if(cb.isChecked())
                return cb;
        return null;
    }

    private void updateChoiceVoteCount(CheckBox cb){
        String name = cb.getText().toString();
        question.vote(name);

    }

    private JsonObjectRequest buildPutQuestionRequest(){

        String baseUrl = BlissApiSingleton.BLISS_BASE_ALL_QUESTIONS_REQUEST;
        String url = baseUrl +"/" + question.getId();
        JSONObject quesObject = question.toJSON();
        JsonObjectRequest jrequest = new JsonObjectRequest(Request.Method.PUT, url, quesObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("APP_DEBUG","[RESPONSE]" + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO Handle error
            }
        });
        return jrequest;
    }


    private void buildRadioGroup(JSONArray choices){

        //radioView.setOrientation(RadioGroup.VERTICAL);
        LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0 ; i < choices.length() ; i++){
            try {
                JSONObject choice = (JSONObject) choices.get(i);
                String name = choice.getString("choice");
                int votes = choice.getInt("votes");
                View v = li.inflate(R.layout.item_choice,null);
                ((TextView)v.findViewById(R.id.count_value)).setText(Integer.toString(votes));
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

    private void cleanSelectionCheckBox(String tag) {
        for (CheckBox cb : choicesCheckboxs) {
            if (!cb.getTag().equals(tag)) {
                cb.setChecked(false);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
