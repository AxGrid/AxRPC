package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCEventChannel;
import com.axgrid.rpc.dto.AxRPCEventDescription;
import com.axgrid.rpc.repository.AxRPCEventListenerRepository;
import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.async.DeferredResult;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * Сервис отправки сообщений
 * @param <V> Сообщение
 * @param <E> Хранилище сообщений
 */
@Slf4j
public abstract class AxRPCEventService<V extends GeneratedMessageV3, VC extends GeneratedMessageV3, E extends AxRPCEventRepository<V>> implements AxRPCEventDescription {

    private Class<V> persistentEventClass;
    private Class<VC> persistentEventCollectionClass;



    @Autowired
    protected AxRPCEventListenerRepository listeners;

    @Autowired
    protected E eventRepository;

    protected VC getResponseMessage(V message) { return getResponseMessage(Collections.singletonList(message)); }
    protected abstract VC getResponseMessage(List<V> message);

    /**
     * Добавить листенер
     * @param channel  канал
     * @param lastId   последнее сообщение
     * @param listener слушатель
     */
    public void listener(String channel, long lastId, DeferredResult<byte[]> listener) {
        //TODO: если есть хоть одно сообщеие в EventRepository сразу вернуть ответ
        List<V> oldEvents = eventRepository.getAll(channel, lastId);
        if (oldEvents!= null && oldEvents.size() > 0) {
            if (log.isDebugEnabled()) log.debug("Found old {} event in channel {}.{}", oldEvents.size(), channel, lastId);
            listener.setResult(getResponseMessage(oldEvents).toByteArray());
            return;
        }
        //Иначе
        if (log.isDebugEnabled()) log.debug("Add new listener for channel {}.{}", channel, lastId);
        listeners.addListener(new AxRPCEventChannel(channel), listener);
    }

    public void removeListener(String channel, DeferredResult<byte[]> listener) {
        listeners.removeListener(new AxRPCEventChannel(channel), listener);
    }

    /**
     * Отправить сообщение всем кто слышет
     * @param channel
     * @param message
     */
    public void send(String channel, V message) {
        if (log.isDebugEnabled()) log.debug("Send new event into channel channel {}: {}", channel, message);
        message = eventRepository.add(channel, message);
        //Отправим всем кто есть
        byte[] messageByte = getResponseMessage(message).toByteArray();
        listeners.sendToAll(new AxRPCEventChannel(channel), messageByte);
    }

    @Override
    public String getEventObject() { return persistentEventClass.getSimpleName(); }

    @Override
    public String getEventObjectFullName() { return persistentEventClass.getName();}

    @Override
    public String getEventCollectionObject() { return persistentEventCollectionClass.getSimpleName(); }

    @Override
    public String getEventCollectionObjectFullName() { return persistentEventCollectionClass.getName();}

    @Override
    public String getHttpEntryPoint() { return "/ev"; }

    public AxRPCEventService() {
        this.persistentEventClass = (Class<V>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.persistentEventCollectionClass = (Class<VC>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];

    }


}
