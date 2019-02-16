package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class BlissApiSingleton  {

    public static String BLISS_HEALTH_REQUEST = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/health";
    public static final String BLISS_BASE_ALL_QUESTIONS_REQUEST = "https://private-618d57-blissrecruitmentapi.apiary-mock.com/questions";

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
