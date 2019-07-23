package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/14 17:00
 * @Description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class LySmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySmsApplication.class);
    }
}
