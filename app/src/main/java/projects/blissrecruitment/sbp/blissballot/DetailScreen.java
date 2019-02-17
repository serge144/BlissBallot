package projects.blissrecruitment.sbp.blissballot;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailScreen extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_screen);

        Bundle b  = getIntent().getBundleExtra("bundle");
        Question q = b.getParcelable("question");
        Log.d("APP_DEBUG","[INFO] Detail view, question id:" + q.getId());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Question Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);

        ImageView image = findViewById(R.id.image_detail);
        Button voteBtn = findViewById(R.id.vote_button);
        TextView questionText = findViewById(R.id.question_text_detail);
        RadioGroup radioView = findViewById(R.id.radio_group_detail);
        buildRadioGroup(radioView,q.getChoices());

        Picasso.get().load(q.getImgUrl()).transform(new CircleTransformation(45,0)).into(image);
        questionText.setText(q.getText());
    }

    public void buildRadioGroup(RadioGroup rgView, JSONArray choices){

        rgView.setOrientation(RadioGroup.VERTICAL);

        for(int i = 0 ; i < choices.length() ; i++){
            try {
                JSONObject choice = (JSONObject) choices.get(i);
                String name = choice.getString("choice");
                int votes = choice.getInt("votes");
                RadioButton rb = new RadioButton(this);
                rb.setText(" " + name);
                rgView.addView(rb);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
