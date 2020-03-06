package com.cuichen.jt808_sdk.sdk;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.cuichen.jt808_sdk.sdk.exceptions.SocketManagerException;
import com.cuichen.jt808_sdk.sdk.interfaces.OnGetResetDeviceList;
import com.cuichen.jt808_sdk.sdk.interfaces.SocketActionListener;
import com.cuichen.jt808_sdk.sdk.interfaces.SocketPulseListener;
import com.cuichen.jt808_sdk.sdk.interfaces.StartLocationBackList;
import com.cuichen.jt808_sdk.sdk.jt808bean.Generate808andSeqBean;
import com.cuichen.jt808_sdk.sdk.jt808bean.JTT808Bean;
import com.cuichen.jt808_sdk.sdk.jt808bean.Jt808MapLocation;
import com.cuichen.jt808_sdk.sdk.jt808coding.JT808Directive;
import com.cuichen.jt808_sdk.sdk.jt808coding.JTT808Coding;
import com.cuichen.jt808_sdk.sdk.jt808utils.ByteUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtil;
import com.cuichen.jt808_sdk.sdk.jt808utils.HexUtils;
import com.cuichen.jt808_sdk.utils.L;
import com.cuichen.jt808_sdk.sdk.protocol.JT808ReaderProtocol;
import com.cuichen.jt808_sdk.sdk.socketbean.PulseData;
import com.cuichen.jt808_sdk.sdk.socketbean.SendDataBean;
import com.cuichen.jt808_sdk.utils.ListUtils;
import com.cuichen.jt808_sdk.utils.PreferenceUtil;
import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.impl.client.action.ActionDispatcher;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.util.ArrayList;
import java.util.List;

public class SocketManager {

    private static SocketManager INSTANCE;
    private static final Object SingleInstanceLocker = new Object();
    private ConnectionInfo info;
    private IConnectionManager mManager;
    private List<Generate808andSeqBean> mSendDatasIng = new ArrayList<>(); //记录已发送，还未收到回复的数据；

    public static SocketManager getInstance() {
        if (INSTANCE == null) {
            synchronized (SingleInstanceLocker) {
                if (INSTANCE == null) {
                    INSTANCE = new SocketManager();
                }
            }
        }

        return INSTANCE;
    }

    /*
     * 基础Socket方法封装
     */

    private void initManager(String ip, int port) {
        info = new ConnectionInfo(ip, port);
        final Handler handler = new Handler(Looper.getMainLooper());
        OkSocketOptions.Builder builder = new OkSocketOptions.Builder();
//        builder.setReconnectionManager(new NoneReconnect());
//        builder.setReconnectionManager(new DefaultReconnectManager());
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
    public Application application;

    public void init(Application application) {
        this.application = application;
        initManager(SocketConfig.socketIp, Integer.parseInt(SocketConfig.socketPort));
    }

    private IConnectionManager getManager() {
        return mManager;
    }

    /**
     * 连接和回调
     * 如果是已连接 ， 则断开连接
     *
     * @param ip
     * @param prot
     * @param socketActionAdapter
     * @throws
     */
    SocketActionAdapter socketAdapter;

    private void connect(String ip, int port, SocketActionAdapter socketActionAdapter) throws Exception {
        if (mManager != null) {
            if (!mManager.isConnect()) {
                //调用通道进行连接
                initManager(ip, port);
                if (socketAdapter != null) mManager.unRegisterReceiver(socketAdapter);
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

    /**
     * 判断是否连接
     * @return 连接 true 未连接 false
     */
    public boolean isConnect() {
        if (mManager == null) return false;
        return mManager.isConnect();
    }

    public void send(byte[] body) {
        mManager.send(new SendDataBean(body));
    }

    /**
     * 获取配置信息
     * @return
     */
    private OkSocketOptions getOption() {
        return mManager.getOption();
    }

    /**
     * 开启心跳
     */
    private void openPulse() {
        OkSocket.open(info)
                .getPulseManager()
                .setPulseSendable(new PulseData(new SocketPulseListener() {
                    @Override
                    public void parse(byte[] bytes) {
                        L.cc("PulseSend: "+HexUtils.formatHexString(bytes));
                        if (socketActionListener != null) socketActionListener.onPulseSend(bytes);
                    }
                }))//只需要设置一次,下一次可以直接调用pulse()
                .pulse();//开始心跳,开始心跳后,心跳管理器会自动进行心跳触发
    }

    /**
     * 维持心跳
     */
    private void feedPulse() {
        if (mManager != null) mManager.getPulseManager().feed();
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mManager.disconnect();
    }
    /**
     * 关闭回调 （请在界面或服务关闭时调用此方法）
     */
    public void unRegisterReceiver() {
        mManager.unRegisterReceiver(socketAdapter);
    }

    /*
    APP需要实现的方法封装
     */

    private SocketManager socketManager;
    private StartLocationBackList startLocationBackList;

    public void setJt808Phont(String mPhont){
      SocketConfig.setmPhont(mPhont);
    }

    public void setJt808TerminalId(String mTerminalId){
      SocketConfig.setmTerminalId(mTerminalId);
    }

    public void setJt808OrderId(String mOrderId){
       SocketConfig.setmOrderId(mOrderId);
    }

    /**
     * 开启位置上报；
     * 鉴权逻辑 https://dev-cv.saicmotor.com/confluence/pages/viewpage.action?pageId=64193234
     *
     * @param startLocationBackList 0为开始成功，APP端开始收集定位信息开始上报；
     */
    public void startLocation(StartLocationBackList startLocationBackList) {
        this.startLocationBackList = startLocationBackList;
        socketManager = INSTANCE;
        if (TextUtils.isEmpty(SocketConfig.getmPhont()) || TextUtils.isEmpty(SocketConfig.getmTerminalId())){
            startLocationBackList.onBack(1019,"手机号或终端ID为空");
        }
        try {
            if (socketManager.isConnect()) {
//                goAuthCode();
                startLocationBackList.onBack(0, "当前已连接了，去上报位置吧^_^");
            } else {
                socketManager.connect(SocketConfig.socketIp, Integer.valueOf(SocketConfig.socketPort), new SocketActionAdapter() {
                    @Override
                    public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
                        L.cc("Connection：连接成功");
                        if (socketActionListener != null) socketActionListener.onSocketConnectionSuccess();
                        socketManager.openPulse();
                        goAuthCode();
                    }

                    @Override
                    public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
                        L.cc("Connection：连接失败");
                        if (socketActionListener != null) socketActionListener.onSocketConnectionFailed();
                        startLocationBackList.onBack(1003, "连接失败，连接超时");
                    }

                    @Override
                    public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
                        L.cc("Connection：连接已断开");
                        if (socketActionListener != null) socketActionListener.onSocketDisconnection();
                        startLocationBackList.onBack(1004, "连接被远程主机断开");
                    }

                    @Override
                    public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
                        onReadResponse(data);
                    }

                    @Override
                    public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
                        String s = HexUtils.formatHexString(data.parse());
                        if (socketActionListener != null) socketActionListener.onSocketWriteResponse(data.parse());
                        L.cc("Write:" + s);
                    }

                    @Override
                    public void onPulseSend(ConnectionInfo info, IPulseSendable data) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            startLocationBackList.onBack(1021, e.getMessage());
        }
    }

    private void onReadResponse(OriginalData data) {
        byte[] body = ByteUtil.byteMergerAll(data.getHeadBytes(), data.getBodyBytes()); //原数据
        if (socketActionListener != null) socketActionListener.onSocketReadResponse(body);
        L.cc("Read:" + HexUtils.formatHexString(body, true));
        byte[] bytes = JTT808Coding.check808Data(body); //去除包头尾的7E标识和校验码
        if (bytes == null) {
            startLocationBackList.onBack(1021, "数据包编译失败");
            return;
        }
        L.jt808(bytes);
        JTT808Bean jt808data = JTT808Coding.resolve808(bytes);
        switch (jt808data.getMsgId()) {
            case 0x8100:
                //是注册回复得消息
                if (jt808data.getReplyResult() == 0x00) {
                    //拿到鉴权码 去鉴权
                    byte[] authCode = jt808data.getAuthenticationCode();
                    PreferenceUtil.set(SocketConfig.JT808AuthCode, HexUtil.byte2HexStrNoSpace(authCode));
                    goAuthCode();
                } else if (jt808data.getReplyResult() == 0x03) { //已注册
                    if (onGetResetDeviceList != null)
                        onGetResetDeviceList.getResetDevice(isReset -> {
                            if (isReset) {
                                getAuthCode();
                            } else {
                                startLocationBackList.onBack(1008, "重置终端失败");
                                stopLocation();
                            }
                        });
                } else {
                    startLocationBackList.onBack(1015, "注册失败：" + jt808data.getReplyResult());
                }
                break;
            case 0x8001:
                if (jt808data.getReturnMsgId() == 0x0002) { //心跳
                    if (socketManager != null) socketManager.feedPulse();
                } else if (0x0102 == jt808data.getReturnMsgId()) { //鉴权
                    if (jt808data.getReplyResult() == 0x00) {
                        startLocationBackList.onBack(0, "位置上报连接成功");
                    } else if (jt808data.getReplyResult() == 0x05) {
                        //鉴权码失效
                        PreferenceUtil.clear(SocketConfig.JT808AuthCode); //清空本地鉴权码
                        getAuthCode();
                    } else {
                        //鉴权失败
                        stopLocation();
                        PreferenceUtil.clear(SocketConfig.JT808AuthCode); //清空本地鉴权码
                        startLocationBackList.onBack(1012, "鉴权失败 code:" + jt808data.getReplyResult());
                    }
                } else if (0x0200 == jt808data.getReturnMsgId()) { //单次位置上报

                }else if (0x0704 == jt808data.getReturnMsgId()){ //批量位置上报

                }
                break;
            case 0x8101:
                //重新获取鉴权码
                if (jt808data.getReplyResult() == 0x00) {
                    //拿到鉴权码 去鉴权
                    byte[] authCode = jt808data.getAuthenticationCode();
                    PreferenceUtil.set(SocketConfig.JT808AuthCode, HexUtil.byte2HexStrNoSpace(authCode));
                    goAuthCode();
                } else {
                    startLocationBackList.onBack(1008, "重新获取鉴权码失败：code" + jt808data.getReplyResult());
                }

                break;
        }
        for (int i = 0; i < mSendDatasIng.size(); i++) {
            if (mSendDatasIng.get(i).getSeqNo() == jt808data.getMsgFlowNumber()){
                mSendDatasIng.remove(i);
                break;
            }
        }
    }


    /*
     可以反编译消息 拿到流水号
                byte[] bytes = JTT808Coding.check808Data(body);
                JTT808Bean jt808data = JTT808Coding.resolve808(bytes);
                int MsgFlowNumber = jt808data.getMsgFlowNumber();
     */

    /**
     * 去注册
     */
    private void goRegister() {
        if (socketManager == null || !socketManager.isConnect()) {
            startLocationBackList.onBack(1004, "未连接到服务");
        } else {
            byte[] register = JT808Directive.register(SocketConfig.mManufacturerId, SocketConfig.mTerminalModel, SocketConfig.getmTerminalId());
            byte[] body = JTT808Coding.generate808(0x0100, SocketConfig.getmPhont(), register);
            socketManager.send((body));
        }
    }

    /**
     * 去鉴权
     */
    private void goAuthCode() {
        if (socketManager == null || !socketManager.isConnect()) {
            startLocationBackList.onBack(1004, "未连接到服务");
        } else {
            String stringAuthCode = PreferenceUtil.get(SocketConfig.JT808AuthCode);
            if (TextUtils.isEmpty(stringAuthCode)) {
                goRegister();
            } else {
                byte[] authCode = HexUtil.hexStringToByte(stringAuthCode);
                byte[] body = JTT808Coding.generate808(0x0102, SocketConfig.getmPhont(), authCode);
                socketManager.send((body));
            }
        }
    }

    /**
     * 注销账户(仅SDK使用)
     */
    public void goLogout() {
        if (socketManager == null || !socketManager.isConnect()) {
            startLocationBackList.onBack(1004, "未连接到服务");
        } else {
            byte[] body = JTT808Coding.generate808(0x0003, SocketConfig.getmPhont(), new byte[]{});
            socketManager.send((body));
        }
    }


    /**
     * 重新获取鉴权码
     */
    private void getAuthCode() {
        if (socketManager == null || !socketManager.isConnect()) {
            startLocationBackList.onBack(1004, "未连接到服务");
        } else {
            byte[] body = JTT808Coding.generate808(0x0103, SocketConfig.getmPhont(), new byte[]{});
            socketManager.send((body));
        }
    }

    public void stopLocation() {
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    private OnGetResetDeviceList onGetResetDeviceList;
    /**
     * 实现重置终端的Http连接
     *
     * @param onGetResetDeviceList 把实现结果回传
     */
    public void setOnGetResetDeviceList(OnGetResetDeviceList onGetResetDeviceList) {
        if (onGetResetDeviceList != null) {
            this.onGetResetDeviceList = onGetResetDeviceList;
        }
    }

    /**
     * Socket连接变化监听，以及消息收发监听
     */
    private SocketActionListener socketActionListener;
    public void setSocketActionListener(SocketActionListener socketActionListener){
        this.socketActionListener = socketActionListener;
    }


    /**
     * 发送单条位置信息
     * @param jt808MapLocation
     */
    public void sendReportLocation(Jt808MapLocation jt808MapLocation) {
        byte[] bytes = JT808Directive.reportLocation(jt808MapLocation);
        Generate808andSeqBean generate808orSeqBean = JTT808Coding.generate808orSeqNo(0x0200, SocketConfig.getmPhont(), bytes);
        byte[] body = generate808orSeqBean.getBytes();
        mManager.send(new SendDataBean(body));
        mSendDatasIng.add(generate808orSeqBean);
    }

    /**
     * 发送批量位置信息
     * @param jt808MapLocations
     */
    public void sendBatchReportLocation(List<Jt808MapLocation> jt808MapLocations) {
        if (ListUtils.isEmpty(jt808MapLocations))return;

        // 防止溢出，批量上传一次最多10条；
       List<List<Jt808MapLocation>> jt808MapLocations10 = ListUtils.split(jt808MapLocations,10);
        for (int i = 0; i < jt808MapLocations10.size(); i++) {
            goBatchReportLocation(jt808MapLocations10.get(i));
        }
    }

    private void goBatchReportLocation(List<Jt808MapLocation> jt808MapLocations){
        ArrayList<byte[]> locations = new ArrayList();
        //先把数据转为Byte数据
        for (int i = 0; i < jt808MapLocations.size(); i++) {
            byte[] bytes = JT808Directive.reportLocation(jt808MapLocations.get(i));
            locations.add(bytes);
        }
        // 编译数据成为批量上传格式
        byte[] batchBytes = JT808Directive.batchReportLocation(locations);
        Generate808andSeqBean generate808orSeqBean = JTT808Coding.generate808orSeqNo(0x0704, SocketConfig.getmPhont(), batchBytes);
        byte[] body = generate808orSeqBean.getBytes();
        mManager.send(new SendDataBean(body));
        mSendDatasIng.add(generate808orSeqBean);
    }

    /**
     * 获取未回复的报文
     * @return
     */
    public List<Generate808andSeqBean> getNoReplyDatas(){
        return mSendDatasIng;
    }
}
