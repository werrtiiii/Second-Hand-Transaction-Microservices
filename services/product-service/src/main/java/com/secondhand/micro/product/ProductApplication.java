package com.secondhand.micro.product;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
@SpringBootApplication(scanBasePackages={"com.secondhand.micro.product","com.secondhand.micro.platform","com.secondhand.common","com.secondhand.auth.security","com.secondhand.micro.security","com.secondhand.product","com.secondhand.comment","com.secondhand.favorite","com.secondhand.report","com.secondhand.admin"},nameGenerator=FullyQualifiedAnnotationBeanNameGenerator.class)
@EntityScan(basePackages={"com.secondhand.product","com.secondhand.comment","com.secondhand.favorite","com.secondhand.report"})
@EnableJpaRepositories(basePackages={"com.secondhand.product","com.secondhand.comment.repository","com.secondhand.favorite.repository","com.secondhand.report.repository"})
@EnableScheduling
public class ProductApplication {
 public static void main(String[] args){SpringApplication.run(ProductApplication.class,args);}
}
