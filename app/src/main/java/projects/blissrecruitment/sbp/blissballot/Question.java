package projects.blissrecruitment.sbp.blissballot;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Question implements Parcelable  {

    private int id;
    private String text;
    private String img_url;
    private String thumb_url;
    private String published_at;
    private Calendar publishDate;
    private JSONArray choices;
    private int maxVotes;


    public Question(int id, String text, String img_url, String thumb_url, String published_at, JSONArray choices)
    {
        this.id = id;
        this.text = text;
        this.img_url = img_url;
        this.thumb_url = thumb_url;
        this.published_at = published_at;
        this.choices = choices;
        this.maxVotes = 0;

        setCalendar();
        setMaxVotes();
    }

    //Used to transfer question data between ListFragment -> DetailView
    //this way we dont need to make request for the question
    public Question(Parcel parcel){
        this.id = parcel.readInt();
        this.text = parcel.readString();
        this.img_url = parcel.readString();
        this.thumb_url = parcel.readString();
        this.published_at = parcel.readString();
        try {
            this.choices = new JSONArray(parcel.readString());
        } catch (JSONException e) {
            //TODO take care of error
            e.printStackTrace();
        }

        setCalendar();
        setMaxVotes();
    }

    //the maxvotes to display a count progress bar for each choice 100*(votes/maxVotes)
    public int getMaxVotes(){
        return maxVotes;
    };

    public void setMaxVotes(){

        for(int i = 0 ; i < choices.length() ; i ++){
            try {
                JSONObject jobj = (JSONObject)choices.get(i);
                int mVotes;
                if((mVotes = jobj.getInt("votes")) > maxVotes)
                    maxVotes = mVotes;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void setCalendar(){
        DateFormat df = new SimpleDateFormat(BlissApiSingleton.BLISS_API_DATE_FORMAT);
        publishDate = Calendar.getInstance();
        try {
            publishDate.setTime(df.parse(published_at));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayDateString(Calendar calendar){

        String mString = calendar.getDisplayName(Calendar.MONTH,Calendar.SHORT,Locale.getDefault());//,Calendar.SHORT, Locale.getDefault());
        mString = mString +" " + calendar.get(Calendar.DAY_OF_MONTH);
        mString = mString +", " + calendar.get(Calendar.YEAR)
                + " at " + calendar.get(Calendar.HOUR_OF_DAY)
                +"h"+calendar.get(Calendar.MINUTE);
        return mString;

    }

    public Calendar getCalendar(){
        return publishDate;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImgUrl() {
        return img_url;
    }

    public void setImgUrl(String img_url) {
        this.img_url = img_url;
    }

    public String getThumbUrl() {
        return thumb_url;
    }

    public void setThumbUrl(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getPublishedAt() {
        return published_at;
    }

    public void setPublished_at(String published_at) {
        this.published_at = published_at;
    }

    public JSONArray getChoices() {
        return choices;
    }

    public void setChoices(JSONArray choices) {
        this.choices = choices;
    }

    public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>(){
        @Override
        public Question createFromParcel(Parcel source) {
            return new Question(source);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[0];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
        dest.writeString(img_url);
        dest.writeString(thumb_url);
        dest.writeString(published_at);
        dest.writeString(choices.toString());
    }

    public void vote(String choice){
        Log.d("APP_DEBUG","[INFO-VOTING] Vote counts BEFORE:" + choices.toString());
        for(int i = 0; i < choices.length(); i++){
            try {
                JSONObject mChoice = (JSONObject) choices.get(i);
                if(mChoice.getString("choice").equals(choice)){
                    int votes = mChoice.getInt("votes");
                    votes = votes +1;
                    mChoice.put("votes",votes);
                    break;
                }
            } catch (JSONException e) {
                //TODO take care of this error
                e.printStackTrace();
            }
        }
        Log.d("APP_DEBUG","[INFO-VOTING] Vote counts AFTER:" + choices.toString());

    }

    public JSONObject toJSON(){

        JSONObject jo = new JSONObject();
        try {
            jo.put("id",id);
            jo.put("text",text);
            jo.put("image_url",img_url);
            jo.put("thumb_url",thumb_url);
            jo.put("published_at",published_at);
            jo.put("choices",choices);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo;
    }

    public static ArrayList<Question> processJSONArray(JSONArray response){

        ArrayList<Question> responseQuestions = new ArrayList<Question>();
        for(int i = 0 ; i < response.length() ; i++){
            JSONObject questionObject = null;
            try{
                questionObject = response.getJSONObject(i);
                Question q = Question.processJSONObject(questionObject);
                responseQuestions.add(q);
            }catch (JSONException jex){
                jex.printStackTrace();
                //TODO handle exception
            }
        }
        return responseQuestions;
    }

    public static Question processJSONObject(JSONObject questionObject){
        Question q = null;
        try{
            int id = questionObject.getInt("id");
            String text = questionObject.getString("question");
            String img_url = questionObject.getString("image_url");
            String thumb_url = questionObject.getString("thumb_url");
            String date = questionObject.getString("published_at");
            JSONArray choices = questionObject.getJSONArray("choices");
            q = new Question(id,text,img_url,thumb_url,date,choices);
        }catch (JSONException jexc){
            jexc.printStackTrace();
            //TODO handle exception
        }
        return q;

    }

}
