package projects.blissrecruitment.sbp.blissballot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String NO_COMS_BROADCAST = "com.blissrecruitment.broadcast.NO_COMS_BROADCAST";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("APP_DEBUG","[BROADCAST-RECEIVER] Got action: " + intent.getAction());

        if (intent.getAction().equals(CONNECTIVITY_CHANGE)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Log.d("APP_DEBUG", "[CONNECTION] Connected");
            } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Log.d("APP_DEBUG", "[CONNECTION] No Internet Connection");

                //@url https://trinitytuts.com/pass-data-from-broadcast-receiver-to-activity-without-reopening-activity/
                Intent i = new Intent(NO_COMS_BROADCAST);
                context.sendBroadcast(i);
            }
        }
    }
}
