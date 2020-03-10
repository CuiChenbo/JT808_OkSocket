package com.cuichen.jt808_sdk.sdk.interfaces;

public interface SocketActionListener {

    void onSocketReadResponse(byte[] bytes);

    void onSocketWriteResponse(byte[] bytes);

    void onPulseSend(byte[] bytes); // 注意：子线程回调

    void onSocketDisconnection();

    void onSocketConnectionSuccess();

    void onSocketConnectionFailed();
}