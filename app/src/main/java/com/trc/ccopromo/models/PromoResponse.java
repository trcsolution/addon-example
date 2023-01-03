package com.trc.ccopromo.models;
// import java.util.*;


// // import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// // import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
// /* ObjectMapper om = new ObjectMapper();
// Root root = om.readValue(myJsonString, Root.class); */

// public class PromoResponse{
//     // @JsonProperty("discount") 
//     public String getDiscount() { 
// 		 return this.discount; } 
//     public void setDiscount(String discount) { 
// 		 this.discount = discount; } 
//     String discount;
//     // @JsonProperty("promo") 
//     public ArrayList<PromoResponsePromos> getPromo() { 
// 		 return this.promo; } 
//     public void setPromo(ArrayList<PromoResponsePromos> promo) { 
// 		 this.promo = promo; } 
//     ArrayList<PromoResponsePromos> promo;
//     // @JsonProperty("itemDiscounts") 
//     public ArrayList<PromoResponseItemDiscount> getItemDiscounts() { 
// 		 return this.itemDiscounts; } 
//     public void setItemDiscounts(ArrayList<PromoResponseItemDiscount> itemDiscounts) { 
// 		 this.itemDiscounts = itemDiscounts; } 
//     ArrayList<PromoResponseItemDiscount> itemDiscounts;
//     // @JsonProperty("transactionNumber") 
//     public String getTransactionNumber() { 
// 		 return this.transactionNumber; } 
//     public void setTransactionNumber(String transactionNumber) { 
// 		 this.transactionNumber = transactionNumber; } 
//     String transactionNumber;
//     // @JsonProperty("items") 
//     public ArrayList<PromoResponseItem> getItems() { 
// 		 return this.items; } 
//     public void setItems(ArrayList<PromoResponseItem> items) { 
// 		 this.items = items; } 
//     ArrayList<PromoResponseItem> items;
//     // @JsonProperty("amount") 
//     public int getAmount() { 
// 		 return this.amount; } 
//     public void setAmount(int amount) { 
// 		 this.amount = amount; } 
//     int amount;

    

// }



import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
// public class ItemDiscount{
//     public int promoId;
//     public String itemCode;
//     public String promoName;
//     public int discount;
// }

public class PromoResponse{
    public String transactionNumber;
    public String discount;
    public ArrayList<ItemDiscount> itemDiscounts;
}


