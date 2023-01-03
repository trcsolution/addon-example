package com.trc.ccopromo.models;

public class PromoResponseItemDiscount{
    // @JsonProperty("itemCode") 
    public String getItemCode() { 
		 return this.itemCode; } 
    public void setItemCode(String itemCode) { 
		 this.itemCode = itemCode; } 
    String itemCode;
    // @JsonProperty("discount") 
    public double getDiscount() { 
		 return this.discount; } 
    public void setDiscount(double discount) { 
		 this.discount = discount; } 
    double discount;
}

