package com.cuichen.jt808_sdk.sdk.socketbean;

import com.cuichen.jt808_sdk.sdk.interfaces.SocketPulseListener;
import com.cuichen.jt808_sdk.sdk.jt808coding.JT808Directive;
import com.cuichen.jt808_sdk.sdk.SocketConfig;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IPulseSendable;


public class PulseData implements IPulseSendable {

    public PulseData(){}
    private SocketPulseListener socketPulseListener;
    public PulseData(SocketPulseListener socketPulseListener){
        this.socketPulseListener = socketPulseListener;
    }

    @Override
    public byte[] parse() {
        byte[] body = JT808Directive.heartPkg(SocketConfig.getmPhont());
        if (socketPulseListener != null) socketPulseListener.parse(body);
        return body;
    }

}