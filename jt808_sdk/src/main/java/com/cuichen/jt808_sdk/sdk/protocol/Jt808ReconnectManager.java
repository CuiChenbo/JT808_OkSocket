package com.cuichen.jt808_sdk.sdk.protocol;

import com.xuhao.didi.core.utils.SLog;
import com.xuhao.didi.socket.client.impl.exceptions.ManuallyDisconnectException;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.connection.AbsReconnectionManager;
import com.xuhao.didi.socket.common.interfaces.basic.AbsLoopThread;
import com.xuhao.didi.socket.common.interfaces.utils.ThreadUtils;

import java.util.Iterator;

public class Jt808ReconnectManager extends AbsReconnectionManager {

    private static final int MAX_CONNECTION_FAILED_TIMES = 12;
    private int mConnectionFailedTimes = 0;
    private volatile ReconnectTestingThread mReconnectTestingThread = new ReconnectTestingThread();

    public Jt808ReconnectManager() {
    }

    public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
        if (this.isNeedReconnect(e)) {
            this.reconnectDelay();
        } else {
            this.resetThread();
        }

    }

    public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
        this.resetThread();
    }

    public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
        if (e != null) {
            ++this.mConnectionFailedTimes;
            if (this.mConnectionFailedTimes > 12) {
                this.resetThread();
                ConnectionInfo originInfo = this.mConnectionManager.getRemoteConnectionInfo();
                ConnectionInfo backupInfo = originInfo.getBackupInfo();
                if (backupInfo != null) {
                    ConnectionInfo bbInfo = new ConnectionInfo(originInfo.getIp(), originInfo.getPort());
                    backupInfo.setBackupInfo(bbInfo);
                    if (!this.mConnectionManager.isConnect()) {
                        SLog.i("Prepare switch to the backup line " + backupInfo.getIp() + ":" + backupInfo.getPort() + " ...");
                        synchronized(this.mConnectionManager) {
                            this.mConnectionManager.switchConnectionInfo(backupInfo);
                        }

                        this.reconnectDelay();
                    }
                } else {
                    this.reconnectDelay();
                }
            } else {
                this.reconnectDelay();
            }
        }

    }

    private boolean isNeedReconnect(Exception e) {
        synchronized(this.mIgnoreDisconnectExceptionList) {
            if (e != null && !(e instanceof ManuallyDisconnectException)) {
                Iterator it = this.mIgnoreDisconnectExceptionList.iterator();

                Class classException;
                do {
                    if (!it.hasNext()) {
                        return true;
                    }

                    classException = (Class)it.next();
                } while(!classException.isAssignableFrom(e.getClass()));

                return false;
            } else {
                return false;
            }
        }
    }

    private synchronized void resetThread() {
        if (this.mReconnectTestingThread != null) {
            this.mReconnectTestingThread.shutdown();
        }

    }

    private void reconnectDelay() {
        synchronized(this.mReconnectTestingThread) {
            if (this.mReconnectTestingThread.isShutdown()) {
                this.mReconnectTestingThread.start();
            }

        }
    }

    public void detach() {
        super.detach();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && this.getClass() == o.getClass();
        }
    }

    private class ReconnectTestingThread extends AbsLoopThread {
        private long mReconnectTimeDelay;

        private ReconnectTestingThread() {
            this.mReconnectTimeDelay = 10000L;
        }

        protected void beforeLoop() throws Exception {
            super.beforeLoop();
            if (this.mReconnectTimeDelay < (long)(Jt808ReconnectManager.this.mConnectionManager.getOption().getConnectTimeoutSecond() * 1000)) {
                this.mReconnectTimeDelay = (long)(Jt808ReconnectManager.this.mConnectionManager.getOption().getConnectTimeoutSecond() * 1000);
            }

        }

        protected void runInLoopThread() throws Exception {
            if (Jt808ReconnectManager.this.mDetach) {
                SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!");
                this.shutdown();
            } else {
                SLog.i("Reconnect after " + this.mReconnectTimeDelay + " mills ...");
                ThreadUtils.sleep(this.mReconnectTimeDelay);
                if (Jt808ReconnectManager.this.mDetach) {
                    SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!");
                    this.shutdown();
                } else if (Jt808ReconnectManager.this.mConnectionManager.isConnect()) {
                    this.shutdown();
                } else {
                    boolean isHolden = Jt808ReconnectManager.this.mConnectionManager.getOption().isConnectionHolden();
                    if (!isHolden) {
                        Jt808ReconnectManager.this.detach();
                        this.shutdown();
                    } else {
                        ConnectionInfo info = Jt808ReconnectManager.this.mConnectionManager.getRemoteConnectionInfo();
                        SLog.i("Reconnect the server " + info.getIp() + ":" + info.getPort() + " ...");
                        synchronized(Jt808ReconnectManager.this.mConnectionManager) {
                            if (!Jt808ReconnectManager.this.mConnectionManager.isConnect()) {
                                Jt808ReconnectManager.this.mConnectionManager.connect();
                            } else {
                                this.shutdown();
                            }

                        }
                    }
                }
            }
        }

        protected void loopFinish(Exception e) {
        }
    }
}
