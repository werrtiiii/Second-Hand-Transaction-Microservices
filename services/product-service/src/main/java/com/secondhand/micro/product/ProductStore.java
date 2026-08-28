package com.secondhand.micro.product;
import com.secondhand.micro.platform.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import java.sql.Statement;
import java.util.*;
@Service
public class ProductStore {
 private final JdbcTemplate db;
 public ProductStore(JdbcTemplate db){this.db=db;}
 public record Product(long id,long sellerId,String title,String description,int priceCent,int quantity,String status,String coverImageUrl,long version){}
 public Product get(long id){
  var rows=db.query("SELECT * FROM products WHERE id=?",(r,n)->new Product(r.getLong("id"),r.getLong("seller_id"),r.getString("title"),r.getString("description"),r.getInt("price_cent"),r.getInt("quantity"),r.getString("status"),r.getString("cover_image_url"),r.getLong("version")),id);
  if(rows.isEmpty())throw Failure.missing();return rows.get(0);
 }
 public Product create(long seller,ProductController.Create r){
  var keys=new GeneratedKeyHolder();
  db.update(c->{var p=c.prepareStatement("INSERT INTO products(seller_id,title,description,price_cent,quantity,status,cover_image_url,free_shipping,shipping_fee_cent,version,created_at,updated_at) VALUES(?,?,?,?,?,'ON_SALE',?,?,?,0,NOW(),NOW())",Statement.RETURN_GENERATED_KEYS);
   p.setLong(1,seller);p.setString(2,r.title());p.setString(3,r.description());p.setInt(4,r.priceCent());p.setInt(5,r.quantity()==null?1:r.quantity());p.setString(6,r.coverImageUrl());p.setBoolean(7,Boolean.TRUE.equals(r.freeShipping()));p.setInt(8,r.shippingFeeCent()==null?0:r.shippingFeeCent());return p;},keys);
  return get(keys.getKey().longValue());
 }
 public Map<String,Object> list(int page,int size){
  if(page<0||size<1||size>50)throw new Failure(400,"BAD_REQUEST","分页范围错误");
  var ids=db.queryForList("SELECT id FROM products WHERE status='ON_SALE' ORDER BY id DESC LIMIT ? OFFSET ?",Long.class,size,(long)page*size);
  return Map.of("content",ids.stream().map(this::get).toList(),"number",page,"size",size,"totalElements",db.queryForObject("SELECT COUNT(*) FROM products WHERE status='ON_SALE'",Long.class));
 }
}
