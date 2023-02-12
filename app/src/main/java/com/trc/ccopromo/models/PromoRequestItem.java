package com.trc.ccopromo.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromoRequestItem{
    public PromoRequestItem(String itemCode,String group,int qty,double  price)
    {
        this.itemCode=itemCode;
        this.group=group;
        this.qty=qty;
        this.price=price;
    }
    @JsonProperty("itemCode") 
    public String itemCode;
    @JsonProperty("group") 
    public String group;
    @JsonProperty("qty") 
    public int qty;
    @JsonProperty("price") 
    public double  price;


}