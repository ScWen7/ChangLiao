package scwen.com.changliao;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by xxh on 2018/6/12.
 */

public class TestMerage extends LinearLayout {
    public TestMerage(Context context) {
        this(context, null);
    }

    public TestMerage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestMerage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);

        View inflate =
                LayoutInflater.from(getContext()).inflate(R.layout.test, this, true);
    }


}
