package projects.blissrecruitment.sbp.blissballot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;

public class ShareDialog extends DialogFragment {

    static ShareDialog newInstance(String query){
        ShareDialog sd = new ShareDialog();
        Bundle args = new Bundle();
        args.putString("query",query);
        sd.setArguments(args);
        return sd;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String query = getArguments().getString("query");
        builder.setTitle("Share");
        builder.setIcon(R.drawable.ic_share_blue);
        String message= "";
        if(query.equals(""))
            message = "Displaying the first results with no query, do you wish to share?";
        else
            message = "Currently showing results for query: " + query + ".\r\n Do you wish to share?";
        builder.setMessage(message);
        builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });


        return builder.create();
    }
}
