package com.secondhand.micro.testing;
import com.secondhand.micro.platform.Tokens;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import java.security.*;
import java.sql.*;
import java.util.*;
public class TestEnvironment implements AutoCloseable {
 private final MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.0").withDatabaseName("bootstrap").withUsername("bootstrap").withPassword("test-only-password");
 private final Map<String,KeyPair> keys=new HashMap<>();private final Set<String> databases=new HashSet<>();
 private final List<ServletWebServerApplicationContext> contexts=new ArrayList<>();
 public TestEnvironment(){try{for(String service:List.of("user","product","trade")){var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);keys.put(service,generator.generateKeyPair());}mysql.start();}catch(Exception e){throw new IllegalStateException(e);}}
 private String base(){return "jdbc:mysql://"+mysql.getHost()+":"+mysql.getMappedPort(3306)+"/";}
 public String url(String service){return base()+"secondhand_"+service+"?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai";}
 public String privateKey(String service){return Base64.getEncoder().encodeToString(keys.get(service).getPrivate().getEncoded());}
 public String publicKey(String service){return Base64.getEncoder().encodeToString(keys.get(service).getPublic().getEncoded());}
 public Tokens tokens(String service){try{return new Tokens(service+"-service",privateKey(service),publicKey("user"),publicKey("product"),publicKey("trade"));}catch(Exception e){throw new IllegalStateException(e);}}
 public void database(String service){
  if(!databases.add(service))return;
  try(var root=DriverManager.getConnection(base()+"bootstrap?allowPublicKeyRetrieval=true&useSSL=false","root",mysql.getPassword());var statement=root.createStatement()){
   statement.execute("CREATE DATABASE secondhand_"+service+" CHARACTER SET utf8mb4");
   statement.execute("CREATE USER '"+service+"_app'@'%' IDENTIFIED BY 'test-only-"+service+"-password'");
   statement.execute("GRANT SELECT,INSERT,UPDATE,DELETE ON secondhand_"+service+".* TO '"+service+"_app'@'%'");
  }catch(SQLException e){throw new IllegalStateException(e);}
  var source=new DriverManagerDataSource(url(service),"root",mysql.getPassword());
  new ResourceDatabasePopulator(new ClassPathResource("db/"+service+"/schema.sql")).execute(source);
 }
 public JdbcTemplate db(String service){return new JdbcTemplate(new DriverManagerDataSource(url(service),service+"_app","test-only-"+service+"-password"));}
 public ServletWebServerApplicationContext start(String service,Class<?> main,Map<String,String> extra){
  database(service);var properties=new LinkedHashMap<String,String>();
  properties.put("server.port","0");properties.put("spring.application.name",service+"-service");
  properties.put("spring.datasource.url",url(service));properties.put("spring.datasource.username",service+"_app");properties.put("spring.datasource.password","test-only-"+service+"-password");properties.put("spring.sql.init.mode","never");
  properties.put("app.service-name",service+"-service");properties.put("app.private-key",privateKey(service));
  for(String name:keys.keySet())properties.put("app."+name+"-public-key",publicKey(name));
  properties.put("app.user-url","http://127.0.0.1:1");properties.put("app.product-url","http://127.0.0.1:1");
  properties.put("app.recovery-delay-ms","600000");properties.put("spring.main.banner-mode","off");
  properties.putAll(extra);
  var context=(ServletWebServerApplicationContext)new SpringApplication(main).run(properties.entrySet().stream().map(e->"--"+e.getKey()+"="+e.getValue()).toArray(String[]::new));
  contexts.add(context);return context;
 }
 public static String http(ServletWebServerApplicationContext context){return "http://127.0.0.1:"+context.getWebServer().getPort();}
 public void close(){for(int i=contexts.size()-1;i>=0;i--)contexts.get(i).close();mysql.stop();}
}
