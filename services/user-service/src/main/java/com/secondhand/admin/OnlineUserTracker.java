package com.secondhand.admin;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
/** 在线状态放在用户库，服务重启及多副本共享同一个事实来源。 */
@Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.service-name",havingValue="user-service")
public class OnlineUserTracker {
 private final JdbcTemplate db;public OnlineUserTracker(JdbcTemplate d){db=d;}
 public void heartbeat(Long userId){db.update("UPDATE user_security_state SET last_seen_at=NOW() WHERE user_id=?",userId);}
 public Set<Long> getActiveUserIds(int minutes){return new HashSet<>(db.queryForList("SELECT user_id FROM user_security_state WHERE last_seen_at>DATE_SUB(NOW(),INTERVAL ? MINUTE)",Long.class,minutes));}
 public long countActive(int minutes){return db.queryForObject("SELECT COUNT(*) FROM user_security_state WHERE last_seen_at>DATE_SUB(NOW(),INTERVAL ? MINUTE)",Long.class,minutes);}
 public void remove(Long id){db.update("UPDATE user_security_state SET last_seen_at=NULL WHERE user_id=?",id);}
}
