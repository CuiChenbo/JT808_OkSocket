package com.cuichen.jt808_oksocket.bean;

import java.text.SimpleDateFormat;

public class LogBean {
    public String mTime;
    public String mLog;
    public String mWho;

    public LogBean(long time, String log) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mTime = format.format(time);
        mLog = log;
    }
}