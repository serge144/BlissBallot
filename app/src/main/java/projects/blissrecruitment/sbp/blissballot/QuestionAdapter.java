package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class QuestionAdapter extends ArrayAdapter<Question> {

    public QuestionAdapter(Context context, ArrayList<Question> questions){
        super(context,R.layout.item_question,questions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.item_question,parent,false);
        TextView questionText = (TextView) customView.findViewById(R.id.question_id);
        Question question = (Question)getItem(position);
        questionText.setText(Integer.toString(question.getId()));

        return customView;
    }

}
