package com.machpay.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiApplicationTests {

    @Test
    public void test() {
        String test = "Daily Quiz";
        assertThat(test).isNotNull();
    }

}
