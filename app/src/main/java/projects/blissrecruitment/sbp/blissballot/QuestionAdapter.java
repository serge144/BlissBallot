package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class QuestionAdapter extends ArrayAdapter<Question> {

    public QuestionAdapter(Context context, ArrayList<Question> questions){
        super(context,R.layout.item_question,questions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.item_question,parent,false);

        Question question = (Question)getItem(position);
        TextView questionText = (TextView) customView.findViewById(R.id.question_text);
        TextView questionDate = (TextView) customView.findViewById(R.id.question_date);
        ImageView thumbPreview = (ImageView) customView.findViewById(R.id.thumb_preview);

        Log.d("APP_DEBUG","[REQUEST-PICASSO] Url: "+ question.getThumbUrl());
        Picasso.get().load(question.getThumbUrl()).transform(new CircleTransformation(45,0)).into(thumbPreview);
        questionText.setText(question.getText());
        questionDate.setText(question.getDisplayDateString(question.getCalendar()));

        return customView;
    }





}
