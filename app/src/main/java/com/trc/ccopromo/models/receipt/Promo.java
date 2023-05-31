package com.trc.ccopromo.models.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Promo {
    public Promo(int promoId,String promoName,double discount,ArrayList<PromoItem> products)
    {
        this.setPromoId(promoId);
        this.setPromoName(promoName);
        this.setProducts(products);
        this.setDiscount(discount);
    }

    @JsonProperty("PromoId") 
    public int getPromoId() { 
		 return this.promoId; } 
    public void setPromoId(int promoId) { 
		 this.promoId = promoId; } 
    int promoId;
    @JsonProperty("PromoName") 
    public String getPromoName() { 
		 return this.promoName; } 
    public void setPromoName(String promoName) { 
		 this.promoName = promoName; } 
    String promoName;

     @JsonProperty("products") 
    public ArrayList<PromoItem> getProducts() { 
		 return this.products; } 
    public void setProducts(ArrayList<PromoItem> products) { 
		 this.products = products; } 
    ArrayList<PromoItem> products;
        
    @JsonProperty("Discount") 
    public double getDiscount() { 
		 return this.discount; } 
    public void setDiscount(double discount) { 
		 this.discount = discount; } 
    double discount;

}
