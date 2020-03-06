package com.cuichen.jt808_sdk.utils;

import android.util.Log;

import com.cuichen.jt808_sdk.sdk.jt808coding.JTT808Coding;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;

import java.util.Arrays;

public class L {
    public static void cc(String msg) {
        cc("CCB", msg);
    }

    public static void cc(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void jt808(byte[] bytes) {
        cc("CCB", "↓\nReadHeadBean(消息头实例化):" + JTT808Coding.resolve808ToHeader(bytes)
                + "\nReadHead(消息头):" + HexUtil.byte2HexStr(Arrays.copyOfRange(bytes, 0, 12))
                + "\nReadBody(消息体):" + HexUtil.byte2HexStr(Arrays.copyOfRange(bytes, 12, bytes.length)));
    }

}