package com.secondhand.micro.user;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
@SpringBootApplication(scanBasePackages={"com.secondhand.micro.user","com.secondhand.micro.platform","com.secondhand.common","com.secondhand.auth.security","com.secondhand.micro.security","com.secondhand.auth","com.secondhand.user","com.secondhand.chat","com.secondhand.admin"},nameGenerator=FullyQualifiedAnnotationBeanNameGenerator.class)
@EntityScan(basePackages={"com.secondhand.auth","com.secondhand.user","com.secondhand.chat"})
@EnableJpaRepositories(basePackages={"com.secondhand.auth.repository","com.secondhand.user.repository","com.secondhand.chat.repository"})
@EnableScheduling
public class UserApplication {
 public static void main(String[] args){SpringApplication.run(UserApplication.class,args);}
}
