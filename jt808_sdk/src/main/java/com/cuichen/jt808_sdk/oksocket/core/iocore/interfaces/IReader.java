package com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces;

import java.io.InputStream;

/**
 * Created by xuhao on 2017/5/16.
 */

public interface IReader<T extends IIOCoreOptions> {

    void initialize(InputStream inputStream, IStateSender stateSender);

    void read() throws RuntimeException;

    void setOption(T option);

    void close();
}
