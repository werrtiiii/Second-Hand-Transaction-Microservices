package com.secondhand.micro.platform;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Path;
@Configuration public class UploadResources implements WebMvcConfigurer {
 private final String directory;public UploadResources(@Value("${app.upload-dir:./uploads}") String d){directory=d;}
 public void addResourceHandlers(ResourceHandlerRegistry registry){registry.addResourceHandler("/uploads/**").addResourceLocations(Path.of(directory).toAbsolutePath().normalize().toUri().toString()+"/");}
}
