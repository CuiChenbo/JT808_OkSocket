package com.cuichen.jt808_sdk.oksocket.client.sdk.client.connection;

import com.cuichen.jt808_sdk.oksocket.client.impl.client.PulseManager;
import com.cuichen.jt808_sdk.oksocket.client.sdk.client.ConnectionInfo;
import com.cuichen.jt808_sdk.oksocket.client.sdk.client.action.ISocketActionListener;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IPulseSendable;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.ISendable;
import com.cuichen.jt808_sdk.oksocket.core.pojo.OriginalData;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 抽象联调管理器
 * Created by xuhao on 2017/6/5.
 */
public abstract class AbsReconnectionManager implements ISocketActionListener {
    /**
     * 连接管理器
     */
    protected volatile IConnectionManager mConnectionManager;
    /**
     * 心跳管理器
     */
    protected PulseManager mPulseManager;
    /**
     * 是否销毁
     */
    protected volatile boolean mDetach;
    /**
     * 需要忽略的断开连接集合,当Exception在此集合中,忽略该类型的断开异常,不会自动重连
     */
    protected volatile Set<Class<? extends Exception>> mIgnoreDisconnectExceptionList = new LinkedHashSet<>();

    public AbsReconnectionManager() {

    }

    /**
     * 关联到某一个连接管理器
     *
     * @param manager 当前连接管理器
     */
    public synchronized void attach(IConnectionManager manager) {
        if (mDetach) {
            detach();
        }
        mDetach = false;
        mConnectionManager = manager;
        mPulseManager = manager.getPulseManager();
        mConnectionManager.registerReceiver(this);
    }

    /**
     * 解除连接当前的连接管理器
     */
    public synchronized void detach() {
        mDetach = true;
        if (mConnectionManager != null) {
            mConnectionManager.unRegisterReceiver(this);
        }
    }

    /**
     * 添加需要忽略的异常,当断开异常为该异常时,将不会进行重连.
     *
     * @param e 需要忽略的异常
     */
    public final void addIgnoreException(Class<? extends Exception> e) {
        synchronized (mIgnoreDisconnectExceptionList) {
            mIgnoreDisconnectExceptionList.add(e);
        }
    }

    /**
     * 添加需要忽略的异常,当断开异常为该异常时,将不会进行重连.
     *
     * @param e 需要删除的异常
     */
    public final void removeIgnoreException(Exception e) {
        synchronized (mIgnoreDisconnectExceptionList) {
            mIgnoreDisconnectExceptionList.remove(e.getClass());
        }
    }

    /**
     * 删除需要忽略的异常
     *
     * @param e 需要忽略的异常
     */
    public final void removeIgnoreException(Class<? extends Exception> e) {
        synchronized (mIgnoreDisconnectExceptionList) {
            mIgnoreDisconnectExceptionList.remove(e);
        }
    }

    /**
     * 删除所有的忽略异常
     */
    public final void removeAll() {
        synchronized (mIgnoreDisconnectExceptionList) {
            mIgnoreDisconnectExceptionList.clear();
        }
    }

    @Override
    public void onSocketIOThreadStart(String action) {
        //do nothing;
    }

    @Override
    public void onSocketIOThreadShutdown(String action, Exception e) {
        //do nothing;
    }

    @Override
    public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
        //do nothing;
    }

    @Override
    public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
        //do nothing;
    }

    @Override
    public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
        //do nothing;
    }

}
