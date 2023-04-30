package com.trc.ccopromo.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.SalesItemNoteEntity;
import com.sap.scco.ap.pos.entity.coupon.DiscountElementEntity;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.pos.util.ui.BroadcasterHolder;

import net.sf.json.JSONObject;

public class Misc {
    public static void setAdditionalField(SalesItemEntity salesItem, String key, String value) {
        AdditionalFieldEntity additionalField2 = salesItem.getAdditionalField(key);
        if (additionalField2 == null) {
            if (value == null)
                return;
            additionalField2 = new AdditionalFieldEntity();
            salesItem.addAdditionalField(additionalField2);
        }
        additionalField2.setFieldName(key);
        additionalField2.setGroupName(com.trc.ccopromo.models.Constants.PROMO_GROUP);
        if(value==null)
        additionalField2.setValue("");
            else
        additionalField2.setValue(value);
    }
    
    public  static ReceiptEntity LoadReceipt(String id)
    {
        CDBSession localSession = CDBSessionFactory.instance.createSession();
        ReceiptPosService localReceiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, localSession);
        var rslt=localReceiptPosService.getReceipt(id);
        localSession.close();
        return rslt;
    }
    
    public static Boolean HasPromo(SalesItemEntity entry)
    {
        var field=entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
        if(field==null)
          return false;
          var rslt=field.getValue();
          if(rslt==null)
           return false;
           return rslt.length()>0;
    }
    public static void ClearPromo(SalesItemEntity entry,boolean clearNote)
    {
        setAdditionalField(entry, com.trc.ccopromo.models.Constants.PROMO_ID,null);
        if(clearNote)
            AddNote(entry,null);
    
    }
    public static void  AddNote(SalesItemEntity salesItem,String Text)
    {
        UUID uuid = UUID.randomUUID();
        AddNote(salesItem,uuid.toString(),Text);
    }
    public static void  AddNote(SalesItemEntity salesItem,String key,String Text)
    {
        if(salesItem.getNotes()!=null)
            if(salesItem.getNotes().stream().anyMatch(a->a.getKey()==key))
        {
            salesItem.getNotes().stream().filter(a->a.getKey()==key).findFirst().get().setText(Text);
            salesItem.setNotes(salesItem.getNotes());
            return;
            
        }
        // else
        {
            salesItem.setNotes(null);
            if(Text==null)
            {
                // salesItem.setNotes(null);
                return;
            }
            // UUID uuid = UUID.randomUUID();
            SalesItemNoteEntity entity=new SalesItemNoteEntity();
            entity.setText(Text);
            entity.setKey(key);
            salesItem.addNote(entity);
            salesItem.setNotes(
                salesItem.getNotes()
                );
        }
        
    }

    

    
    
    public static void SetLineDiscount(SalesItemEntity salesItem,BigDecimal discount){
        salesItem.setPercentageDiscount(false);
        salesItem.setDiscountAmount(discount);
        if(discount.compareTo(BigDecimal.ZERO)==0)
        {
            salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.NONPROMO_PROMO_DISCOUNT_CODE);
            salesItem.setDiscountManuallyChanged(false);
        }
            else
        {
            salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
            salesItem.setDiscountManuallyChanged(true);
        }
        salesItem.setMarkChanged(true);
        salesItem.setItemDiscountChanged(true);
        
    }
    public static Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt)
    {
        return receipt.getSalesItems().stream().filter(b-> !b.getStatus().equalsIgnoreCase("3") );
    }
    public static void ClearCoupon(SalesItemEntity salesItem)
    {
        var discounts=salesItem.getDiscountElements();
        if(!discounts.isEmpty())
        {
            salesItem.getDiscountElements().forEach(discount->
            {
                salesItem.getReceipt().getCouponAssignments().remove(discount.getCouponAssignment());
                salesItem.getReceipt().getDiscountElements().remove(discount);    
            });
            for (DiscountElementEntity discount : discounts) {

                
                // var discount=discounts.get(0);
                discount.getAppliedRule().getCouponAssignments().clear();
                discount.getCouponAssignment().getAppliedRules().clear();
                discount.setCouponAssignment(null);

                // salesItem.getReceipt().getCouponAssignments().remove(discount.getCouponAssignment());
                // salesItem.getReceipt().getDiscountElements().remove(discount);    
            
            }
        salesItem.getDiscountElements().clear();

        
        }
    }
    public static int getPromoId(SalesItemEntity entry)
    {
        
        if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
                return Integer.valueOf(0);
            else
                return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue());
    }
    public static void pushEvent(String eventName, Object payload) {
        JSONObject oJSON = new JSONObject();
        oJSON.put("type", "event");
        oJSON.put("eventName", eventName);
        if (payload != null) {
            oJSON.put("eventPayload", payload);
        }
        if (BroadcasterHolder.INSTANCE.getBroadcaster() != null) {
            BroadcasterHolder.INSTANCE.getBroadcaster().broadcastActionForPath("/ws/tech", "plugin", oJSON);
        }
    }
    public static void showMessageToNewUI(String msg) {
        JSONObject oJSON = new JSONObject();
        oJSON.put("type", "event");
        oJSON.put("eventName", "SHOW_MESSAGE_BOX");
        oJSON.put("eventPayload", msg);

        BroadcasterHolder.INSTANCE.getBroadcaster().broadcastActionForPath("/ws/tech", "plugin", oJSON);
    }

    public static com.trc.ccopromo.models.PromoRequest MakePromoRequest(ReceiptEntity receipt,ArrayList<Integer> refPromos)
    {
        com.trc.ccopromo.models.PromoRequest promorequest = new com.trc.ccopromo.models.PromoRequest();
        promorequest.items=new ArrayList<com.trc.ccopromo.models.PromoRequestItem>(
                receipt.getSalesItems().stream().filter(a -> !a.getStatus().equals("3") &&  a.getMaterial() != null
                //&& a.getDiscountElements().isEmpty()
                ).map(item -> 
                    new com.trc.ccopromo.models.PromoRequestItem(item.getId(),item.getMaterial().getArticleGroup().getId(),item.getQuantity().intValue(),item.getUnitGrossAmount().doubleValue())
                ).collect(Collectors.toList()));
        promorequest.transactionNumber=receipt.getId();
        promorequest.refPromos=refPromos;
        return promorequest;
    
    }
    

}
