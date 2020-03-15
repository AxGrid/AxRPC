package com.axgrid.rpc;

import com.axgrid.rpc.dto.AxRPCDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringTestApplication.class , webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SpringTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"debug=true", "server.port=0"})
@Slf4j
public class TestAxRPCWeb {

    RestTemplate restTemplate = new RestTemplate();

    @Value("${local.server.port}")
    public int port;

    @Test
    public void testWebDescription() {
        Assert.assertNotEquals(0, port);
        log.info("PORT:{}", port);
        Assert.assertTrue(true);
    }


    @Test
    public void getJson() {
        List<AxRPCDescription> r = new ArrayList<>();
        ResponseEntity<AxRPCDescriptionImpl[]> response = restTemplate.getForEntity(String.format("http://localhost:%d/ax-rpc/v1/description.json", port), AxRPCDescriptionImpl[].class);
        Assert.assertSame(response.getStatusCode(), HttpStatus.OK);
        AxRPCDescriptionImpl[] descs = response.getBody();
        Assert.assertNotNull(descs);
        Assert.assertNotEquals(descs.length, 0);
        Assert.assertEquals(descs[0].getHttpEntryPoint(), "/ax-rpc/v1/test");

    }
}
