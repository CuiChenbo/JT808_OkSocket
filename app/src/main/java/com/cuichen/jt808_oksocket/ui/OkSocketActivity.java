package com.cuichen.jt808_oksocket.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.cuichen.jt808_oksocket.R;
import com.cuichen.jt808_oksocket.adapter.LogAdapter;
import com.cuichen.jt808_oksocket.bean.LogBean;
import com.cuichen.jt808_oksocket.utils.L;
import com.cuichen.jt808_oksocket.utils.TU;
import com.cuichen.jt808_sdk.sdk.SocketConfig;
import com.cuichen.jt808_sdk.sdk.SocketManagerTest;
import com.cuichen.jt808_sdk.sdk.exceptions.SocketManagerException;
import com.cuichen.jt808_sdk.sdk.jt808bean.Header808Bean;
import com.cuichen.jt808_sdk.sdk.jt808bean.JTT808Bean;
import com.cuichen.jt808_sdk.sdk.jt808coding.JT808Directive;
import com.cuichen.jt808_sdk.sdk.jt808coding.JTT808Coding;
import com.cuichen.jt808_sdk.sdk.jt808utils.ByteUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtils;
import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.NoneReconnect;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.widget.Toast.LENGTH_SHORT;

public class OkSocketActivity extends AppCompatActivity {

    private EditText etIp, etPort, etContent;
    private Button btConnect, btSend, btSendPluse , btZhuCe , btAuto , btZhuXiao , btLocation, btLocation2;
    private SwitchCompat switch_reconnect;
    private RecyclerView rv;

    private LogAdapter logAdapter = new LogAdapter();

    //暂时存储注册返回的鉴权码
    private byte[] authCode;

    private SocketManagerTest socketManager;

    /**
     * 初始化Socket
     */
    private void connect() {
       String ip = etIp.getText().toString().trim();
       int port = Integer.valueOf(etPort.getText().toString().trim());
        try {
            socketManager.connect(ip, port, new SocketActionAdapter() {
                @Override
                public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
                    logPrint("Connection：连接成功");
                    btConnect.setText("已连接");
                }

                @Override
                public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
                    logPrint("Connection：连接失败");
                    L.c("Connection：连接失败");
                    btConnect.setText("连接失败");
                    e.fillInStackTrace();
                }

                @Override
                public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
                    logPrint("Connection：连接已断开");
                    L.c("Connection：连接已断开");
                    btConnect.setText("连接已断开");
                    e.fillInStackTrace();
                }

                @Override
                public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
                    byte[] body = ByteUtil.byteMergerAll(data.getHeadBytes() ,data.getBodyBytes()) ;
                    L.c("Read(原数据):" + HexUtils.formatHexString(body));
                    logPrint("Read(原数据):" + HexUtils.formatHexString(body));
                    byte[] bytes ;
                    try {
                        bytes = JTT808Coding.check808DataThrows(body);
                    } catch (SocketManagerException e) {
                        e.printStackTrace();
                        logPrint("Read:" + e.getMessage());
                        return;
                    }
                    L.c("Read(去除包头尾的7E标识和校验码):" + HexUtils.formatHexString(bytes));
                    if (bytes != null) {
                        JTT808Bean bean = JTT808Coding.resolve808(bytes);
                        Header808Bean head808 = JTT808Coding.resolve808ToHeader(bytes);
                        L.c("ReadHead:" + head808.toString());
                        L.c("ReadBody:" + HexUtil.byte2HexStr(data.getBodyBytes()));

                        if (bean.getMsgId() == 0x8100){
                            //是注册回复得消息
                            authCode = bean.getAuthenticationCode();
                        }else if (bean.getMsgId() == 0x8001) {
                            if (bean.getReturnMsgId() == 0x0002) { //心跳
                                if (socketManager != null) socketManager.feedPulse();
                            }
                        }
                    }
//                    logPrint("Read(编译后):" + HexUtils.formatHexString(bytes));
                }

                @Override
                public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
//            String str = new String(data.parse(), Charset.forName("utf-8"));
                    String s = HexUtils.formatHexString(data.parse());
                    L.c("Write:" + s);
                    logPrint("Write:" + s);
                }

                @Override
                public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
                    logPrint("PulseSend: 发送心跳成功");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void init() {
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(manager);
        rv.setAdapter(logAdapter);

        etContent.setText("C");
        etIp.setText(SocketConfig.socketIp);
        etPort.setText(SocketConfig.socketPort);
        socketManager = SocketManagerTest.getInstance();
        socketManager.init();
        OkSocketOptions okSocketOptions = socketManager.getOption();
        switch_reconnect.setChecked(!(okSocketOptions.getReconnectionManager() instanceof NoneReconnect));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_socket);
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etContent = findViewById(R.id.etContent);
        btConnect = findViewById(R.id.lianjie);
        rv = findViewById(R.id.rv);
        switch_reconnect = findViewById(R.id.switch_reconnect);
        btSend = findViewById(R.id.btSendDatas);
        btSendPluse = findViewById(R.id.btSendPluse);
        btZhuCe = findViewById(R.id.btZhuCe);
        btAuto = findViewById(R.id.btAuto);
        btZhuXiao = findViewById(R.id.btZhuXiao);
        btLocation = findViewById(R.id.btLocation);
        btLocation2 = findViewById(R.id.btLocation2);
        init();
        initList();
    }

    private void initList() {

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        switch_reconnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    if (!(socketManager.getManager().getReconnectionManager() instanceof NoneReconnect)) {
                        socketManager.getManager().option(new OkSocketOptions.Builder(socketManager.getManager().getOption()).setReconnectionManager(new NoneReconnect()).build());
                        logPrint("关闭重连管理器");
                    }
                } else {
                    if (socketManager.getManager().getReconnectionManager() instanceof NoneReconnect) {
                        socketManager.getManager().option(new OkSocketOptions.Builder(socketManager.getManager().getOption()).setReconnectionManager(OkSocketOptions.getDefault().getReconnectionManager()).build());
                        logPrint("打开重连管理器");
                    }
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDatas(etContent.getText().toString());
            }
        });

        btSendPluse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    socketManager.openPulse();
                }
            }
        });

        btZhuCe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    byte[] register = JT808Directive.register(SocketConfig.mManufacturerId,SocketConfig.mTerminalModel,SocketConfig.getmTerminalId());
                    byte[] body = JTT808Coding.generate808(0x0100, SocketConfig.getmPhont(),register);
                    socketManager.send((body));
                }
            }
        });

        btAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    if (authCode == null) {
                        TU.s("auto.null");return;}
                    byte[] body = JTT808Coding.generate808(0x0102, SocketConfig.getmPhont(),authCode);
                    socketManager.send((body));
                }
            }
        });

        btZhuXiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    byte[] body = JTT808Coding.generate808(0x0003, SocketConfig.getmPhont(),new byte[]{});
                    socketManager.send((body));
                }
            }
        });

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    getLocationListener(5000,OkSocketActivity.this,false);
                }
            }
        });

        btLocation2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socketManager == null || !socketManager.isConnect()) {
                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
                } else {
                    locations.clear();
                    getLocationListener(1000,OkSocketActivity.this,true);
                }
            }
        });
    }


    public void sendDatas(String msg) {
        if (socketManager == null || !socketManager.isConnect()) {
            Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(msg.trim())) {
                TU.s("msg == null"); return;
            }
            byte[] body = JTT808Coding.generate808(0X0001, SocketConfig.getmPhont(), msg.getBytes(Charset.forName("GBK")), 0, 0, SocketConfig.getSocketMsgCount());
            socketManager.send(body);
            etContent.setText("");
        }
    }

    private void logPrint(final String log) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            LogBean logBean = new LogBean(System.currentTimeMillis(), log);
            logAdapter.getDataList().add(0, logBean);
            logAdapter.notifyDataSetChanged();
        } else {
            final String threadName = Thread.currentThread().getName();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    logPrint(threadName + " 线程打印(In Thread):" + log);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.disconnect();
//            mManager.unRegisterReceiver(socketActionAdapter);
        }
        if (mlocationClient != null){
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
    }


    /**
     * 获取位置信息
     *
     * @param callback
     */
    private AMapLocationClient mlocationClient;

    private ArrayList<byte[]> locations = new ArrayList();

    public void getLocationListener(final long interval, Context context , final boolean isBatch) {
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
                reportMapLocation(amapLocation,isBatch);

                if (interval <= 0) {
                    mlocationClient.stopLocation();
                    mlocationClient.onDestroy();
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationCacheEnable(true);
//        mlocationClient.startAssistantLocation(); //
        mlocationClient.setLocationOption(mLocationOption);
//        mlocationClient.enableBackgroundLocation(1,new Notification());
        mlocationClient.startLocation();
    }

    private void reportMapLocation(AMapLocation amapLocation , boolean isBatch){
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
                Date date = new Date(amapLocation.getTime());
                L.c(amapLocation.toString());

                byte[] bytes = JT808Directive.reportLocation(amapLocation.getLatitude()
                        , amapLocation.getLongitude()
                        , amapLocation.getAltitude()
                        , amapLocation.getSpeed()
                        , amapLocation.getBearing()
                        , amapLocation.getAccuracy()
                        , df.format(date));
                if (isBatch){
                    locations.add(bytes);
                    if (locations.size() >= 3){
                        mlocationClient.stopLocation();
                        mlocationClient.onDestroy();
                        byte[] batchBytes = JT808Directive.batchReportLocation(locations);
                        byte[] body = JTT808Coding.generate808(0x0704, SocketConfig.getmPhont(), batchBytes);
                        socketManager.send((body));
                    }
                }else {
                    byte[] body = JTT808Coding.generate808(0x0200, SocketConfig.getmPhont(), bytes);
                    socketManager.send((body));
                }

            } else {
                TU.s( "ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "ErrCode:" + amapLocation.getErrorCode() + ", errInfo:" + amapLocation.getErrorInfo());
            }
        }
    }
}
