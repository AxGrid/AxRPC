package com.axgrid.rpc;


import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
@Import(SpringTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "debug=true")
@Slf4j
public class TestAxRPC {

    @Autowired
    AxTestRPC rpc;

    @Test
    public void testIsTrue() {
        Assert.assertTrue(true);
        Assert.assertEquals(2, rpc.methodsCount());
        Request.Builder r = Request.newBuilder();
        r.setCorrelationId(151);
        r.setOpPing(OpPing.newBuilder()
                .setTime(123456)
                .build());

        Response resp = rpc.request(r.build(), null);

        Assert.assertEquals(resp.getRpPing().getTime(), r.getOpPing().getTime());
        Assert.assertTrue(resp.getSuccess());
        Assert.assertEquals(resp.getCorrelationId(), r.getCorrelationId());
    }

    @Test
    public void testContext() {
        AxContext ctx = new AxContext();
        ctx.counter = 1;
        ctx.loggedIn = true;
        Request.Builder r = Request.newBuilder();
        r.setCorrelationId(151);
        r.setOpHelloWorld(OpHelloWorld.newBuilder().setName("Zed").build());
        Response resp = rpc.request(r.build(), ctx);
        Assert.assertTrue(resp.getSuccess());
        Assert.assertEquals(resp.getCorrelationId(), r.getCorrelationId());
        Assert.assertEquals(resp.getRpHelloWorld().getResult() ,"Hello Zed");
        Assert.assertEquals(ctx.counter, 2);
    }

    @Test
    public void testUnauthorizedContext() {
        AxContext ctx = new AxContext();
        ctx.counter = 1;
        ctx.loggedIn = false;
        Request.Builder r = Request.newBuilder();
        r.setCorrelationId(151);
        r.setOpHelloWorld(OpHelloWorld.newBuilder().setName("Zed").build());
        Response resp = rpc.request(r.build(), ctx);
        Assert.assertFalse(resp.getSuccess());
        Assert.assertEquals(resp.getCorrelationId(), r.getCorrelationId());
        Assert.assertEquals(resp.getErrorCode(), 401);
    }

}
