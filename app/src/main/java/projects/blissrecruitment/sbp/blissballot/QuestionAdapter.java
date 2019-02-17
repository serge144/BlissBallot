package projects.blissrecruitment.sbp.blissballot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class QuestionAdapter extends ArrayAdapter<Question> {

    public QuestionAdapter(Context context, ArrayList<Question> questions){
        super(context,R.layout.item_question,questions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.item_question,parent,false);
        Question question = (Question)getItem(position);
        TextView questionText = (TextView) customView.findViewById(R.id.question_id);
        ImageView thumbPreview = (ImageView) customView.findViewById(R.id.thumb_preview);

        //BitmapDrawable drawable = (BitmapDrawable) thumbPreview.getDrawable();
        //Bitmap bitmap = drawable.getBitmap();
        //Bitmap result = getRoundedCornerBitmap(bitmap);
        //thumbPreview.setImageBitmap(result);
        Log.d("APP_DEBUG","[REQUEST-PICASSO] Url: "+ question.getThumbUrl());
        Picasso.get().load(question.getThumbUrl()).transform(new CircleTransformation(45,0)).into(thumbPreview);
        questionText.setText(Integer.toString(question.getId()));

        return customView;
    }



    /*Technique to round the bitmap obtained from this url:
    * @credit @url: https://ruibm.com/2009/06/16/rounded-corner-bitmaps-on-android/
    * @param bitmap the originial bitmap
    * @return output the processed bitmap
    * */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 45;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
