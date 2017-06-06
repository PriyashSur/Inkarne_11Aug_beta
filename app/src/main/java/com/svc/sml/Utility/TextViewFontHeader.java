package com.svc.sml.Utility;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.svc.sml.InkarneAppContext;
import com.svc.sml.R;

/**
 * Created by himanshu on 3/17/16.
 */
public class TextViewFontHeader extends TextView {

    public TextViewFontHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setType(context);
    }

    public TextViewFontHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        setType(context);
    }

    public TextViewFontHeader(Context context) {
        super(context);
        setType(context);
    }

    private void setType1(Context context){
        this.setTypeface(Typeface.createFromAsset(context.getAssets(),
                "foo.ttf"));
        this.setShadowLayer(1.5f, 5, 5, getContext().getResources().getColor(R.color.hint_text_color));
    }

    private void setType(Context context){
        this.setTypeface(InkarneAppContext.getInkarneTypeFaceHeaderJennaSue());
        //this.setTypeface(InkarneAppContext.getInkarneTypeFaceFutura());
        //this.setShadowLayer(1.5f, 5, 5, getContext().getResources().getColor(R.color.hint_text_color));
    }
}