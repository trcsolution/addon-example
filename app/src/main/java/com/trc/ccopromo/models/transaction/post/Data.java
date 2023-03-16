package com.trc.ccopromo.models.transaction.post;
// import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Data{
    @JsonProperty("TransactionNumber") 
    public String transactionNumber;
    @JsonProperty("RefTransactionNumber") 
    public String refTransactionNumber;
    @JsonProperty("IsPosted") 
    public boolean isPosted;
    public List<Item> items;
}