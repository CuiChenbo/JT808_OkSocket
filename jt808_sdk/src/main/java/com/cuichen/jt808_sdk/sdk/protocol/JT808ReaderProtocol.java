package com.cuichen.jt808_sdk.sdk.protocol;

import com.cuichen.jt808_sdk.sdk.jt808bean.Header808Bean;
import com.cuichen.jt808_sdk.sdk.jt808coding.JTT808Coding;
import com.cuichen.jt808_sdk.sdk.SocketConfig;
import com.cuichen.jt808_sdk.oksocket.core.protocol.IReaderProtocol;

import java.nio.ByteOrder;
import java.util.Arrays;

public class JT808ReaderProtocol implements IReaderProtocol {
//    @Override
//    public int getHeaderLength() {
//        return SocketConfig.getSocketHeadLength();
//    }
//
//    @Override
//    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
//        //去除0X7E
//        byte[] del7eBytes = Arrays.copyOfRange(header, 1, header.length);
//        // 把JT808数据头解析成为实体
//        Header808Bean header808 = JTT808Coding.resolve808ToHeader(del7eBytes);
//        // 从消息头实体中获取消息体长度+2（检验码、包尾0X7E
//        return header808.getBodyAttr().getBodyLength() + 2;
//    }

//    已经重写接收方法  不用设置消息头长度和消息体长度
    @Override
    public int getHeaderLength() {
        return 3;
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        return 3;
    }
}
