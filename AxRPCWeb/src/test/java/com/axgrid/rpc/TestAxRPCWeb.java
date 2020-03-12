package com.axgrid.rpc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
@Import(SpringTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"debug=true", "server.port=0"})
@Slf4j
public class TestAxRPCWeb {

    RestTemplate restTemplate = new RestTemplate();


    @Test
    public void testWebDescription() {
        Assert.assertTrue(true);
    }


    @Test
    public void getJson() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/ax-rpc/v1/description.json", String.class);
        Assert.assertSame(response.getStatusCode(), HttpStatus.OK);

    }
}
