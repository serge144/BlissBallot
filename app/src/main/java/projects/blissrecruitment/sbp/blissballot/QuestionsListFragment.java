package projects.blissrecruitment.sbp.blissballot;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionsListFragment extends ListFragment {


    public QuestionsListFragment() {
        // Required empty public constructor
    }

    ListAdapter adapter;
    Question question;
    ArrayList<Question> questions;
    ArrayAdapter<Question> array_aux;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Question q1 = new Question("whats the time");
        Question q2 = new Question("weather");
        questions = new ArrayList<Question>();
        questions.add(q1);
        questions.add(q2);

        View v = inflater.inflate(R.layout.fragment_questions_list,container,false);

        /*searchView = v.findViewById(R.id.searchView2);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        */
        adapter = new QuestionAdapter(getContext(),questions);
        setListAdapter(adapter);
        array_aux = (ArrayAdapter<Question>) this.getListAdapter();
        return v;
    }

}
