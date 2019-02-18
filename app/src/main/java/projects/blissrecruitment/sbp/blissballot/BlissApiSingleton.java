package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Timer;

public class BlissApiSingleton  {

    public static String BLISS_HEALTH_REQUEST = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/health";
    public static final String BLISS_BASE_ALL_QUESTIONS_REQUEST = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/questions";
    public static final String SHARE_BASE_URL = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/share";
    public static final String BASE_DEEPLINK_QUESTION = "blissrecruitment://questions?question_id=";
    public static final String BASE_DEEPLINK_FILTER = "blissrecruitment://questions?question_filter=";
    public static final String ACTIVITY_CALLER = "activity_caller_id";

    private static BlissApiSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private BlissApiSingleton(Context context){
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized BlissApiSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BlissApiSingleton(context);
        }
        return mInstance;
    }

    public static boolean isConnected(Context ctx){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        Log.d("APP_DEBUG","[REQUEST]" + req.toString());
        getRequestQueue().add(req);
    }

}
