package com.cuichen.jt808_oksocket.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class LocationUtils {


    /**
     * amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
     *                         amapLocation.getLatitude();//获取纬度
     *                         amapLocation.getLongitude();//获取经度
     *                         amapLocation.getAccuracy();//获取精度信息
     *                         amapLocation.getAltitude(); //高度
     *                         amapLocation.getSpeed(); //速度 m/s
     *                         amapLocation.getBearing(); //方向
     *                         amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
     *                         amapLocation.getCountry();//国家信息
     *                         amapLocation.getProvince();//省信息
     *                         amapLocation.getCity();//城市信息
     *                         amapLocation.getDistrict();//城区信息
     *                         amapLocation.getStreet();//街道信息
     *                         amapLocation.getStreetNum();//街道门牌号信息
     *                         amapLocation.getCityCode();//城市编码
     *                         amapLocation.getAdCode();//地区编码
     *                         amapLocation.getAoiName();//获取当前定位点的AOI信息
     *                         amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
     *                         amapLocation.getFloor();//获取当前室内定位的楼层
     *                         amapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
     * //获取定位时间
     *                         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     *                         Date date = new Date(amapLocation.getTime());
     *                         df.format(date)
     */

    /**
     * 获取位置信息
     *
     * @param callback
     */
    private AMapLocationClient mlocationClient;

    public void getLocationListener(final long interval, Context context) {
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
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {

                        if (interval <= 0) {
                            mlocationClient.stopLocation();
                            mlocationClient.onDestroy();
                        }
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError", "ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                    }
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationCacheEnable(true);
//        mlocationClient.startAssistantLocation(); //
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param
     * @return true 表示开启
     */
    public static boolean isLocationOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public boolean isLocationEnabled(ContentResolver contentResolver) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /**
     * 直接跳转至位置信息设置界面
     */
    public final int LOCATION_SOURCE_SETTINGS = 16;
    public void openLocation(Activity context) {
        Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivityForResult(intent,LOCATION_SOURCE_SETTINGS);
    }
}
