package projects.blissrecruitment.sbp.blissballot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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

    /*
    * This interface is used to attach some component that wishes to listen for the First Response Event
    * for example, the MainActivity implements this interface, this way it can send message back to the activity
    * */
    //=======================================================
    OnFirstResponseListener mCallback;

    public interface OnFirstResponseListener{
        public void onFirstResponseListener();
    }

    public void setOnFirstResponseListener(Activity activity){
        try{
            mCallback = (OnFirstResponseListener)activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnFirstResponseListener");
        }
    }

    //=======================================================

    public QuestionsListFragment() {
        // Required empty public constructor
    }

    private ArrayList<Question> questions;
    private ListView listView;
    private ListAdapter adapter;
    private SearchView searchView;

    private boolean fetchingRecords = false; //used to determine if its fetching records (when the list reaches bottom)
    private boolean firstSearch = true; //used to determine if it was the first fetch in the current query

    private int limit = 10;
    private int currentOffset = 0;
    private String filter = "";
    public static final String COMS_ID = "list_fragment_id";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_questions_list,container,false);

        //get search view UI and build the listeners
        searchView = v.findViewById(R.id.search_question);
        searchView.setOnQueryTextListener(buildSearchListener());
        //setSearchCleanButtonListener();

        //detects when the user reaches the bottom of the list
        listView = v.findViewById(android.R.id.list);
        setScrollBottomListener(listView);

        //initialize the array related with the ListView
        questions = new ArrayList<Question>();

        //DEEP LINK - get the query from MainActivity in case the app was opened with a deep link
        Bundle bundle = getArguments();
        processDeepLinkBundle(bundle);

        //fetch the first 10 records with current filter param
        JsonArrayRequest allQuestionsRequest = buildGetQuestionsRequest(limit,currentOffset,filter);
        BlissApiSingleton.getInstance(getContext()).addToRequestQueue(allQuestionsRequest);

        //set an adapter for the ListView
        adapter = new QuestionAdapter(getContext(),questions);
        setListAdapter(adapter);

        return v;
    }

    /*  Builds the request to fetch 10 questions, always controlling the offset based on the number of responses and the current filter
    * */
    public JsonArrayRequest buildGetQuestionsRequest(Integer limit, Integer offset, String filter){

        String baseUrl = BlissApiSingleton.BLISS_BASE_ALL_QUESTIONS_REQUEST;
        String url = baseUrl + "?limit=" + Integer.toString(limit) + "&offset=" + Integer.toString(offset) + "&filter=" + filter;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response!=null){
                    updateListView(Question.processJSONArray(response));//converts JSON to arrayList of questions and updates the list
                    currentOffset = currentOffset + response.length(); //if we got 5 records when we have already 10, then offset = 15
                    mCallback.onFirstResponseListener();
                    fetchingRecords = false; //used for the scroll-bottom detection of the list view
                }else{
                    Toast.makeText(getContext(),BlissApiSingleton.ERROR_MESSAGE,Toast.LENGTH_SHORT).show();
                }

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

    /*
        Updates the List view with new records
        @param lv   new records to add to the list view
    */
    private void updateListView(ArrayList<Question> newQuestions){

            if(firstSearch){ // clean in the first search, but then when scrolling to the bottom, it must not clear the questions anymore
                questions.clear();
                firstSearch = false;
            }
            questions.addAll(newQuestions);
            ArrayAdapter<Question> aux = (ArrayAdapter<Question>)getListAdapter();
            aux.notifyDataSetChanged();

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
                        //currentOffset = currentOffset + limit; //increase offset (+10)
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
                firstSearch = true;
                filter = query;
                currentOffset = 0;
                JsonArrayRequest searchRequest = buildGetQuestionsRequest(limit,currentOffset,filter);
                BlissApiSingleton.getInstance(getContext()).addToRequestQueue(searchRequest);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        } ;
    }

    /*  When a question_filter Deeplink is clicked, the main_activity receives it, and passes onto this Fragment
        in a Bundle
    * */
    public void processDeepLinkBundle(Bundle bundle){

        if(bundle != null && bundle.containsKey("question_filter")){
            filter = bundle.getString("question_filter");
            Log.d("APP_DEBUG","[DEEP-LINK] question_filter param is: " + filter);
            if(filter!=null) {
                if(filter.length()>0)
                    searchView.setQuery(filter, false);
            }else
                filter = "";
        }
    }

    public String getFilter(){
        return filter;
    }

    /*
    * When a question is clicked, we transfer the questions data to the Detail View on a bundle
    * instead of making another request
    * */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d("APP_DEBUG","[INFO] Clicked Item Position:"+ position + " id:" + id);

        //need to wrap the Parcelable inside a Bundle so it doesnt get ClassNotFoundException
        Question q = questions.get(position);
        Bundle b = new Bundle();
        b.putParcelable("question",q);
        Intent detailIntent = new Intent(getActivity(),DetailScreen.class);
        detailIntent.putExtra("bundle",b);
        detailIntent.putExtra(BlissApiSingleton.ACTIVITY_CALLER,COMS_ID);
        startActivity(detailIntent);
    }



}
