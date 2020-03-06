package com.cuichen.jt808_oksocket.utils;

import android.os.Looper;
import android.widget.Toast;

import com.cuichen.jt808_oksocket.APP;


public class TU {
    private static Toast mToast;
    public static void s(String s){
        try{
        if (mToast == null) mToast = Toast.makeText(APP.mC,s,Toast.LENGTH_SHORT);
        mToast.setText(s);
         mToast.show();
    } catch (Exception e) {
        //解决在子线程中调用Toast的异常情况处理
        Looper.prepare();
        Toast.makeText(APP.mC, s, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    }
}
