package com.trc.ccopromo.models.storedpromo;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
public class StoredPromos{
    @JsonProperty("p") 
    public ArrayList<StoredPromo> storedPromos;
}
