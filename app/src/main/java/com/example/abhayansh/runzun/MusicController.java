package com.example.abhayansh.runzun;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;

public class MusicController extends MediaController {

    ImageView shuff = new ImageView(getContext());
    ImageView repeat = new ImageView(getContext());

    public MusicController(Context context,boolean value) {
        super(context,value);
    }

    public void hide(){
        super.show(0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Context c = getContext();
            ((Activity) c).moveTaskToBack(true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        shuff.setImageResource(R.drawable.random);
        shuff.setPadding(10,10,10,10);
        shuff.setId(R.id.shuffle_button);
        shuff.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.NO_GRAVITY;
        params.topMargin = 47;
        params.leftMargin =100;
        params.height = 80;
        params.width = 80;
        addView(shuff, params);

        repeat.setImageResource(R.drawable.repeat);
        repeat.setPadding(10,10,10,10);
        repeat.setId(R.id.repeat_button);
        repeat.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.END;
        params1.topMargin = 47;
        params1.rightMargin = 100;
        params1.height = 80;
        params1.width = 80;
        addView(repeat, params1);

    }

}
