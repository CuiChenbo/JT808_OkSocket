package com.cuichen.jt808_sdk.sdk.jt808coding;

import com.cuichen.jt808_sdk.sdk.SocketConfig;
import com.cuichen.jt808_sdk.sdk.jt808bean.Jt808MapLocation;
import com.cuichen.jt808_sdk.sdk.jt808utils.BCD8421Operater;
import com.cuichen.jt808_sdk.sdk.jt808utils.BitOperator;
import com.cuichen.jt808_sdk.sdk.jt808utils.ByteUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class JT808Directive {

    /**
     * 终端注册
     *
     * @param manufacturerId 制造商 ID
     * @param terminalModel  终端型号
     * @param terminalId     终端 ID
     * @return
     */
    public static byte[] register(String manufacturerId, String terminalModel, String terminalId) {
        //省域 ID
        byte[] p = BitOperator.numToByteArray(31, 2);
        //省域 市县域 ID
        byte[] c = BitOperator.numToByteArray(72, 2);
        //制造商 ID
        byte[] mId = manufacturerId.getBytes();
        //终端型号
        byte[] tmId = terminalModel.getBytes();
        //终端 ID
        byte[] tId = terminalId.getBytes();
        //车牌颜色
        byte[] s = {0};
        // 车辆标识
        byte[] vin = "LSFAM630000000008".getBytes();

        return ByteUtil.byteMergerAll(p, c, mId, tmId, tId, s,vin);
    }

    /**
     * 位置信息汇报 ***废弃
     *
     * @param alarmType  报警/事件类型 0x03：车距过近报警 0x08：自动刹车报警
     * @param terminalId 终端id
     * @param lat        纬度
     * @param lng        经度
     * @param speed      速度
     * @return
     */
    public static byte[] locationReport(byte alarmType, String terminalId, long lat, long lng, int alarmId, double speed) {
        byte[] alarm = {0, 0, 0, 0};
        //32 位二进制 从高到低位
        String radix2State = "00000000000000000000000000000010";
        //2进制转int 在装4个字节的byte
        byte[] state = ByteUtil.int2Bytes(Integer.parseInt(radix2State, 2));
        //DWORD经纬度
        byte[] latb = ByteUtil.longToDword(lat);
        byte[] lngb = ByteUtil.longToDword(lng);
        byte[] gaoChen = {0, 0};
        byte[] speedb = BitOperator.numToByteArray((int) (speed * 10), 2);
        byte[] orientation = {0, 0};
        //bcd时间
        byte[] bcdTime = TimeUtils.getBcdTime();
        //位置信息附加项
        byte[] gjfzjsData = advancedDriverAssistance(alarmType, terminalId, latb, lngb, bcdTime, alarmId, speed);
        return ByteUtil.byteMergerAll(alarm, state, latb, lngb, gaoChen, speedb, orientation, bcdTime, gjfzjsData);
    }

    /**
     * 参考文档《江苏道路运输车辆主动安全智能防控系统通讯协议规范》
     * 表 4‑15高级驾驶辅助报警信息数据格式
     * 生成位置附加项数据 ***废弃
     *
     * @param alarmType  报警/事件类型 (0x03,0x08)
     * @param terminalId 终端id
     * @param lat        纬度
     * @param lng        经度
     * @param alarmId    报警id
     * @param speed      速度
     */
    public static byte[] advancedDriverAssistance(byte alarmType, String terminalId, byte[] lat, byte[] lng,
                                                  byte[] bcdTime, int alarmId, double speed) {
        //报警ID转4个字节的byte
        byte[] id = ByteUtil.int2Bytes(alarmId);
        //速度转byte
        byte sb = ByteUtil.int2Byte((int) (speed * 10));
        byte[] gjjsfz = {0x01, alarmType, 0x04, 0, 0, 0, 0, 0, sb, 0, 0};
        //车辆状态
        byte[] state = {0, 0};
        byte[] alarmData = ByteUtil.byteMergerAll(id, gjjsfz, lat, lng, bcdTime, state);
        //报警标识号格式
        byte[] terminal = terminalId.getBytes();
        byte[] end = {0, 0, 0};
        byte[] bytes = ByteUtil.byteMergerAll(alarmData, terminal, bcdTime, end);
        //附加信息id 长度
        byte[] extensionId = {0x64, (byte) bytes.length};
        return ByteUtil.byteMergerAll(extensionId, bytes);
    }

    /**
     * 单独上报经纬度 ***废弃
     *
     * @param lat 纬度
     * @param lng 经度
     * @return
     */
    public static byte[] reportLatLng(long lat, long lng) {
        byte[] alarm = {0, 0, 0, 0};
        //32 位二进制 从高到低位
        String radix2State = "00000000000000000000000000000010";
        //2进制转int 在装4个字节的byte
        byte[] state = ByteUtil.int2Bytes(Integer.parseInt(radix2State, 2));
        //DWORD经纬度
        byte[] latb = ByteUtil.longToDword(lat);
        byte[] lngb = ByteUtil.longToDword(lng);
        byte[] gaoChen = {0, 0};
        byte[] speedb = {0, 0};
        byte[] orientation = {0, 0};
        //bcd时间
        byte[] bcdTime = TimeUtils.getBcdTime();
        //位置信息附加项
        return ByteUtil.byteMergerAll(alarm, state, latb, lngb, gaoChen, speedb, orientation, bcdTime);
    }

    /**
     * 位置信息汇报
     *
     * @param lat      纬度
     * @param lng      经度
     * @param altitude 高度
     * @param speed    速度
     * @param bearing  角度/方向
     * @param accuracy  精度
     * @param time     时间
     * @return
     */
    public static byte[] reportLocation(double lat, double lng, double altitude, float speed, float bearing, float accuracy, String time) {
        byte[] alarm = {0, 0, 0, 0};
//        byte[] state = {0, 0, 0, 0};
        byte[] state = stateLocationData(lat,lng);
        //DWORD经纬度
        double pow106 = Math.pow(10, 6);
        double lat106 = lat * pow106;
        double lng106 = lng * pow106;
        byte[] latb = ByteUtil.longToDword(Math.round(lat106));
        byte[] lngb = ByteUtil.longToDword(Math.round(lng106));
        // WORD 高度 速度 方向
        byte[] gaoChen = BitOperator.numToByteArray((int) altitude, 2);
        byte[] speedb = BitOperator.numToByteArray((int) (speed * 3.6), 2);
        byte[] orientation = BitOperator.numToByteArray((int) bearing, 2);
        //bcd时间
        byte[] bcdTime = BCD8421Operater.string2Bcd(time);
        //位置信息附加项
        byte[] additionLocation = additionLocationData(SocketConfig.getmOrderId(),(int) altitude,String.valueOf(accuracy));
        return ByteUtil.byteMergerAll(alarm, state, latb, lngb, gaoChen, speedb, orientation, bcdTime,additionLocation);
    }

    public static byte[] reportLocation(Jt808MapLocation jtData){
       return reportLocation(jtData.getLat()
               ,jtData.getLng()
               ,jtData.getAccuracy()
               ,jtData.getSpeed()
               ,jtData.getBearing()
               ,jtData.getAccuracy()
               ,jtData.getTime());
    }

    /**
     * 位置信息状态项
     *
     * @param
     * @return
     */
    private static byte[] stateLocationData(double lat, double lng) {
        String state = "00";
        state = state + (lat < 0 ? "1" : "0");
        state = state + (lng < 0 ? "1" : "0");
        state = state + "0000000000000000000000000000";
        byte[] stateByte = ByteUtil.int2DWord(Integer.parseInt(state, 2));
        return stateByte;
    }

    /**
     * 位置信息附加项
     * @param order 订单号
     * @param altitude 高度
     * @param accuracy 精度
     * @return
     */
    private static byte[] additionLocationData(String order, int altitude, String accuracy) {
        byte[] orderType = new byte[2]; //订单号
        orderType[0] = (byte) (0xE1);
        orderType[1] = ByteUtil.int2Byte(order.getBytes().length);
        byte[] orderMsg = ByteUtil.byteMergerAll(orderType, order.getBytes());

        byte[] altitudeType = new byte[3]; //高度的正负
        altitudeType[0] = (byte) (0xE2);
        altitudeType[1] = (byte) (0x01);
        altitudeType[2] = (byte) (altitude < 0 ? 0x31 : 0x30);

        byte[] accuracyType = new byte[2]; //订单号
        accuracyType[0] = (byte) (0xE1);
        accuracyType[1] = ByteUtil.int2Byte(accuracy.getBytes().length);
        byte[] accuracyMsg = ByteUtil.byteMergerAll(accuracyType, accuracy.getBytes());

        return ByteUtil.byteMergerAll(orderMsg, altitudeType, accuracyMsg);
    }

    /**
     * 批量位置信息汇报
     * @return
     */
    public static byte[] batchReportLocation(List<byte[]> locations) {
        byte[] counts = ByteUtil.int2Word(locations.size()); //数据项个数
        byte[] batchType = {0}; //位置数据类型 0：正常位置批量汇报，1：盲区补报

        List<byte[]> formatLocations = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            //取出每一条的位置数据，然后拼接（位置汇报数据体长度+位置汇报数据体）
            byte[] cLocation = locations.get(i);
            byte[] clocationLength = ByteUtil.int2Word(cLocation.length); //位置汇报数据体长度
            formatLocations.add(ByteUtil.byteMergerAll(clocationLength, cLocation));
        }

        byte[] batchLocations = ByteUtil.byteMergerAll(formatLocations); //位置汇报数据体
        return ByteUtil.byteMergerAll(counts, batchType, batchLocations);
    }

    /**
     * 发送心跳
     */
    public static byte[] heartPkg(String phone) {
        return JTT808Coding.generate808(0x0002, phone, new byte[]{});
    }

//    /**
//     * 上传图片
//     */
//    public synchronized void uploadImage(long lat, long lng, byte[] imageBytes) {
//        String s = "lat=" + lat + " lng=" + lng;
//        byte[] id = ByteUtil.longToDword(System.currentTimeMillis());
//        byte[] type = {0, 0, 1, 0};
//        byte[] location = JTT808Coding.reportLatLng(lat, lng);
//
//        if (imageBytes == null) return;
//        int length = imageBytes.length;
//        //每个包的大小
//        double everyPkgSize = 900.d;
//        //分包的总包数
//        long totalPkg = Math.round(Math.ceil(length / everyPkgSize));
////        Log.e(TAG, "上传图片---->" + s + "  图片总大小：" + length + "  分包的总包数：" + totalPkg);
//        for (int i = 1; i <= totalPkg; i++) {
//            int end = (int) (i * everyPkgSize);
//            if (end >= length) {
//                end = length;
//            }
//            byte[] sendByte;
//            byte[] bytes = Arrays.copyOfRange(imageBytes, (int) ((i - 1) * everyPkgSize), end);
//            if (i == 1) {
//                byte[] allData = ByteUtil.byteMergerAll(id, type, location, bytes);
//                sendByte = JTT808Coding.generate808(0x0801, MainActivity.PHONE,
//                        allData, 1, totalPkg, i);
//            } else {
//                sendByte = JTT808Coding.generate808(0x0801, MainActivity.PHONE,
//                        bytes, 1, totalPkg, i);
//            }
//            SocketClient.getInstance().sendMsg(sendByte);
//        }
////        Log.e(TAG, "上传图片完成---->");
//    }
//
//    /**
//     * 上传视频 对协议进行扩展 5代表mp4格式
//     */
//    public synchronized void uploadMp4(long lat, long lng, byte[] mp4Bytes) {
//        String s = "lat=" + lat + " lng=" + lng;
//        byte[] id = ByteUtil.longToDword(System.currentTimeMillis());
//        byte[] type = {2, 5, 1, 0};
//        byte[] location = JTT808Coding.reportLatLng(lat, lng);
//        if (mp4Bytes == null) return;
//        int length = mp4Bytes.length;
//        //每个包的大小
//        double everyPkgSize = 900.d;
//        //分包的总包数
//        long totalPkg = Math.round(Math.ceil(length / everyPkgSize));
////        Log.e(TAG, "上传视频---->" + s + "  视频总大小：" + length + "  分包的总包数：" + totalPkg);
//        for (int i = 1; i <= totalPkg; i++) {
//            int end = (int) (i * everyPkgSize);
//            if (end >= length) {
//                end = length;
//            }
//            byte[] sendByte;
//            byte[] bytes = Arrays.copyOfRange(mp4Bytes, (int) ((i - 1) * everyPkgSize), end);
//            if (i == 1) {
//                byte[] allData = ByteUtil.byteMergerAll(id, type, location, bytes);
//                sendByte = JTT808Coding.generate808(0x0801, MainActivity.PHONE,
//                        allData, 1, totalPkg, i);
//            } else {
//                sendByte = JTT808Coding.generate808(0x0801, MainActivity.PHONE,
//                        bytes, 1, totalPkg, i);
//            }
//            SocketClient.getInstance().sendMsg(sendByte);
//        }
////        Log.e(TAG, "上传图片完成---->");
//
//    }
}
