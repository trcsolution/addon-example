package com.trc.ccopromo.models;
import java.util.ArrayList;

public class Coupons {
    public Coupons(){
        this.coupons=new ArrayList<>();
    }
    public Coupons(ArrayList<Coupon> _coupons){
        this.coupons=_coupons;
    }
    public ArrayList<Coupon> coupons;
    
}