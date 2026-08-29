package com.secondhand.micro.platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.*;
/** 业务事务内落库；发送采用租约、指数退避及接收端去重。 */
@Component @ConditionalOnProperty(name="app.outbox-enabled",havingValue="true")
public class Outbox {
 private final JdbcTemplate db;private final ObjectMapper json;private final Remote remote;private final String userUrl;private final String worker=UUID.randomUUID().toString();
 public Outbox(JdbcTemplate d,ObjectMapper j,Remote r,@Value("${app.user-url}") String u){db=d;json=j;remote=r;userUrl=u;}
 public void enqueue(long recipient,String kind,Object payload){
  try{db.update("INSERT INTO outbox_events(id,recipient_id,kind,payload,next_attempt_at,created_at) VALUES(?,?,?,?,NOW(),NOW())",UUID.randomUUID().toString(),recipient,kind,json.writeValueAsString(payload));}
  catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException(e);}
 }
 @Scheduled(fixedDelayString="${app.outbox-delay-ms:1000}")
 public void deliver(){
  for(var row:db.queryForList("SELECT id FROM outbox_events WHERE published_at IS NULL AND next_attempt_at<=NOW() AND (lease_until IS NULL OR lease_until<NOW()) ORDER BY next_attempt_at LIMIT 50")){
   String id=(String)row.get("id");
   if(db.update("UPDATE outbox_events SET lease_owner=?,lease_until=DATE_ADD(NOW(),INTERVAL 30 SECOND) WHERE id=? AND published_at IS NULL AND (lease_until IS NULL OR lease_until<NOW())",worker,id)!=1)continue;
   var event=db.queryForMap("SELECT * FROM outbox_events WHERE id=?",id);
   try{
    remote.post(userUrl,"user-service","/internal/v1/notifications",Map.of("eventId",id,"recipientId",event.get("recipient_id"),"kind",event.get("kind"),"payload",json.readTree((String)event.get("payload"))),id);
    db.update("UPDATE outbox_events SET published_at=NOW(),lease_until=NULL,last_error=NULL WHERE id=? AND lease_owner=?",id,worker);
   }catch(Exception failure){
    int attempts=((Number)event.get("attempts")).intValue()+1;int delay=Math.min(300,1<<Math.min(8,attempts));
    db.update("UPDATE outbox_events SET attempts=?,next_attempt_at=DATE_ADD(NOW(),INTERVAL ? SECOND),lease_until=NULL,last_error=? WHERE id=? AND lease_owner=?",attempts,delay,failure.getClass().getSimpleName(),id,worker);
    if(attempts>=5)org.slf4j.LoggerFactory.getLogger(getClass()).warn("事件投递等待恢复 eventId={} attempts={}",id,attempts);
   }
  }
 }
}
