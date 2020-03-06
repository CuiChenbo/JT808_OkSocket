package com.cuichen.jt808_sdk.sdk;

import android.os.Handler;
import android.os.Looper;

import com.cuichen.jt808_sdk.sdk.exceptions.SocketManagerException;
import com.cuichen.jt808_sdk.sdk.protocol.JT808ReaderProtocol;
import com.cuichen.jt808_sdk.sdk.socketbean.PulseData;
import com.cuichen.jt808_sdk.sdk.socketbean.SendDataBean;
import com.xuhao.didi.socket.client.impl.client.action.ActionDispatcher;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

public class SocketManagerTest {

    private static SocketManagerTest INSTANCE;
    private static final Object SingleInstanceLocker = new Object();
    private ConnectionInfo info;
    private IConnectionManager mManager;

    public static SocketManagerTest getInstance() {
        if (INSTANCE == null) {
            synchronized (SingleInstanceLocker) {
                if (INSTANCE == null) {
                    INSTANCE = new SocketManagerTest();
                }
            }
        }

        return INSTANCE;
    }

    private void initManager(String ip, int prot) {
        info = new ConnectionInfo(ip, prot);
        final Handler handler = new Handler(Looper.getMainLooper());
        OkSocketOptions.Builder builder = new OkSocketOptions.Builder();
//        builder.setReconnectionManager(new NoneReconnect());
        builder.setPulseFrequency(5000L); //心跳间隔
        builder.setCallbackThreadModeToken(new OkSocketOptions.ThreadModeToken() {
            @Override
            public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
                handler.post(runnable);
            }
        });
        builder.setReaderProtocol(new JT808ReaderProtocol());
        mManager = OkSocket.open(info).option(builder.build());
    }

    /**
     * 初始化
     */
    public void init() {
        initManager("", 0);
    }

    public IConnectionManager getManager(){
        return mManager;
    }

    /**
     * 连接和回调
     * 如果是已连接 ， 则断开连接
     * @param ip
     * @param prot
     * @param socketActionAdapter
     * @throws
     */
    SocketActionAdapter socketAdapter;

    public void connect(String ip, int prot, SocketActionAdapter socketActionAdapter) throws Exception {
        if (mManager != null) {
            if (!mManager.isConnect()) {
                //调用通道进行连接
                initManager(ip, prot);
                if (socketAdapter != null)mManager.unRegisterReceiver(socketAdapter);
                socketAdapter = socketActionAdapter;
                mManager.registerReceiver(socketActionAdapter);
                mManager.connect();
            } else {
                mManager.disconnect();
            }
        } else {
            throw new SocketManagerException("请先初始化");
        }

    }

    public boolean isConnect() {
        if (mManager == null) return false;
        return mManager.isConnect();
    }

    public void send(byte[] body) {
        mManager.send(new SendDataBean(body));
    }

    public OkSocketOptions getOption() {
        return mManager.getOption();
    }

    public void openPulse() {
        OkSocket.open(info)
                .getPulseManager()
                .setPulseSendable(new PulseData())//只需要设置一次,下一次可以直接调用pulse()
                .pulse();//开始心跳,开始心跳后,心跳管理器会自动进行心跳触发
    }

    public void feedPulse() {
        if (mManager != null) mManager.getPulseManager().feed();
    }

    public void disconnect() {
        mManager.disconnect();
        mManager.unRegisterReceiver(socketAdapter);
    }
}
