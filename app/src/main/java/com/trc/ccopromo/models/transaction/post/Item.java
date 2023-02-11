package com.trc.ccopromo.models.transaction.post;

import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;

public class Item
{
    public Item(String itemid,String  groupid,double qty,double price,double discount,AdditionalFieldEntity additionalFieldEntity)
    {
        this.itemId=itemid;
        this.groupId=groupid;
        this.qty=qty;
        this.price=price;
        this.discount=discount;
        this.promoId=0;
        if(additionalFieldEntity!=null)
        this.promoId=Integer.valueOf(additionalFieldEntity.getValue());
    }
    public String itemId;
    public String groupId;
    public double qty;
    public double price;
    public double discount;
    public int promoId;
}
