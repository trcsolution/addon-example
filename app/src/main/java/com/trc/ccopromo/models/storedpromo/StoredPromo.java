package com.trc.ccopromo.models.storedpromo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StoredPromo{
    @JsonProperty("PromoId") 
    public int promoId;
    @JsonProperty("PromoName") 
    public String promoName;
    @JsonProperty("PromoType") 
    public String promoType;

    @JsonProperty("jsonBody") 
    public String jsonBody;
}
