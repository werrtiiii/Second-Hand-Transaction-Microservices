package com.secondhand.chat.service;
import com.fasterxml.jackson.databind.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
/** 用户服务只读本库通知投影，不再跨库联查订单、商品或举报。 */
@Service public class MessageCenterService {
 private final JdbcTemplate db;private final ObjectMapper json;
 public MessageCenterService(JdbcTemplate d,ObjectMapper j){db=d;json=j;}
 public List<JsonNode> getSystemMessages(Long id){return messages(id,"system");}
 public List<JsonNode> getCommentNotifications(Long id){return messages(id,"comment");}
 private List<JsonNode> messages(long id,String kind){
  return db.query("SELECT payload FROM notifications WHERE recipient_id=? AND kind=? ORDER BY created_at DESC LIMIT 50",(rs,n)->{
   try{return json.readTree(rs.getString(1));}catch(Exception e){throw new IllegalStateException(e);}
  },id,kind);
 }
}
