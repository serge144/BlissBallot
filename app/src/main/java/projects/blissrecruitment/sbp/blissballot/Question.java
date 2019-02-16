package projects.blissrecruitment.sbp.blissballot;

import org.json.JSONArray;

public class Question {

    private int id;
    private String text;
    private String img_url;
    private String thumb_url;
    private String published_at;
    private JSONArray choices;


    public Question(int id, String text, String img_url, String thumb_url, String published_at, JSONArray choices){
        this.id = id;
        this.text = text;
        this.img_url = img_url;
        this.thumb_url = thumb_url;
        this.published_at = published_at;
        this.choices = choices;
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


}
