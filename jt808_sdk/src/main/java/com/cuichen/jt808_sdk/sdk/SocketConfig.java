package com.cuichen.jt808_sdk.sdk;


public class SocketConfig {

//    public static String  socketIp = "10.130.151.86" ;// IpUtils.getIPAddress(APP.mC);
    public static String  socketIp = "117.186.24.38" ;// IpUtils.getIPAddress(APP.mC);
    public static String  socketPort = "8314" ;

    // Jt808 数据加密方式 占3个Bit位 0表示不加密
    public static String JT808HEADER_ENCRYPT = "000";
    // Jt808 保留位 占2个Bit位
    public static String JT808HEADER_RESERVE = "00";

    //消息协议头的长度(okSocketService只接收固定4字节)
    private static int SocketHeadLength = 13;

    //本地保存鉴权码的KEY
    public static String JT808AuthCode = "SP_JT808AUTHCODE";

    public static String mManufacturerId = "CCCBB";
    public static String mTerminalModel = "ANDROID0000000000000";

    private static String mPhont = "018339962222";  // 手机号
    private static String mTerminalId = "AAAAAAA";  // 终端号
    private static String mOrderId = "1234567";  // 订单号
//    public static String mPhont = "015651821852";

    public static int getSocketHeadLength(){
        return SocketHeadLength;
    }

    //发送消息流水号
    private static int SocketMsgCount = 0;
    public static int getSocketMsgCount(){
      return ++SocketMsgCount;
    }
    public static void ClearSocketMsgCount(){
         SocketMsgCount = 0 ;
    }

    public static String getmPhont() {
        return mPhont;
    }

    public static void setmPhont(String mPhont) {
        SocketConfig.mPhont = mPhont;
    }

    public static String getmTerminalId() {
        return mTerminalId;
    }

    public static void setmTerminalId(String mTerminalId) {
        SocketConfig.mTerminalId = mTerminalId;
    }

    public static String getmOrderId() {
        return mOrderId;
    }

    public static void setmOrderId(String mOrderId) {
        SocketConfig.mOrderId = mOrderId;
    }

}
