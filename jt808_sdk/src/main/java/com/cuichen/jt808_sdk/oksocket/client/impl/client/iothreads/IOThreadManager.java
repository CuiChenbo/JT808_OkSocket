package com.cuichen.jt808_sdk.oksocket.client.impl.client.iothreads;


import com.cuichen.jt808_sdk.oksocket.client.impl.exceptions.ManuallyDisconnectException;
import com.cuichen.jt808_sdk.oksocket.client.sdk.client.OkSocketOptions;
import com.cuichen.jt808_sdk.oksocket.core.iocore.ReaderImpl;
import com.cuichen.jt808_sdk.oksocket.core.iocore.WriterImpl;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IReader;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.ISendable;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IStateSender;
import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IWriter;
import com.cuichen.jt808_sdk.oksocket.core.protocol.IReaderProtocol;
import com.cuichen.jt808_sdk.oksocket.core.utils.SLog;
import com.cuichen.jt808_sdk.oksocket.interfaces.basic.AbsLoopThread;
import com.cuichen.jt808_sdk.oksocket.interfaces.common_interfacies.IIOManager;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xuhao on 2017/5/31.
 */

public class IOThreadManager implements IIOManager<OkSocketOptions> {

    private InputStream mInputStream;

    private OutputStream mOutputStream;

    private volatile OkSocketOptions mOkOptions;

    private IStateSender mSender;

    private IReader mReader;

    private IWriter mWriter;

    private AbsLoopThread mSimplexThread;

    private DuplexReadThread mDuplexReadThread;

    private DuplexWriteThread mDuplexWriteThread;

    private OkSocketOptions.IOThreadMode mCurrentThreadMode;

    public IOThreadManager(InputStream inputStream,
                           OutputStream outputStream,
                           OkSocketOptions okOptions,
                           IStateSender stateSender) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mOkOptions = okOptions;
        mSender = stateSender;
        initIO();
    }

    private void initIO() {
        assertHeaderProtocolNotEmpty();
        mReader = new ReaderImpl();
        mReader.initialize(mInputStream, mSender);
        mWriter = new WriterImpl();
        mWriter.initialize(mOutputStream, mSender);
    }

    @Override
    public synchronized void startEngine() {
        mCurrentThreadMode = mOkOptions.getIOThreadMode();
        //初始化读写工具类
        mReader.setOption(mOkOptions);
        mWriter.setOption(mOkOptions);
        switch (mOkOptions.getIOThreadMode()) {
            case DUPLEX:
                SLog.w("DUPLEX is processing");
                duplex();
                break;
            case SIMPLEX:
                SLog.w("SIMPLEX is processing");
                simplex();
                break;
            default:
                throw new RuntimeException("未定义的线程模式");
        }
    }

    private void duplex() {
        shutdownAllThread(null);
        mDuplexWriteThread = new DuplexWriteThread(mWriter, mSender);
        mDuplexReadThread = new DuplexReadThread(mReader, mSender);
        mDuplexWriteThread.start();
        mDuplexReadThread.start();
    }

    private void simplex() {
        shutdownAllThread(null);
        mSimplexThread = new SimplexIOThread(mReader, mWriter, mSender);
        mSimplexThread.start();
    }

    private void shutdownAllThread(Exception e) {
        if (mSimplexThread != null) {
            mSimplexThread.shutdown(e);
            mSimplexThread = null;
        }
        if (mDuplexReadThread != null) {
            mDuplexReadThread.shutdown(e);
            mDuplexReadThread = null;
        }
        if (mDuplexWriteThread != null) {
            mDuplexWriteThread.shutdown(e);
            mDuplexWriteThread = null;
        }
    }

    @Override
    public synchronized void setOkOptions(OkSocketOptions options) {
        mOkOptions = options;
        if (mCurrentThreadMode == null) {
            mCurrentThreadMode = mOkOptions.getIOThreadMode();
        }
        assertTheThreadModeNotChanged();
        assertHeaderProtocolNotEmpty();

        mWriter.setOption(mOkOptions);
        mReader.setOption(mOkOptions);
    }

    @Override
    public void send(ISendable sendable) {
        mWriter.offer(sendable);
    }

    @Override
    public void close() {
        close(new ManuallyDisconnectException());
    }

    @Override
    public synchronized void close(Exception e) {
        shutdownAllThread(e);
        mCurrentThreadMode = null;
    }

    private void assertHeaderProtocolNotEmpty() {
        IReaderProtocol protocol = mOkOptions.getReaderProtocol();
        if (protocol == null) {
            throw new IllegalArgumentException("The reader protocol can not be Null.");
        }

        if (protocol.getHeaderLength() == 0) {
            throw new IllegalArgumentException("The header length can not be zero.");
        }
    }

    private void assertTheThreadModeNotChanged() {
        if (mOkOptions.getIOThreadMode() != mCurrentThreadMode) {
            throw new IllegalArgumentException("can't hot change iothread mode from " + mCurrentThreadMode + " to "
                    + mOkOptions.getIOThreadMode() + " in blocking io manager");
        }
    }

}
