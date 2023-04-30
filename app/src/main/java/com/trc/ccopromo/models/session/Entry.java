package com.trc.ccopromo.models.session;

import java.math.BigDecimal;

  
public class Entry {
    public Entry(String key,DiscountSource source,BigDecimal discount)
    {
        this.Key=key;
        this.discount=discount;
        this.discountsource=source;
    }
    
    public String Key;
    public BigDecimal discount;
    public DiscountSource discountsource;
}
