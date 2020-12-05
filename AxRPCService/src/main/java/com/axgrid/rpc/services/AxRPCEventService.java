package com.axgrid.rpc.services;

import com.axgrid.rpc.dto.AxRPCEventChannel;
import com.axgrid.rpc.dto.AxRPCEventDescription;
import com.axgrid.rpc.repository.AxRPCEventListenerRepository;
import com.axgrid.rpc.repository.AxRPCEventRepository;
import com.google.protobuf.GeneratedMessageV3;
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
public abstract class AxRPCEventService<V extends GeneratedMessageV3, E extends AxRPCEventRepository<V>> implements AxRPCEventDescription {

    private Class<V> persistentEventClass;

    @Autowired
    protected AxRPCEventListenerRepository listeners;

    @Autowired
    protected E eventRepository;

    protected byte[] getResponseMessage(V message) { return getResponseMessage(Collections.singletonList(message)); }
    protected abstract byte[] getResponseMessage(List<V> message);

    /**
     * Добавить листенер
     * @param channel  канал
     * @param lastId   последнее сообщение
     * @param listener слушатель
     */
    public void listener(String channel, long lastId, DeferredResult<byte[]> listener) {
        //TODO: если есть хоть одно сообщеие в EventRepository сразу вернуть ответ
        List<V> oldEvents = eventRepository.getAll(channel, lastId);
        if (oldEvents!= null) {
            listener.setResult(getResponseMessage(oldEvents));
            return;
        }
        //Иначе
        listeners.addListener(new AxRPCEventChannel(channel), listener);
    }

    /**
     * Отправить сообщение всем кто слышет
     * @param channel
     * @param message
     */
    public void send(String channel, V message) {
        message = eventRepository.add(channel, message);
        //Отправим всем кто есть
        byte[] messageByte = getResponseMessage(message);
        listeners.sendToAll(new AxRPCEventChannel(channel), messageByte);
    }

    @Override
    public String getEventObject() { return persistentEventClass.getSimpleName(); }

    @Override
    public String getEventObjectFullName() { return persistentEventClass.getName();}

    @Override
    public String getHttpEntryPoint() { return "/ev"; }

    public AxRPCEventService() {
        this.persistentEventClass = (Class<V>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

    }


}
