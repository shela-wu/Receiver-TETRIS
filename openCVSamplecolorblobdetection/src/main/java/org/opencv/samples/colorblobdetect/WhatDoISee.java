package org.opencv.samples.colorblobdetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Matth on 7/23/2017.
 */

public class WhatDoISee extends RelativeLayout {
    Paint colorPaint = new Paint();

    public void initialize() {
        colorPaint = new Paint();
        setBackgroundColor(Color.WHITE);
    }

    public WhatDoISee(Context context) {
        super(context);
        initialize();
    }

    public WhatDoISee(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public WhatDoISee(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(canvas.getWidth()/2,canvas.getHeight()/2,Math.min(canvas.getWidth()/2,canvas.getHeight()/2),colorPaint);
    }

    public void updatePreview(double[] color){
        colorPaint.setColor(Color.rgb((int)color[0],(int)color[1],(int)color[2]));
        invalidate();
    }

}
