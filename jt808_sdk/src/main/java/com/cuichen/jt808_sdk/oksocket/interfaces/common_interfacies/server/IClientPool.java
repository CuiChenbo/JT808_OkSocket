package com.cuichen.jt808_sdk.oksocket.interfaces.common_interfacies.server;


import com.cuichen.jt808_sdk.oksocket.core.iocore.interfaces.ISendable;

public interface IClientPool<T, K> {

    void cache(T t);

    T findByUniqueTag(K key);

    int size();

    void sendToAll(ISendable sendable);
}
