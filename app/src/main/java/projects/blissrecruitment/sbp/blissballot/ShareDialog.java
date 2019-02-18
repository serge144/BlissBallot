package projects.blissrecruitment.sbp.blissballot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO use only one buildShareDialog ( 2 is too much code )
public class ShareDialog extends DialogFragment {

    public static final int SHARE_FILTER_MODE = 100;
    public static final int SHARE_QUESTION_MODE = 101;

    OnShareListener mCallBack;
    public interface OnShareListener{
        public void onShareListener(String text);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallBack = (OnShareListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+" must implement OnShareListener");
        }
    }

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
    *
     */
    public AlertDialog buildShareQuestionDialog(final int questionId){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.share_dialog,null);
        builder.setView(v);
        builder.setTitle("Share");
        builder.setIcon(R.drawable.ic_share_blue);

        final EditText email = v.findViewById(R.id.email_edit_text);
        TextView uri = v.findViewById(R.id.uri_text);
        String uriString = BlissApiSingleton.BASE_DEEPLINK_QUESTION + "" +questionId;
        uri.setText(uriString);

        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailText = email.getText().toString();
                if(isEmailValid(emailText)){
                    String deeplink = "\"" + BlissApiSingleton.BASE_DEEPLINK_QUESTION + "" + questionId + "\"";
                    JsonObjectRequest shareQuestionRequest = buildShareRequest(emailText,deeplink);
                    BlissApiSingleton.getInstance(getContext()).addToRequestQueue(shareQuestionRequest);
                }else{
                    mCallBack.onShareListener("Please insert a valid email");
                }

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.share_dialog,null);
        builder.setView(v);
        builder.setTitle("Share");
        builder.setIcon(R.drawable.ic_share_blue);

        final EditText email = v.findViewById(R.id.email_edit_text);
        TextView uri = v.findViewById(R.id.uri_text);
        String uriString = BlissApiSingleton.BASE_DEEPLINK_FILTER + "" +query;
        uri.setText(uriString);

        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailText = email.getText().toString();
                if(isEmailValid(emailText)){
                    String deeplink = "\"" + BlissApiSingleton.BASE_DEEPLINK_FILTER + "" + query + "\"";
                    JsonObjectRequest shareQuestionRequest = buildShareRequest(emailText,deeplink);
                    BlissApiSingleton.getInstance(getContext()).addToRequestQueue(shareQuestionRequest);
                }else{
                    mCallBack.onShareListener("Please insert a valid email");
                }

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
                            if(response!= null){
                                if(response.getString("status").equals("OK"))
                                    mCallBack.onShareListener("Successfully share: "+ deeplink);
                                else
                                    mCallBack.onShareListener(BlissApiSingleton.ERROR_MESSAGE);
                            }else{
                                mCallBack.onShareListener(BlissApiSingleton.ERROR_MESSAGE);
                            }
                        } catch (JSONException e) {
                            mCallBack.onShareListener(BlissApiSingleton.ERROR_MESSAGE);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCallBack.onShareListener(BlissApiSingleton.ERROR_MESSAGE);
                    }
                });
        return jsonObjectRequest;
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
