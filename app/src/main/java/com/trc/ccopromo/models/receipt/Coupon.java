package com.trc.ccopromo.models.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coupon {
    public Coupon(String code,double discount,String name,String validfrom,String expiring)
    {
        this.code = code;
        this.discount = discount;
        this.name=name;
        this.validfrom=validfrom;
        this.expiring=expiring;
    }
    @JsonProperty("code") 
    public String getCode() { 
		 return this.code; } 
    public void setCode(String code) { 
		 this.code = code; } 
    String code;

    @JsonProperty("expiring") 
    public String getExpiring() { 
		 return this.expiring; } 
    public void setExpiring(String expiring) { 
		 this.expiring = expiring; } 
    String expiring;

    @JsonProperty("validfrom") 
    public String getValidfrom() { 
		 return this.validfrom; } 
    public void setValidfrom(String validfrom) { 
		 this.validfrom = validfrom; } 
    String validfrom;

    @JsonProperty("name") 
    public String getName() { 
		 return this.name; } 
    public void setName(String name) { 
		 this.code = name; } 
    String name;


    @JsonProperty("discount") 
    public double getDiscount() { 
		 return this.discount; } 
    public void setDiscount(double discount) { 
		 this.discount = discount; } 
    double discount;

}
