package com.cuichen.jt808_sdk.oksocket.interfaces.common_interfacies.server;


import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.IIOCoreOptions;


public interface IServerManagerPrivate<E extends IIOCoreOptions> extends IServerManager<E> {
    void initServerPrivate(int serverPort);
}
