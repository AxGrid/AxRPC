package com.axgrid.rpc;

import com.axgrid.rpc.repository.AxRPCCache;
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

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
@Import(SpringTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "debug=true")
@Slf4j
public class TestAxRPCCache {

    @Autowired
    AxRPCCache<Response> cache;

    @Test
    public void testCache() {

        Response r = Response.newBuilder()
                .setCorrelationId(1)
                .build();

        String trx1 = UUID.randomUUID().toString();
        String trx2 = UUID.randomUUID().toString();

        Response r2 = cache.put(trx1, r);
        Assert.assertEquals(r.getCorrelationId(), r2.getCorrelationId());
        Response r3 = cache.get(trx1);
        Assert.assertNotNull(r3);
        Assert.assertEquals(r.getCorrelationId(), r3.getCorrelationId());
        Response r4 = cache.get(trx2);
        Assert.assertNull(r4);
        r = Response.newBuilder()
                .setCorrelationId(2)
                .build();
        r2 = cache.put(trx1, r);

        Assert.assertEquals(r.getCorrelationId(), r2.getCorrelationId());
        r3 = cache.get(trx1);
        Assert.assertNotNull(r3);
        Assert.assertEquals(r.getCorrelationId(), r3.getCorrelationId());
    }

    @Test
    public void testPutOrGetCache() {
        String trx1 = UUID.randomUUID().toString();
        Response r = Response.newBuilder()
                .setCorrelationId(1)
                .build();
        Response r2 = cache.putOrGet(trx1, r);
        Assert.assertEquals(r.getCorrelationId(), r2.getCorrelationId());
        r = Response.newBuilder()
                .setCorrelationId(5)
                .build();

        r2 = cache.putOrGet(trx1, r);
        Assert.assertNotEquals(r.getCorrelationId(), r2.getCorrelationId());
        Assert.assertEquals(1, r2.getCorrelationId());
    }

    @Test
    public void testNullTRXCache() {
        Response r = Response.newBuilder()
                .setCorrelationId(1)
                .build();
        Response r2 = cache.putOrGet(null, r);
        Assert.assertEquals(r.getCorrelationId(), r2.getCorrelationId());

        r = Response.newBuilder()
                .setCorrelationId(5)
                .build();

        r2 = cache.putOrGet(null, r);
        Assert.assertEquals(r.getCorrelationId(), r2.getCorrelationId());
    }
}
