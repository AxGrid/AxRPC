package com.axgrid.rpc.repository;

import com.axgrid.rpc.AxListenerQueue;
import com.axgrid.rpc.dto.AxRPCEventChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Хранит ссылки от LP объектов
 */
@Slf4j
@Repository
public class AxRPCEventListenerRepository implements HealthIndicator {
    final Map<AxRPCEventChannel, AxListenerQueue> listeners = new ConcurrentHashMap<>();
    final Queue<AxRPCEventChannel> channelsTimeout = new LinkedBlockingQueue<>();

    long listenerTimeout = 60_000 * 10;

    @Value("${axgrid.rpc.channelAlert:3000}")
    long axChannelAlert = 3000;
    @Value("${axgrid.rpc.listenerAlert:15000}")
    long axListenersAlert = 3000 * 5;

    /**
     * Добавить листенер
     * @param channel
     * @param listener
     */
    public void addListener(AxRPCEventChannel channel, DeferredResult<byte[]> listener) {
        listeners.compute(channel, (k,v) -> {
            k.setUpdateTime(new Date().getTime());
            if (v == null)
                v = new AxListenerQueue();
            else
                channelsTimeout.removeIf(chn -> chn.equals(k));
            channelsTimeout.add(k);
            v.add(listener);
            return v;
        });
        if (log.isDebugEnabled()) log.debug("Add new listener {} for channel {}", listener, channel);
    }

    /**
     * Удалить листенер
     * @param channel
     * @param listener
     */
    public void removeListener(AxRPCEventChannel channel, DeferredResult<byte[]> listener) {
        if (!listeners.containsKey(channel)) return;
        if (log.isDebugEnabled()) log.debug("Remove listener {} from channel {}", listener, channel);
        listeners.get(channel).remove(listener);
    }

    /**
     * Отправить всем кто присоеденен
     * @param channel
     * @param data
     */
    public void sendToAll(AxRPCEventChannel channel, byte[] data) {
        if (!listeners.containsKey(channel)) return;
        Queue<DeferredResult<byte[]>> channelListeners = listeners.get(channel);
        while(channelListeners.size() > 0){
            DeferredResult<byte[]> listener = channelListeners.poll();
            if (listener == null || listener.isSetOrExpired()) continue;
            if (log.isDebugEnabled()) log.debug("  Send to {} {}bytes", listener, data.length);
            listener.setResult(data);
        }
    }

    @Scheduled(fixedDelay = 10_000)
    protected void cleanUp() {
        if (listeners.size() == 0) return;
        long timeoutTime = new Date().getTime() - listenerTimeout;
        while(channelsTimeout.peek() != null && channelsTimeout.peek().getUpdateTime() < timeoutTime) {
            AxRPCEventChannel channel = channelsTimeout.poll();
            listeners.get(channel).forEach(item -> item.setResult(new byte[0]));
            listeners.remove(channel);
        }
    }

    @Override
    public Health health() {
        Health.Builder status = Health.up();
        if (listeners.size() > axChannelAlert || AxListenerQueue.getTotalCount() > axListenersAlert) {
            status = Health.down();
        }
        status.withDetail("Channels", listeners.size());
        status.withDetail("Listeners", AxListenerQueue.getTotalCount());
        return status.build();
    }
}
