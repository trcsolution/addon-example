package com.trc.ccopromo.models;

import com.fasterxml.jackson.annotation.JsonInclude;

// @JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ItemDiscount{
    public int promoId;
    public String itemCode;
    // @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    public int promoType;
    public String promoName;
    public double discount;
}
