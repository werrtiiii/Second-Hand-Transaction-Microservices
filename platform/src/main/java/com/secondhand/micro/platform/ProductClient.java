package com.secondhand.micro.platform;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component public class ProductClient {
 private final Remote remote;private final String url;private final ObjectMapper json;
 public ProductClient(Remote r,@Value("${app.product-url}") String u,ObjectMapper j){remote=r;url=u;json=j;}
 public ProductSnapshot getById(long id){return json.convertValue(remote.get(url,"product-service","/internal/v1/products/"+id),ProductSnapshot.class);}
}
