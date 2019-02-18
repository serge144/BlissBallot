package projects.blissrecruitment.sbp.blissballot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareDialog extends DialogFragment {

    public static final int SHARE_FILTER_MODE = 100;
    public static final int SHARE_QUESTION_MODE = 101;

    static ShareDialog newInstance(String query, int shareMode){
        ShareDialog sd = new ShareDialog();
        Bundle args = new Bundle();
        args.putString("query",query);
        args.putInt("sharemode",shareMode);
        sd.setArguments(args);
        return sd;
    }

    static ShareDialog newInstance(int questionId, int shareMode){
        ShareDialog sd = new ShareDialog();
        Bundle args = new Bundle();
        args.putInt("questionId",questionId);
        args.putInt("sharemode",shareMode);
        sd.setArguments(args);
        return sd;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        int shareMode = getArguments().getInt("sharemode");
        switch (shareMode){
            case ShareDialog.SHARE_FILTER_MODE:
                return buildShareFilterDialog(getArguments().getString("query"));
            case ShareDialog.SHARE_QUESTION_MODE:
                return buildShareQuestionDialog(getArguments().getInt("questionId"));
        }
        return new AlertDialog.Builder(getActivity()).create();
    }

    /*
    * Builds the share dialog when in the ListFragment
     */
    public AlertDialog buildShareQuestionDialog(final int questionId){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Share");
        builder.setIcon(R.drawable.ic_share_blue);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Email");
        input.setLayoutParams(lp);

        builder.setView(input);
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();
                String deeplink =  "\"" + BlissApiSingleton.BASE_DEEPLINK_QUESTION + "" + questionId + "\"";
                JsonObjectRequest shareQuestionRequest = buildShareRequest(email,deeplink);
                BlissApiSingleton.getInstance(getContext()).addToRequestQueue(shareQuestionRequest);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }

    /*
     * Builds the share dialog when in the ListFragment (search)
     */
    public AlertDialog buildShareFilterDialog(final String query){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Share");
        builder.setIcon(R.drawable.ic_share_blue);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Email");
        input.setLayoutParams(lp);
        builder.setView(input);

        String message= "";
        if(query.equals(""))
            message = "Displaying the first results with no query, do you wish to share?";
        else
            message = "Currently showing results for query: " + query + ".\r\n Do you wish to share?";
        builder.setMessage(message);
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();
                String deeplink = "\"" + BlissApiSingleton.BASE_DEEPLINK_FILTER + "" + query + "\"";
                JsonObjectRequest shareQuestionRequest = buildShareRequest(email,deeplink);
                BlissApiSingleton.getInstance(getContext()).addToRequestQueue(shareQuestionRequest);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }

    /*
    * Builds a request to the /share endpoint using the apropriate deeplink
    * deeplink is either blissrecruitment://question_filter= OR blissrecruitment://question_id=
    * */
    public JsonObjectRequest buildShareRequest(String email, final String deeplink){
        String baseUrl = BlissApiSingleton.SHARE_BASE_URL;
        String url = baseUrl + "?destination_email=" + email + "&content_url="+ deeplink;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("APP_DEBUG","[RESPONSE] "+ response.toString());
                        try {
                            if(response.getString("status").equals("OK")){
                                Toast.makeText(getContext(),"Successfully Shared: "+ deeplink, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(),"Something went wrong.",Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getContext(),"Something went wrong.",Toast.LENGTH_SHORT).show();
                    }
                });
        return jsonObjectRequest;

    }
}
