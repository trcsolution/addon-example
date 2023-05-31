package com.trc.ccopromo.models.receipt;

import java.math.BigDecimal;
// import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromoItem {
    public PromoItem(String Id,String description,double amount,double discount,double qty)
    {
        this.setId(Id);
        this.setDescription(description);
        this.setAmount(amount);
        this.setQty(qty);
        this.setDiscount(discount);
    }

     @JsonProperty("Id") 
    public String getId() { 
		 return this.id; } 
    public void setId(String id) { 
		 this.id = id; } 
    String id;
    @JsonProperty("Description") 
    public String getDescription() { 
		 return this.description; } 
    public void setDescription(String description) { 
		 this.description = description; } 
    String description;
    @JsonProperty("Amount") 
    public double getAmount() { 
		 return this.amount; } 
    public void setAmount(double amount) { 
		 this.amount = amount; } 
    double amount;

    @JsonProperty("Qty") 
    public double getQty() { 
		 return this.qty; } 
    public void setQty(double qty) { 
		 this.qty = qty; } 
    double qty;

    @JsonProperty("Discount") 
    public double getDiscount() { 
		 return this.discount; } 
    public void setDiscount(double discount) { 
		 this.discount = discount; } 
    double discount;

}
