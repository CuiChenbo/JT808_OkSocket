package com.cuichen.jt808_oksocket.service;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cuichen.jt808_oksocket.APP;
import com.cuichen.jt808_oksocket.service.service_interface.OnJt808LocationBackList;
import com.cuichen.jt808_oksocket.service.service_interface.OnServiceWorking;
import com.fanjun.keeplive.config.KeepLiveService;
import com.cuichen.jt808_sdk.sdk.SocketManager;
import com.cuichen.jt808_sdk.sdk.interfaces.OnGetResetDeviceList;
import com.cuichen.jt808_sdk.sdk.interfaces.OnResetDeviceList;
import com.cuichen.jt808_sdk.sdk.interfaces.SocketActionListener;
import com.cuichen.jt808_sdk.sdk.interfaces.StartLocationBackList;
import com.cuichen.jt808_sdk.sdk.jt808bean.Generate808andSeqBean;
import com.cuichen.jt808_sdk.sdk.jt808bean.Jt808MapLocation;
import com.cuichen.jt808_sdk.utils.L;
import com.cuichen.jt808_sdk.utils.ListUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 运力位置上报的长连接保活服务
 * 开启位置上报
 * 停止位置上报
 */
public class Jt808SocketKeepService implements KeepLiveService {

    private SocketManager socketManager;

    @Override
    public void onWorking() {
        if (socketManager == null) {
            socketManager = SocketManager.getInstance();
            socketManager.init(APP.mC);
        }
        if (onServiceWorking != null) onServiceWorking.onWorking();
    }

    @Override
    public void onStop() {
        if (socketManager != null)socketManager.unRegisterReceiver();
    }

    public boolean isConnect(){
        if (socketManager == null){
            return false;
        }else {
           return socketManager.isConnect();
        }
    }

    public void setJt808Config(String phont , String terminalId , String orderId) {
        if (socketManager == null)return;
        socketManager.setJt808Phont(phont);
        socketManager.setJt808TerminalId(terminalId);
        socketManager.setJt808OrderId(orderId);
    }

    public void setJt808OrderId(String orderId) {
        if (socketManager == null)return;
        socketManager.setJt808OrderId(orderId);
    }

    private OnJt808LocationBackList onJt808LocationBackList;
    public void startLocation(final OnJt808LocationBackList onJt808LocationBackList) {
        if (socketManager == null)return;
        this.onJt808LocationBackList = onJt808LocationBackList;
        socketManager.setOnGetResetDeviceList(new OnGetResetDeviceList() {

            @Override
            public void getResetDevice(OnResetDeviceList t) {
                                socketManager.goLogout();
                                t.resetDevice(true);
                            }
        });
        socketManager.startLocation(new StartLocationBackList() {
            @Override
            public void onBack(int code, String msg) {
              if (code == 0){
                  getLocationListener(APP.mC , 5000,false);
              }
                if (onJt808LocationBackList != null)onJt808LocationBackList.onCallBack(code,msg);
            }
        });
    }

    private OnServiceWorking onServiceWorking;
    public void setOnServiceWorkingList(OnServiceWorking onServiceWorking){
        this.onServiceWorking = onServiceWorking;
    }

    /**
     * 获取位置信息
     *
     * @param callback
     */
    private AMapLocationClient mlocationClient;

    /**
     * 位置上报
     *
     * @param context
     * @param interval
     * @param isBatch  是否批量上报
     */
    private void getLocationListener(Context context, final long interval, final boolean isBatch) {
        mlocationClient = new AMapLocationClient(context);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        if (interval <= 0) {
            mLocationOption.setOnceLocation(true); //只定位一次
        } else {
            mLocationOption.setOnceLocation(false); //连续定位
            mLocationOption.setInterval(interval);
        }
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                reportMapLocation(amapLocation, isBatch);
                if (interval <= 0) {
                    mlocationClient.stopLocation();
                    mlocationClient.onDestroy();
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationCacheEnable(true);
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    private List<Jt808MapLocation> locationList = new ArrayList<>();
    private void reportMapLocation(AMapLocation amapLocation, boolean isBatch) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
                Date date = new Date(amapLocation.getTime());
                L.cc(amapLocation.toString());
                Jt808MapLocation jt808MapLocation = new Jt808MapLocation(amapLocation.getLatitude()
                        , amapLocation.getLongitude()
                        , amapLocation.getAltitude()
                        , amapLocation.getSpeed()
                        , amapLocation.getBearing()
                        , amapLocation.getAccuracy()
                        , df.format(date));

                if (socketManager.isConnect()) {
                    socketManager.sendReportLocation(jt808MapLocation);
                   if (!ListUtils.isEmpty(locationList)){
                       socketManager.sendBatchReportLocation(locationList);
                       locationList.clear();
                   }
                } else {
                    locationList.add(0,jt808MapLocation); //如果当前连接时断开状态，则先记录位置信息
                }

            } else {
                if (onJt808LocationBackList == null) onJt808LocationBackList.onCallBack(1001 , amapLocation.getErrorInfo());
                Log.e("AmapError", "ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
            }
        }
    }

    public void setSocketListener(SocketActionListener socketListener){
        socketManager.setSocketActionListener(socketListener);
    }

    public List<Generate808andSeqBean> getNoReplyDatas(){
       return socketManager.getNoReplyDatas();
    }
    /**
     * 停止位置上报，断开长连接，断开位置监听
     */
    public void goStopLocation() {
        if (socketManager != null)socketManager.stopLocation();
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
    }


}