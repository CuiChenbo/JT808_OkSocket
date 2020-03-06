package com.cuichen.jt808_oksocket.utils;

import com.google.common.primitives.Bytes;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MsgPackageUtils {

   private static byte SIGN = 0x7e;
    private static byte SIGN_D = 0x7d;
    private static byte SIGN_02 = 0x02;
    private static byte SIGN_01 = 0x01;

    private static boolean nextIsNotAdd = false; //下一次是否不添加

//    接收消息时：转义还原——>验证校验码——>解析消息
    public static byte[] read(byte[] bytes) {

        List<Byte> newbytes = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == SIGN) {
                //是标记位就不添加
            }else if (nextIsNotAdd) { //本地遍历不添加
                nextIsNotAdd = false;
            } else if (bytes[i] == SIGN_D) { //需要转义
                if (bytes.length > i + 1) {
                    if (bytes[i + 1] == SIGN_01) {
                        newbytes.add((byte) 0x7d);
                        nextIsNotAdd = true; //转义后 下次不添加
                    } else if (bytes[i + 1] == SIGN_02) {
                        newbytes.add((byte) 0x7e);
                        nextIsNotAdd = true;  //转义后 下次不添加
                    }
                }
            } else {
                newbytes.add(bytes[i]);
            }
        }

       byte checkCode = newbytes.get(newbytes.size()-1); //获取检验码
       newbytes.remove(newbytes.size()-1); //去掉最后一位检验码
       byte[] resultByte = Bytes.toArray(newbytes); //需要返回的bute数组
       if (checkCode == getXor(resultByte)){
          //通过
       }else {
          //未通过
       }
        return resultByte;
    }


    //    发送消息时：消息封装——>计算并填充校验码——>转义
    public static byte[] write(String str) {
        byte[] msgId = HexUtils.hexStringToBytes("0002"); //消息ID
        byte[] msgType = HexUtils.hexStringToBytes("0000"); //消息体属性
        byte[] msgMobile = HexUtils.hexStringToBytes("013838642235"); //终端手机号
        byte[] msgCount = HexUtils.hexStringToBytes("0001"); //消息流水号
//        byte[] msgPackage = HexUtils.hexStringToBytes("0000"); //消息包封装项

        ByteBuffer headBuffer = ByteBuffer.allocate(12);
        headBuffer.order(ByteOrder.BIG_ENDIAN);
        headBuffer.put(msgId);
        headBuffer.put(msgType);
        headBuffer.put(msgMobile);
        headBuffer.put(msgCount);
//        headBuffer.put(msgPackage);
        byte[] head = headBuffer.array(); //获得消息头

        byte[] body = str.getBytes(Charset.defaultCharset()); //原消息（转义前的消息）
        ByteBuffer originalByte = ByteBuffer.allocate(head.length + body.length);
        originalByte.order(ByteOrder.BIG_ENDIAN);
        originalByte.put(head);
        originalByte.put(body);

        byte xor = getXor(originalByte.array()); //消息头和消息体异或

        List<Byte> sendByte = new ArrayList<>();
        sendByte.add(SIGN);
        for (int i = 0; i < originalByte.array().length; i++) { //遍历添加内容（消息头+消息体）
            if (originalByte.get(i) == SIGN) {
                sendByte.add(SIGN_D);
                sendByte.add(SIGN_02);
            } else if (originalByte.get(i) == SIGN_D) {
                sendByte.add(SIGN_D);
                sendByte.add(SIGN_01);
            } else {
                sendByte.add(originalByte.get(i));
            }
        }
        if (xor == SIGN) { //添加异或
            sendByte.add(SIGN_D);
            sendByte.add(SIGN_02);
        } else if (xor == SIGN_D) {
            sendByte.add(SIGN_D);
            sendByte.add(SIGN_01);
        } else {
            sendByte.add(xor);
        }
        sendByte.add(SIGN); // 添加尾标识位
        L.c(HexUtils.formatHexString(Bytes.toArray(sendByte)));
        return Bytes.toArray(sendByte);

    }

    private static byte getXor(byte[] data) {
        byte temp = 0;
        for (int i = 1; i < data.length; i++) {
            temp ^= data[i];
        }
        return temp;
    }
}
