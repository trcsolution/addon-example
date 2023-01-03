// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
package com.trc.ccopromo.models;
import java.util.*;
// import java.util.concurrent.atomic.AtomicBoolean;
// import java.util.stream.Collectors;


public class PromoRequest{
    // @JsonProperty("transactionNumber") 
    public String getTransactionNumber() { 
		 return this.transactionNumber; } 
    public void setTransactionNumber(String transactionNumber) { 
		 this.transactionNumber = transactionNumber; } 
    String transactionNumber;
    // @JsonProperty("items") 
    public ArrayList<PromoRequestItem> getItems() { 
		 return this.items; } 
    public void setItems(ArrayList<PromoRequestItem> items) { 
		 this.items = items; } 
    ArrayList<PromoRequestItem> items;
    // @JsonProperty("amount") 
    public int getAmount() { 
		 return this.amount; } 
    public void setAmount(int amount) { 
		 this.amount = amount; } 
    int amount;
    // @JsonProperty("coupons") 
    public ArrayList<String> getCoupons() { 
		 return this.coupons; } 
    public void setCoupons(ArrayList<String> coupons) { 
		 this.coupons = coupons; } 
    ArrayList<String> coupons;
    // @JsonProperty("refPromos") 
    public ArrayList<Integer> getRefPromos() { 
		 return this.refPromos; } 
    public void setRefPromos(ArrayList<Integer> refPromos) { 
		 this.refPromos = refPromos; } 
    ArrayList<Integer> refPromos;
    
}

