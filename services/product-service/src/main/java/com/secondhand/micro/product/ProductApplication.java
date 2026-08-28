package com.secondhand.micro.product;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages={"com.secondhand.micro.product","com.secondhand.micro.platform"})
@EnableScheduling
public class ProductApplication {
 public static void main(String[] args) { SpringApplication.run(ProductApplication.class,args); }
}
