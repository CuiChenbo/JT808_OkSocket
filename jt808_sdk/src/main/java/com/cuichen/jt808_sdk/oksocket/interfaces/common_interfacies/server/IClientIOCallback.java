package com.cuichen.jt808_sdk.oksocket.interfaces.common_interfacies.server;


import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.ISendable;
import com.cuichen.jt808_sdk.oksocket.core.pojo.OriginalData;

public interface IClientIOCallback {

    void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool);

    void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool);

}
