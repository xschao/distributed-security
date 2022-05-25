package cn.jjxy.security.distributed.uaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author qinggu
 * @date 2021年09月02日20:05
 */
//37集
@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
@EnableFeignClients(basePackages = {"cn.jjxy.security.distributed.uaa"})
public class UAAServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UAAServiceApplication.class,args);
    }
}
