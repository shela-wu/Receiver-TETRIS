package org.opencv.samples.colorblobdetect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Matth on 6/27/2017.
 */

public class TargetView extends RelativeLayout {
    Paint border = new Paint();
    Paint textPaint=new Paint();
    String text="";
    public TargetView(Context context) {
        super(context);
    }
    public TargetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TargetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void SetUp(int color, String text){
        border.setStrokeWidth(10f);
        border.setColor(color);
        border.setStyle(Paint.Style.STROKE);
        this.setBackgroundColor(Color.TRANSPARENT);
        textPaint.setColor(Color.WHITE);
        textPaint.setShadowLayer(5,5,5,Color.BLACK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(50f);
        this.text=text;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(5,5,getWidth()-5,getHeight()-5,border);
        canvas.drawText(text,10,(getHeight())-10,textPaint);
    }
}
