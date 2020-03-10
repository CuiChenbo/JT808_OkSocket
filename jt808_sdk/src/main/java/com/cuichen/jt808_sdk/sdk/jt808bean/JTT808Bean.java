package com.cuichen.jt808_sdk.sdk.jt808bean;


import com.cuichen.jt808_sdk.sdk.jt808utils.ByteUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;

import java.util.Arrays;


/**
 * 解析获注册返回的数据内容的808实体数据
 *
 * @author CCB
 */

public class JTT808Bean {

    //消息id
    private int msgId;
    //消息体属性
    private int msgBodyAttributes;
    //手机号
    private String phoneNumber;
    //消息流水号
    private int msgFlowNumber;
    //应答流水号
    private int replyFlowNumber;
    //应答结果
    private int replyResult;
    //数据包返回的消息id
    private int returnMsgId;
    //鉴权码
    private byte[] authenticationCode;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(byte[] msgId) {
        this.msgId = ByteUtil.bytes2Int(msgId);
    }

    public int getMsgBodyAttributes() {
        return msgBodyAttributes;
    }

    public void setMsgBodyAttributes(byte[] msgBodyAttributes) {
        this.msgBodyAttributes = ByteUtil.bytes2Int(msgBodyAttributes);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(byte[] phoneNumber) {
        this.phoneNumber = HexUtil.ByteToString(phoneNumber);
    }

    public int getMsgFlowNumber() {
        return msgFlowNumber;
    }

    public void setMsgFlowNumber(byte[] msgFlowNumber) {
        this.msgFlowNumber = ByteUtil.bytes2Int(msgFlowNumber);
    }

    public int getReplyFlowNumber() {
        return replyFlowNumber;
    }

    public void setReplyFlowNumber(byte[] replyFlowNumber) {
        this.replyFlowNumber = ByteUtil.bytes2Int(replyFlowNumber);
    }

    public int getReplyResult() {
        return replyResult;
    }

    public void setReplyResult(byte replyResult) {
        this.replyResult = ByteUtil.byte2Int(replyResult);
    }

    public byte[] getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(byte[] authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public int getReturnMsgId() {
        return returnMsgId;
    }

    public void setReturnMsgId(byte[] returnMsgId) {
        this.returnMsgId = ByteUtil.fourBytes2Int(returnMsgId);
    }

    @Override
    public String toString() {
        return "JTT808Bean{" +
                "msgId=" + msgId +
                ", msgBodyAttributes=" + msgBodyAttributes +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", msgFlowNumber=" + msgFlowNumber +
                ", replyFlowNumber=" + replyFlowNumber +
                ", replyResult=" + replyResult +
                ", returnMsgId=" + returnMsgId +
                ", authenticationCode=" + Arrays.toString(authenticationCode) +
                '}';
    }
}
