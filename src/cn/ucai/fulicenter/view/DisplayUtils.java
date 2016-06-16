package cn.ucai.fulicenter.view;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

public class DisplayUtils {

    public static void initBack(final Activity activity) {
        LinearLayout clickArea = (LinearLayout) activity.findViewById(R.id.backClickArea);

        clickArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish();
                }
            });
    }

    public static void initBackWithTitle(final Activity activity, String title) {
        TextView tvTitle = (TextView) activity.findViewById(R.id.tv_head_title);
        tvTitle.setText(title);
        initBack(activity);
    }
}
