package com.axgrid.rpc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringTestApplication.class , webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"debug=true", "server.port=0"})
@Slf4j
public class TestAxRPCEvent {
    RestTemplate restTemplate = new RestTemplate();

    final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    MyEventService eventService;

    @Value("${local.server.port}")
    public int port;

    @Test
    public void testWebDescription() {
        Assert.assertNotEquals(0, port);
        log.info("PORT:{}", port);
        Assert.assertTrue(true);
    }

    @Test
    public void testTimeoutEvent() {
        byte[] res = restTemplate.getForObject("http://localhost:"+port+"/ax-rpc/v1/test/ev?s={session}&c={channel}&l={lastId}", byte[].class, "", "p-1", 0);
        Assert.assertNull(res);
    }

    @Test
    public void testSendOldEvent() throws Exception {
        String channel = "p-2";
        Future<byte[]> res = executorService.submit(() -> restTemplate.getForObject("http://localhost:"+port+"/ax-rpc/v1/test/ev?s={session}&c={channel}&l={lastId}", byte[].class, "", channel, 0));
        eventService.send(channel, Event.newBuilder().setMessage("hello world").build());
        byte[] data = res.get(2000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(data);
        EventCollection outEvents = EventCollection.parseFrom(data);
        Assert.assertNotNull(outEvents);
        Assert.assertNotEquals(outEvents.getEventsCount(), 0);
        Event outEvent = outEvents.getEvents(0);
        Assert.assertEquals(outEvent.getMessage(), "hello world");
        Assert.assertEquals(0, AxListenerQueue.getTotalCount());
    }


    @Test
    public void testSendLongPoolingEvent() throws Exception {
        String channel = "p-3";
        Future<byte[]> res = executorService.submit(() -> restTemplate.getForObject("http://localhost:"+port+"/ax-rpc/v1/test/ev?s={session}&c={channel}&l={lastId}", byte[].class, "", channel, 0));
        Thread.sleep(500);
        Assert.assertEquals(1, AxListenerQueue.getTotalCount());
        eventService.send(channel, Event.newBuilder().setMessage("hello world").build());
        byte[] data = res.get(2000, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(data);
        EventCollection outEvents = EventCollection.parseFrom(data);
        Assert.assertNotNull(outEvents);
        Assert.assertNotEquals(outEvents.getEventsCount(), 0);
        Event outEvent = outEvents.getEvents(0);
        Assert.assertEquals(outEvent.getMessage(), "hello world");
        Assert.assertEquals(0, AxListenerQueue.getTotalCount());
    }

}
