package com.secondhand.micro.user;
import com.secondhand.micro.platform.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class TradeQueries {
 private final Remote remote;private final String url;
 public TradeQueries(Remote r,@Value("${app.trade-url}") String u){remote=r;url=u;}
 public double getAverageScoreBySellerId(long id){return rating(id).path("averageScore").asDouble();}
 public long getCountBySellerId(long id){return rating(id).path("totalCount").asLong();}
 public com.fasterxml.jackson.databind.JsonNode rating(long id){return remote.get(url,"trade-service","/internal/v1/ratings/"+id);}
 public com.fasterxml.jackson.databind.JsonNode counts(long id){return remote.get(url,"trade-service","/internal/v1/users/"+id+"/trade-counts");}
}
