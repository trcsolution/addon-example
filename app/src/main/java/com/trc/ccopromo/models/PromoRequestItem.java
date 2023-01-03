package com.trc.ccopromo.models;


public class PromoRequestItem{
    // @JsonProperty("itemCode") 
    public String getItemCode() { 
		 return this.itemCode; } 
    public void setItemCode(String itemCode) { 
		 this.itemCode = itemCode; } 
    String itemCode;
    // @JsonProperty("group") 
    public String getGroup() { 
		 return this.group; } 
    public void setGroup(String group) { 
		 this.group = group; } 
    String group;
    // @JsonProperty("qty") 
    public int getQty() { 
		 return this.qty; } 
    public void setQty(int qty) { 
		 this.qty = qty; } 
    int qty;
    // @JsonProperty("price") 
    public double getPrice() { 
		 return this.price; } 
    public void setPrice(double price) { 
		 this.price = price; } 
    double  price;

    

}