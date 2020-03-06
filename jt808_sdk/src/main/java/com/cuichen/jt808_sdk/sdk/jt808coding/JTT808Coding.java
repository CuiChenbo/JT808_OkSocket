package com.cuichen.jt808_sdk.sdk.jt808coding;


import com.cuichen.jt808_sdk.sdk.SocketConfig;
import com.cuichen.jt808_sdk.sdk.exceptions.SocketManagerException;
import com.cuichen.jt808_sdk.sdk.jt808bean.Generate808andSeqBean;
import com.cuichen.jt808_sdk.sdk.jt808bean.Header808Bean;
import com.cuichen.jt808_sdk.sdk.jt808bean.JTT808Bean;
import com.cuichen.jt808_sdk.sdk.jt808utils.BCD8421Operater;
import com.cuichen.jt808_sdk.sdk.jt808utils.BitOperator;
import com.cuichen.jt808_sdk.sdk.jt808utils.ByteUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;

import java.util.Arrays;

/**
 * 文件名:    JTT808Coding
 * 创建时间:  2018/11/12 on 11:09
 * 描述:     TODO
 * 数据格式：  标识位 | 消息头 | 消息体 | 校验码 | 标识位
 * 步骤： 注册，鉴权，握手
 *
 * @author
 */
/*
方法注释：
Arrays.copyOfRange(T[ ] original,int from,int to)
将一个原始的数组original，从下标from开始复制，复制到上标to，生成一个新的数组。
注意这里包括下标from，不包括上标to
 */

public class JTT808Coding {

    private static int FLOW_NUM = 0;
    private static final String FLAG_7E = " 7E";
    private static final String FLAG_7D = " 7D";

    /**
     * 包装808数据
     *
     * @param msgId   消息id
     * @param phone   终端手机号
     * @param msgBody 消息体
     * @return
     */
    public static byte[] generate808(int msgId, String phone, byte[] msgBody) {
        return generate808(msgId, phone, msgBody, 0, 0, 0);
    }

    /**
     * 包装808数据,分包消息
     *
     * @param msgId      消息id
     * @param phone      终端手机号
     * @param msgBody    消息体
     * @param subpackage 是否分包 0:不分包 1:分包
     * @param totalPkg   总包数
     * @param pkgNo      当前序号
     */
    public static byte[] generate808(int msgId, String phone, byte[] msgBody, int subpackage, long totalPkg, long pkgNo) {

        //=========================标识位==================================//
        byte[] flag = new byte[]{0x7E};

        //=========================消息头==================================//
        //[0,1]消息Id
        byte[] msgIdb = BitOperator.numToByteArray(msgId, 2);
        //[2,3]消息体属性
        byte[] msgBodyAttributes = msgBodyAttributes(msgBody.length, subpackage);
        //[4,9]终端手机号 BCD[6](占6位)
        byte[] terminalPhone = BCD8421Operater.string2Bcd(phone);
        //[10,11]流水号
        byte[] flowNum = BitOperator.numToByteArray(SocketConfig.getSocketMsgCount(), 2);
        //[12]消息包封装项 不分包 就没有
        //[12]消息包封装项 不分包 就没有
        byte[] msgHeader;
        if (subpackage == 1) {
            //分包
            byte[] totalPkgB = BitOperator.numToByteArray(totalPkg, 2);
            byte[] pkgNob = BitOperator.numToByteArray(pkgNo, 2);
            msgHeader = ByteUtil.byteMergerAll(msgIdb, msgBodyAttributes, terminalPhone, flowNum, totalPkgB, pkgNob);
        } else {
            msgHeader = ByteUtil.byteMergerAll(msgIdb, msgBodyAttributes, terminalPhone, flowNum);
        }
        //=========================数据合并（消息头，消息体）=====================//
        byte[] bytes = ByteUtil.byteMergerAll(msgHeader, msgBody);

        //=========================计算校验码==================================//
        String checkCodeHexStr = HexUtil.getBCC(bytes);
        byte[] checkCode = HexUtil.hexStringToByte(checkCodeHexStr);

        //=========================合并:消息头 消息体 校验码 得到总数据============//
        byte[] AllData = ByteUtil.byteMergerAll(bytes, checkCode);

        //=========================转义 7E和7D==================================//
        // 转成16进制字符串
        String hexStr = HexUtil.byte2HexStr(AllData);
        // 替换 7E和7D
        String replaceHexStr = hexStr.replaceAll(FLAG_7D, " 7D 01")
                .replaceAll(FLAG_7E, " 7D 02")
                // 最后去除空格
                .replaceAll(" ", "");

        //替换好后 转回byte[]
        byte[] replaceByte = HexUtil.hexStringToByte(replaceHexStr);

        //=========================最终传输给服务器的数据==================================//
        return ByteUtil.byteMergerAll(flag, replaceByte, flag);
    }



    public static Generate808andSeqBean generate808orSeqNo(int msgId, String phone, byte[] msgBody) {
        return generate808orSeqNo(msgId, phone, msgBody,0,0,0);
    }
    /**
     * 包装808数据,分包消息
     *
     * @param msgId      消息id
     * @param phone      终端手机号
     * @param msgBody    消息体
     * @param subpackage 是否分包 0:不分包 1:分包
     * @param totalPkg   总包数
     * @param pkgNo      当前序号
     * @return 包装好的数据和消息序号
     */
    public static Generate808andSeqBean generate808orSeqNo(int msgId, String phone, byte[] msgBody, int subpackage, long totalPkg, long pkgNo) {

        //=========================标识位==================================//
        byte[] flag = new byte[]{0x7E};

        //=========================消息头==================================//
        //[0,1]消息Id
        byte[] msgIdb = BitOperator.numToByteArray(msgId, 2);
        //[2,3]消息体属性
        byte[] msgBodyAttributes = msgBodyAttributes(msgBody.length, subpackage);
        //[4,9]终端手机号 BCD[6](占6位)
        byte[] terminalPhone = BCD8421Operater.string2Bcd(phone);
        //[10,11]流水号
        int seqNo = SocketConfig.getSocketMsgCount();
        byte[] flowNum = BitOperator.numToByteArray(seqNo, 2);
        //[12]消息包封装项 不分包 就没有
        //[12]消息包封装项 不分包 就没有
        byte[] msgHeader;
        if (subpackage == 1) {
            //分包
            byte[] totalPkgB = BitOperator.numToByteArray(totalPkg, 2);
            byte[] pkgNob = BitOperator.numToByteArray(pkgNo, 2);
            msgHeader = ByteUtil.byteMergerAll(msgIdb, msgBodyAttributes, terminalPhone, flowNum, totalPkgB, pkgNob);
        } else {
            msgHeader = ByteUtil.byteMergerAll(msgIdb, msgBodyAttributes, terminalPhone, flowNum);
        }
        //=========================数据合并（消息头，消息体）=====================//
        byte[] bytes = ByteUtil.byteMergerAll(msgHeader, msgBody);

        //=========================计算校验码==================================//
        String checkCodeHexStr = HexUtil.getBCC(bytes);
        byte[] checkCode = HexUtil.hexStringToByte(checkCodeHexStr);

        //=========================合并:消息头 消息体 校验码 得到总数据============//
        byte[] AllData = ByteUtil.byteMergerAll(bytes, checkCode);

        //=========================转义 7E和7D==================================//
        // 转成16进制字符串
        String hexStr = HexUtil.byte2HexStr(AllData);
        // 替换 7E和7D
        String replaceHexStr = hexStr.replaceAll(FLAG_7D, " 7D 01")
                .replaceAll(FLAG_7E, " 7D 02")
                // 最后去除空格
                .replaceAll(" ", "");

        //替换好后 转回byte[]
        byte[] replaceByte = HexUtil.hexStringToByte(replaceHexStr);

        //=========================最终传输给服务器的数据==================================//
        byte[] generate808 = ByteUtil.byteMergerAll(flag, replaceByte, flag);
        Generate808andSeqBean generate808orSeqBean = new Generate808andSeqBean(seqNo,generate808,System.currentTimeMillis());
        return generate808orSeqBean;
    }

    /**
     * 解析服务器返回的808数据包
     *
     * @return not null有用的数据包 null 数据校验失败
     */
    public static byte[] check808Data(byte[] bytes) {
        //去除包头 包尾的7E标识 在去掉校验码
        byte[] del7eBytes = Arrays.copyOfRange(bytes, 1, bytes.length - 2);
        //获取数据上的校验码
        String checkCode = HexUtil.byte2HexStr(Arrays.copyOfRange(bytes, bytes.length - 2, bytes.length - 1));
        // 转成16进制字符串
        String hexStr = HexUtil.byte2HexStr(del7eBytes);
        // 替换  7D 02->7E  7D 01->7D
        String replaceHexStr = hexStr.replaceAll(" 7D 02", " 7E")
                .replaceAll(" 7D 01", " 7D")
                // 最后去除空格
                .replaceAll(" ", "");
        byte[] data = HexUtil.hexStringToByte(replaceHexStr);
        //计算校验码
        String sumCode = HexUtil.getBCC(data);
        if (!checkCode.equals(sumCode)) {
            return null;
        }
        return data;

    }

    /**
     * 解析服务器返回的808数据包
     *
     * @return not null有用的数据包 null 数据校验失败
     */
    public static byte[] check808DataThrows(byte[] bytes) throws SocketManagerException {
        //去除包头 包尾的7E标识 在去掉校验码
        byte[] del7eBytes = Arrays.copyOfRange(bytes, 1, bytes.length - 2);
        //获取数据上的校验码
        String checkCode = HexUtil.byte2HexStr(Arrays.copyOfRange(bytes, bytes.length - 2, bytes.length - 1));
        // 转成16进制字符串
        String hexStr = HexUtil.byte2HexStr(del7eBytes);
        // 替换  7D 02->7E  7D 01->7D
        String replaceHexStr = hexStr.replaceAll(" 7D 02", " 7E")
                .replaceAll(" 7D 01", " 7D")
                // 最后去除空格
                .replaceAll(" ", "");
        byte[] data = HexUtil.hexStringToByte(replaceHexStr);
        //计算校验码
        String sumCode = HexUtil.getBCC(data);
        if (!checkCode.equals(sumCode)) {
            throw new SocketManagerException("计算校验码失败： "+HexUtil.byte2HexStr(bytes));
        }
        return data;

    }

    /**
     * 解析808数据解析成实体类
     *
     * @param bytes 去除了7E的数据
     *              eg:80 01 00 05 04 00 45 50 34 32 00 00 00 00    01 02 00
     * @return
     */
    public static JTT808Bean resolve808(byte[] bytes) {
        JTT808Bean jtt808 = new JTT808Bean();
        byte[] msgId = Arrays.copyOfRange(bytes, 0, 2);
        byte[] msgBodyAttributes = Arrays.copyOfRange(bytes, 2, 4);
        byte[] phone = Arrays.copyOfRange(bytes, 4, 10);
        byte[] msgFlowNum = Arrays.copyOfRange(bytes, 10, 12);
        jtt808.setMsgId(msgId);
        jtt808.setMsgBodyAttributes(msgBodyAttributes);
        jtt808.setPhoneNumber(phone);
        jtt808.setMsgFlowNumber(msgFlowNum);
        //以上是消息头↑

        byte[] replyFlowNum = Arrays.copyOfRange(bytes, 12, 14);
        jtt808.setReplyFlowNumber(replyFlowNum); //应答流水号
        //注册或重置鉴权码数据包
        if (0x8100 == jtt808.getMsgId() || 0x8101  == jtt808.getMsgId()) {
            byte replyResult = bytes[14];
            byte[] auth = Arrays.copyOfRange(bytes, 15, bytes.length);
            jtt808.setReplyResult(replyResult);
            jtt808.setAuthenticationCode(auth);
            //鉴权数据／心跳数据包
        } else if (0x8001 == jtt808.getMsgId()) {
            byte[] returnMsgId = Arrays.copyOfRange(bytes, 14, 16);
            //最后一位为鉴权结果 0 通过 !=0 不通过
            byte result = bytes[bytes.length - 1];
            jtt808.setReplyResult(result);
            jtt808.setReturnMsgId(returnMsgId);
        }
        return jtt808;
    }

    /**
     * 解析808数据解析成实体类
     *
     * @param bytes 去除了7E的数据
     *              eg:80 01 00 05 04 00 45 50 34 32 00 00 00 00 01 02 00
     * @return
     */
    public static Header808Bean resolve808ToHeader(byte[] bytes) {
        Header808Bean header808 = new Header808Bean();
        byte[] msgId = Arrays.copyOfRange(bytes, 0, 2);
        byte[] msgBodyAttributes = Arrays.copyOfRange(bytes, 2, 4);
        byte[] phone = Arrays.copyOfRange(bytes, 4, 10);
        byte[] msgFlowNum = Arrays.copyOfRange(bytes, 10, 12);

        header808.setMsgID(ByteUtil.bytes2Int(msgId));
        header808.setMobile(HexUtil.byte2HexStrNoSpace(phone));
        header808.setSeqNO(ByteUtil.bytes2Int(msgFlowNum));
        Header808Bean.BodyAttrBean  bodyAttrBean = new Header808Bean.BodyAttrBean();
        bodyAttrBean.setBodyLength(BitOperator.msgHeaderGetBodyLength(msgBodyAttributes)); //获取消息体长度
        String bodyAtteBit = ByteUtil.byteToBit(msgBodyAttributes[0]); //取到前8位
        bodyAttrBean.setEncrypt(!"000".equals(bodyAtteBit.substring(3,6))); //获取是否加密位
        bodyAttrBean.setSplit(!"0".equals(bodyAtteBit.substring(2,3))); //获取是否分包位
        header808.setBodyAttr(bodyAttrBean);
        return header808;
    }

    /**
     * 生成消息体属性
     *
     * @param subpackage [13]是否分包 0:不分包 1:分包
     */
    private static byte[] msgBodyAttributes(int msgLength, int subpackage) {
        byte[] length = BitOperator.numToByteArray(msgLength, 2);
        //[0,9]消息体长度
        String msgBodyLength = "" +
                //第一个字节最后2bit
                +(byte) ((length[0] >> 1) & 0x1) + (byte) ((length[0] >> 0) & 0x1)
                //第二个字节8bit
                + (byte) ((length[1] >> 7) & 0x1) + (byte) ((length[1] >> 6) & 0x1)
                + (byte) ((length[1] >> 5) & 0x1) + (byte) ((length[1] >> 4) & 0x1)
                + (byte) ((length[1] >> 3) & 0x1) + (byte) ((length[1] >> 2) & 0x1)
                + (byte) ((length[1] >> 1) & 0x1) + (byte) ((length[1] >> 0) & 0x1);
        //[10,12]数据加密方式 0 表示不加密
        String encryption = SocketConfig.JT808HEADER_ENCRYPT;
        //[13]分包
        String subpackageB = String.valueOf(subpackage);
        //[14,15]保留位
        String reserve = SocketConfig.JT808HEADER_RESERVE;
        String msgAttributes = reserve + subpackageB + encryption + msgBodyLength;
        // 消息体属性
        int msgBodyAttr = Integer.parseInt(msgAttributes, 2);
        return BitOperator.numToByteArray(msgBodyAttr, 2);
    }
}
