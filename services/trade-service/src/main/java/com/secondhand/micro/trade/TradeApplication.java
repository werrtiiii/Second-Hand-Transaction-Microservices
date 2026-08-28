package com.secondhand.micro.trade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages={"com.secondhand.micro.trade","com.secondhand.micro.platform"})
@EnableScheduling
public class TradeApplication {
 public static void main(String[] args) { SpringApplication.run(TradeApplication.class,args); }
}
