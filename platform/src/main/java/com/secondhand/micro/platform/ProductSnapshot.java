package com.secondhand.micro.platform;
public record ProductSnapshot(Long id,Long sellerId,String title,String coverImageUrl,Integer priceCent,Integer quantity,String status,Long version){
 public Long getId(){return id;} public Long getSellerId(){return sellerId;}
 public String getTitle(){return title;} public String getCoverImageUrl(){return coverImageUrl;}
 public Integer getPriceCent(){return priceCent;} public Integer getQuantity(){return quantity;} public String getStatus(){return status;}
}
