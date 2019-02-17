package projects.blissrecruitment.sbp.blissballot;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionsListFragment extends ListFragment {


    public QuestionsListFragment() {
        // Required empty public constructor
    }


    private ArrayList<Question> questions;
    private ListView listView;
    private ListAdapter adapter;
    private SearchView searchView;

    private boolean fetchingRecords = false;
    private boolean searchMode = false;
    private boolean firstSearch = true; //used to determine if it was the first fetch in search mode

    private int limit = 10;
    private int currentOffset = 0;
    private String filter = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_questions_list,container,false);

        //detects when the user reaches the bottom of the list
        listView = v.findViewById(android.R.id.list);
        setScrollBottomListener(listView);

        //initialize the array related with the ListView
        questions = new ArrayList<Question>();

        //fetch the first 10 records
        JsonArrayRequest allQuestionsRequest = buildGetQuestionsRequest(limit,currentOffset,filter);
        BlissApiSingleton.getInstance(getContext()).addToRequestQueue(allQuestionsRequest);

        //set an adapter for the ListView
        adapter = new QuestionAdapter(getContext(),questions);
        setListAdapter(adapter);

        searchView = v.findViewById(R.id.search_question);
        searchView.setOnQueryTextListener(buildSearchListener());

        return v;
    }

    public JsonArrayRequest buildGetQuestionsRequest(Integer limit, Integer offset, String filter){

        String baseUrl = BlissApiSingleton.BLISS_BASE_ALL_QUESTIONS_REQUEST;
        String url = baseUrl + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset) + "&filter=" + filter;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                updateListView(processJSONArray(response));
                fetchingRecords = false; //used for the scroll-bottom detection of the list view
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO take care of error
                error.printStackTrace();
            }
        });

        return request;
    }

    private ArrayList<Question> processJSONArray(JSONArray response){

        ArrayList<Question> responseQuestions = new ArrayList<Question>();
        for(int i = 0 ; i < response.length() ; i++){
            JSONObject questionObject = null;
            try{
                questionObject = response.getJSONObject(i);
                int id = questionObject.getInt("id");
                String text = questionObject.getString("question");
                String img_url = questionObject.getString("image_url");
                String thumb_url = questionObject.getString("thumb_url");
                String date = questionObject.getString("published_at");
                JSONArray choices = questionObject.getJSONArray("choices");
                Question q = new Question(id,text,img_url,thumb_url,date,choices);
                responseQuestions.add(q);
            }catch (JSONException jex){
                jex.printStackTrace();
            }
        }
        return responseQuestions;
    }

    /*
        Updates the List view with new records
        @param lv   new records to add to the list view
    */
    private void updateListView(ArrayList<Question> newQuestions){

        if(searchMode){
            if(firstSearch){ // clean in the first search, but then when scrolling to the bottom, it must not clear the questions anymore
                questions.clear();
                firstSearch = false;
            }
            questions.addAll(newQuestions);
            ArrayAdapter<Question> aux = (ArrayAdapter<Question>)getListAdapter();
            aux.notifyDataSetChanged();
        }else{
            //append newQuestions to questions no need to clean
            questions.addAll(newQuestions);
            ArrayAdapter<Question> aux = (ArrayAdapter<Question>)getListAdapter();
            aux.notifyDataSetChanged();
        }
    }

    /*
        Sets a scroll listener that detects when the list as reached the bottom (user intents to load more records)
        @param lv   the ListView
    */
    public void setScrollBottomListener(ListView lv){

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //if its not fetching any records already then...
                if(!fetchingRecords){
                    int position = firstVisibleItem+visibleItemCount;
                    int listLimit = totalItemCount;
                    // Check if the bottom list has been reached
                    if (position >= listLimit && totalItemCount > 0) {
                        Log.d("APP_DEBUG","[INFO] List bottom reached");
                        fetchingRecords = true;
                        currentOffset = currentOffset + limit; //increase offset (+10)
                        JsonArrayRequest allQuestionsRequest = buildGetQuestionsRequest(limit,currentOffset,filter);
                        BlissApiSingleton.getInstance(getContext()).addToRequestQueue(allQuestionsRequest);
                    }
                }
            }
        });
    }

    /*  Builds the SearchView listener. The Search listener takes care of sending requests with filter to API
    *   @return the Listener
    */
    public SearchView.OnQueryTextListener buildSearchListener(){

        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listView.setSelection(0); //need to go up
                searchMode = true;
                firstSearch = true;
                filter = query;
                currentOffset = 0;
                Log.d("APP_DEBUG","[INFO] Now in search mode");
                JsonArrayRequest searchRequest = buildGetQuestionsRequest(limit,currentOffset,query);
                BlissApiSingleton.getInstance(getContext()).addToRequestQueue(searchRequest);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    searchMode = false;
                    firstSearch = true;
                    filter = "";
                    currentOffset = 0;
                    Log.d("APP_DEBUG","[INFO] Now in normal mode");
                    JsonArrayRequest searchRequest = buildGetQuestionsRequest(limit,currentOffset,filter); //update the list with first 10 recs
                    BlissApiSingleton.getInstance(getContext()).addToRequestQueue(searchRequest);
                }
                return false;
            }
        } ;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d("APP_DEBUG","[INFO] Clicked Item Position:"+ position + " id:" + id);

        Intent detailIntent = new Intent(getActivity(),DetailScreen.class);
        Question q = questions.get(position);
        detailIntent.putExtra("question",q);
        startActivity(detailIntent);

    }
}
