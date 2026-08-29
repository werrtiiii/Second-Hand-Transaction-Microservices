package com.secondhand.micro.trade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
@SpringBootApplication(scanBasePackages={"com.secondhand.micro.trade","com.secondhand.micro.platform","com.secondhand.common","com.secondhand.auth.security","com.secondhand.micro.security","com.secondhand.order","com.secondhand.offer","com.secondhand.aftersale","com.secondhand.rating","com.secondhand.payment","com.secondhand.logistics","com.secondhand.admin"},nameGenerator=FullyQualifiedAnnotationBeanNameGenerator.class)
@EntityScan(basePackages={"com.secondhand.order","com.secondhand.offer","com.secondhand.aftersale","com.secondhand.rating","com.secondhand.payment","com.secondhand.logistics"})
@EnableJpaRepositories(basePackages={"com.secondhand.order.repository","com.secondhand.offer.repository","com.secondhand.aftersale.repository","com.secondhand.rating.repository"})
@EnableScheduling
public class TradeApplication {
 public static void main(String[] args){SpringApplication.run(TradeApplication.class,args);}
}
