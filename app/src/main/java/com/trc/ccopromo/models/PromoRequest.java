// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
package com.trc.ccopromo.models;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
public class PromoRequest{
    // public PromoRequest)
    @JsonProperty("transactionNumber") 
    
    public String transactionNumber;
    @JsonProperty("items") 
    public ArrayList<PromoRequestItem> items;
    @JsonProperty("amount") 
    public double amount;
    @JsonProperty("coupons") 
    public ArrayList<String> coupons;
    @JsonProperty("refPromos") 
    public ArrayList<Integer> refPromos;
    
    @JsonProperty("promotions") 
    
    public List<com.trc.ccopromo.models.storedpromo.StoredPromo> promotions;
    
}

