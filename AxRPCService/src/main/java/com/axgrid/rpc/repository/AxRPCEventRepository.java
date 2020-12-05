package com.axgrid.rpc.repository;

import com.google.protobuf.GeneratedMessageV3;

import java.util.List;

/**
 * Хранит команды которые нужно отправить в канал
 * @param <V> Единичное сообщение, то что хранить будем
 */
public interface AxRPCEventRepository<V extends GeneratedMessageV3> {
    List<V> getAll(String channel, Long lastId);
    V add(String channel, V message);
}
