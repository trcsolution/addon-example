package com.trc.ccopromo.models.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coupon {
    public Coupon(String code,double discount)
    {
        this.code = code;
        this.discount = discount;
    }
    @JsonProperty("code") 
    public String getCode() { 
		 return this.code; } 
    public void setCode(String code) { 
		 this.code = code; } 
    String code;


    @JsonProperty("discount") 
    public double getDiscount() { 
		 return this.discount; } 
    public void setDiscount(double discount) { 
		 this.discount = discount; } 
    double discount;


}
