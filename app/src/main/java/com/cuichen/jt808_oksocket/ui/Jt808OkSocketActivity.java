package com.cuichen.jt808_oksocket.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cuichen.jt808_oksocket.R;
import com.cuichen.jt808_oksocket.adapter.LogAdapter;
import com.cuichen.jt808_oksocket.bean.LogBean;
import com.cuichen.jt808_oksocket.service.Jt808SocketKeepService;
import com.cuichen.jt808_oksocket.service.service_interface.OnJt808LocationBackList;
import com.cuichen.jt808_oksocket.service.service_interface.OnServiceWorking;
import com.cuichen.jt808_oksocket.utils.GsonUtils;
import com.fanjun.keeplive.KeepLive;
import com.fanjun.keeplive.config.ForegroundNotification;
import com.fanjun.keeplive.config.ForegroundNotificationClickListener;
import com.cuichen.jt808_sdk.sdk.interfaces.SocketActionListener;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;
import com.cuichen.jt808_sdk.utils.L;

public class Jt808OkSocketActivity extends AppCompatActivity {

    private RecyclerView rv;

    private LogAdapter logAdapter = new LogAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jt808_ok_socket);
        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(manager);
        rv.setAdapter(logAdapter);
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

    public void startLocation(View view) {
        startKeepService();
    }
    public void stopLocation(View view) {
        if (socketKeepLiveService == null)return;
        socketKeepLiveService.goStopLocation();
    }

    public void diaoyongService(View view) {
        if (socketKeepLiveService == null)return;
        L.cc("cccbbb", GsonUtils.toJson(socketKeepLiveService.getNoReplyDatas()));
    }

    public void Service1(View view) {

    }



    private Jt808SocketKeepService socketKeepLiveService;
    private void startKeepService() {
        //定义前台服务的默认样式。即标题、描述和图标
        ForegroundNotification foregroundNotification = new ForegroundNotification("叫个货车","正在汇报位置", R.mipmap.ic_launcher,
                //定义前台服务的通知点击事件
                new ForegroundNotificationClickListener() {
                    @Override
                    public void foregroundNotificationClick(Context context, Intent intent) {

                    }
                });

//启动保活服务
        socketKeepLiveService =  new Jt808SocketKeepService();
        KeepLive.startWork(this.getApplication(), KeepLive.RunMode.ENERGY, foregroundNotification, socketKeepLiveService);
        socketKeepLiveService.setOnServiceWorkingList(new OnServiceWorking() {
            @Override
            public void onWorking() {
                socketKeepLiveService.setJt808Config("18300000000" , "AAAAAAA" , "123456");
                socketKeepLiveService.startLocation(new OnJt808LocationBackList() {
                    @Override
                    public void onCallBack(int code, String msg) {
                        logPrint("startLocation):"+msg);
                    }
                });
                socketKeepLiveService.setSocketListener(new SocketActionListener() {
                    @Override
                    public void onSocketReadResponse(byte[] bytes) {
                        logPrint("Read):"+ HexUtil.byte2HexStrNoSpace(bytes));
                    }

                    @Override
                    public void onSocketWriteResponse(byte[] bytes) {
                        logPrint("Write):"+ HexUtil.byte2HexStrNoSpace(bytes));
                    }

                    @Override
                    public void onPulseSend(final byte[] bytes) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logPrint("onPulseSend):"+ HexUtil.byte2HexStrNoSpace(bytes));
                            }
                        });

                    }

                    @Override
                    public void onSocketDisconnection() {
                        logPrint("onSocketDisconnection):");
                    }

                    @Override
                    public void onSocketConnectionSuccess() {
                        logPrint("onSocketConnectionSuccess):");
                    }

                    @Override
                    public void onSocketConnectionFailed() {
                        logPrint("onSocketConnectionFailed):");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketKeepLiveService != null) socketKeepLiveService.goStopLocation();
    }
}
