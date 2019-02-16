package projects.blissrecruitment.sbp.blissballot;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class DetailScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_screen);

        Intent intent = getIntent();
        Question q = intent.getParcelableExtra("question");
        Log.d("APP_DEBUG","[INFO] Detail view, question id:" + q.getId());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Question Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
