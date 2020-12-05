package com.axgrid.rpc;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AxListenerQueue extends LinkedBlockingQueue<DeferredResult<byte[]>> {
    static final AtomicLong totalCount = new AtomicLong(0);

    public static long getTotalCount() { return totalCount.get(); }

    @Override
    public boolean add(DeferredResult<byte[]> deferredResult) {
        totalCount.incrementAndGet();
        return super.add(deferredResult);
    }

    @Override
    public boolean remove(Object o) {
        boolean res = super.remove(o);
        if (res) totalCount.decrementAndGet();
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends DeferredResult<byte[]>> c) {
        totalCount.addAndGet(c.size());
        return super.addAll(c);
    }

    @Override
    public void clear() {
        totalCount.addAndGet(-this.size());
        super.clear();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().allMatch(this::remove);
    }

    @Override
    public DeferredResult<byte[]> poll() {
        totalCount.decrementAndGet();
        DeferredResult<byte[]> res = super.poll();
        if (res != null) totalCount.decrementAndGet();
        return res;
    }

    @Override
    public DeferredResult<byte[]> poll(long timeout, TimeUnit unit) throws InterruptedException {
        DeferredResult<byte[]> res = super.poll(timeout, unit);
        if (res != null) totalCount.decrementAndGet();
        return res;
    }
}
