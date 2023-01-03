package com.trc.ccopromo.models;


public class PromoResponsePromos{
    // @JsonProperty("id") 
    public int getId() { 
		 return this.id; } 
    public void setId(int id) { 
		 this.id = id; } 
    int id;
    // @JsonProperty("name") 
    public String getName() { 
		 return this.name; } 
    public void setName(String name) { 
		 this.name = name; } 
    String name;
    // @JsonProperty("promoType") 
    public String getPromoType() { 
		 return this.promoType; } 
    public void setPromoType(String promoType) { 
		 this.promoType = promoType; } 
    String promoType;
    // @JsonProperty("items") 
    public String getItems() { 
		 return this.items; } 
    public void setItems(String items) { 
		 this.items = items; } 
    String items;
    // @JsonProperty("count") 
    public int getCount() { 
		 return this.count; } 
    public void setCount(int count) { 
		 this.count = count; } 
    int count;
    // @JsonProperty("discount") 
    public int getDiscount() { 
		 return this.discount; } 
    public void setDiscount(int discount) { 
		 this.discount = discount; } 
    int discount;
}
