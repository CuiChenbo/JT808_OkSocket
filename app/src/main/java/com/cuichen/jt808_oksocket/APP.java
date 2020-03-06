package com.cuichen.jt808_oksocket;

import android.app.Application;

public class APP extends Application {

    public static APP mC;

    @Override
    public void onCreate() {
        super.onCreate();
        mC = this;
    }
}
