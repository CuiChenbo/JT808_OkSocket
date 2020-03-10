package com.cuichen.jt808_sdk.sdk.jt808coding;

import android.text.TextUtils;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;
import java.util.ArrayList;
import java.util.List;

/*
 1、7E8001000501833996255200130014000200DB7E
 2、7E8001000501833996255200130014000200DB
 3、7E7E8001000501833996255200130014000200DB7E
 假如读取到这样三条数据，可发现数据1正常，数据2、3发生粘包情况；需要特殊处理；
 */
public class Jt808StickDataUtil {

    private static StringBuffer residueData; //遗留数据

    /**
     * 处理粘包数据；
     *
     * @param bytes 新数据
     * @return 根据7E处理粘包后的集合
     */
    public static List<byte[]> getStickData(byte[] bytes) {
        String Tag7E = "7E";
        List<byte[]> datas = new ArrayList<>();//创建返回的集合
        String data = HexUtil.byte2HexStrNoSpace(bytes); //把byte数组转十六进制字符串
        if (getStringCount(data, Tag7E) == 2
                && Tag7E.equals(data.substring(0, 2))
                && Tag7E.equals(data.substring(data.length() - 2))) { //7E一共出现两次，并且是在头尾处，则该数据不用处理；
            datas.add(bytes);
            return datas;
        }
        if (residueData != null && !TextUtils.isEmpty(residueData.toString())) {  //查看是否有遗留数据，如果有则把新数据拼接在遗留数据后面
            data = residueData.append(data).toString();
        }
        int start = data.indexOf(Tag7E); //从第一个标识位开始读数据
        int count7E = 0; //读取到标识位的次数
        StringBuffer currentHex = new StringBuffer(); //存放读取到的数据
        for (int i = start; i < data.length(); i = i + 2) {

            String currentByte;
            if (data.length() < i + 2) { //最后字数不足两位的情况
                currentByte = data.substring(i, i + 1);
            } else {
                currentByte = data.substring(i, i + 2);
            }

            if (Tag7E.equals(currentByte)) {
                //如果是7E的情况
                count7E++;
                currentHex.append(currentByte);
                if (count7E == 2) { //读取到第二个标识位时，把当前的数据归为一组；
                    count7E = 0;
                    datas.add(HexUtil.hexStringToByte(currentHex.toString()));
                    currentHex.delete(0, currentHex.length());
                }
            } else {
                //当有标识位的情况开始添加数据
                if (count7E == 1) currentHex.append(currentByte);
            }
        }
        residueData = currentHex; //全部遍历后剩余的数据和下次拼接
        return datas;
    }

    //获取一个字符串，查找这个字符串出现的次数;
    private static int getStringCount(String str, String key) {
        int count = 0;
        while (str.contains(key)) {
            count++;
            str = str.substring(str.indexOf(key) + key.length());
        }
        return count;
    }

    public static void clear() {
        if (residueData == null) return;
        residueData.delete(0, residueData.length());
    }

}
