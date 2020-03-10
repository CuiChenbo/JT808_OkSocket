package com.cuichen.jt808_sdk.oksocket.client.impl.client.iothreads;


import com.cuichen.jt808_sdk.oksocket.client.impl.exceptions.ManuallyDisconnectException;
import com.cuichen.jt808_sdk.oksocket.client.sdk.client.action.IAction;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IReader;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IStateSender;
import com.cuichen.jt808_sdk.oksocket.core.utils.SLog;
import com.cuichen.jt808_sdk.oksocket.interfaces.basic.AbsLoopThread;

import java.io.IOException;

/**
 * Created by xuhao on 2017/5/17.
 */

public class DuplexReadThread extends AbsLoopThread {
    private IStateSender mStateSender;

    private IReader mReader;

    public DuplexReadThread(IReader reader, IStateSender stateSender) {
        super("client_duplex_read_thread");
        this.mStateSender = stateSender;
        this.mReader = reader;
    }

    @Override
    protected void beforeLoop() {
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_START);
    }

    @Override
    protected void runInLoopThread() throws IOException {
        mReader.read();
    }

    @Override
    public synchronized void shutdown(Exception e) {
        mReader.close();
        super.shutdown(e);
    }

    @Override
    protected void loopFinish(Exception e) {
        e = e instanceof ManuallyDisconnectException ? null : e;
        if (e != null) {
            SLog.e("duplex read error,thread is dead with exception:" + e.getMessage());
        }
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_SHUTDOWN, e);
    }
}
