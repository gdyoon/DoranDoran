package com.doraesol.dorandoran;

import android.app.Activity;
import android.widget.Toast;

/*
두 번 뒤로가기시 종료
 */
public class BackPressCloseHandler extends Activity {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;
    public BackPressCloseHandler(MenuActivity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            toast.cancel();
        }
    }



    private void showGuide() {
        toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 터치하시면 종료됩니다.",
                Toast.LENGTH_SHORT);
        toast.show();
    }
}