// package com.trc.ccopromo;

// import java.math.BigDecimal;
// import java.util.stream.Stream;
// import java.util.ArrayList;
// import java.util.UUID;

// import com.sap.scco.ap.pos.dao.CDBSession;
// import com.sap.scco.ap.pos.dao.CDBSessionFactory;
// import com.sap.scco.ap.pos.service.SalesItemNotePosService;

// import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
// import com.sap.scco.ap.pos.entity.ReceiptEntity;
// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.sap.scco.ap.pos.entity.SalesItemMetaDataEntity;
// import com.sap.scco.ap.pos.entity.SalesItemNoteEntity;
// import com.sap.scco.ap.pos.entity.BaseEntity.MetaInfos;
// import com.sap.scco.ap.pos.entity.coupon.DiscountElementEntity;
// import com.sap.scco.ap.pos.service.ReceiptPosService;
// import com.sap.scco.ap.pos.service.ServiceFactory;
// import com.sap.scco.ap.pos.util.ui.BroadcasterHolder;


// import net.sf.json.JSONObject;

// public class TransactionTools {
//     public static void setAdditionalField(SalesItemEntity salesItem, String key, String value) {
//         AdditionalFieldEntity additionalField2 = salesItem.getAdditionalField(key);
//         if (additionalField2 == null) {
//             if (value == null)
//                 return;
//             additionalField2 = new AdditionalFieldEntity();
//             salesItem.addAdditionalField(additionalField2);
//         }
//         additionalField2.setFieldName(key);
//         additionalField2.setGroupName(com.trc.ccopromo.models.Constants.PROMO_GROUP);
//         if(value==null)
//         additionalField2.setValue("");
//             else
//         additionalField2.setValue(value);
//     }
//     public static void setTransactionAdditionalField(ReceiptEntity receipt, String key, String value) {
//         AdditionalFieldEntity additionalField2 = receipt.getAdditionalField(key);
//         if (additionalField2 == null) {
//             if (value == null)
//                 return;
//             additionalField2 = new AdditionalFieldEntity();
//             receipt.addAdditionalField(additionalField2);
//         }
//         additionalField2.setFieldName(key);
//         additionalField2.setGroupName(com.trc.ccopromo.models.Constants.PROMO_GROUP);
//         if(value==null)
//         additionalField2.setValue("");
//             else
//         additionalField2.setValue(value);
//     }
//     public  static ReceiptEntity LoadReceipt(String id)
//     {
//         CDBSession localSession = CDBSessionFactory.instance.createSession();
//         ReceiptPosService localReceiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, localSession);
//         var rslt=localReceiptPosService.getReceipt(id);
//         localSession.close();
//         return rslt;

    
//     }
//     // public static Boolean IsPromoDiscount(SalesItemEntity entry)
//     // {
//     //     if(entry.getDiscountPurposeCode()==null)
//     //         return false;
//     //         else
//     //         return entry.getDiscountPurposeCode().equals(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
//     // }
//     public static Boolean HasPromo(SalesItemEntity entry)
//     {
//         var field=entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
//         if(field==null)
//           return false;
//           var rslt=field.getValue();
//           if(rslt==null)
//            return false;
//            return rslt.length()>0;
//     }
//     public static void ClearPromo(SalesItemEntity entry)
//     {
//         setAdditionalField(entry, com.trc.ccopromo.models.Constants.PROMO_ID,null);
        
    
//     }
//     public static void  AddNote(SalesItemEntity salesItem,String key,String Text)
//     {
//         if(salesItem.getNotes().stream().anyMatch(a->a.getKey()==key))
//         {
//             salesItem.getNotes().stream().filter(a->a.getKey()==key).findFirst().get().setText(Text);
//             salesItem.setNotes(salesItem.getNotes());
            
//         }
//         else
//         {
//             // var existItems=salesItem.getNotes().stream().map(a->new SalesItemNoteEntity(){
//             //     String key=a.getKey();
//             //     String text=a.getText();
//             // }).toArray();
//             // salesItem.setNotes(null);
//             // for (Object entry : existItems) {
                
//             //     //SalesItemNoteEntity entity=new SalesItemNoteEntity();
//             //     //entity.setText((SalesItemNoteEntity)object.text);
//             //     //entity.setKey(object.key);
//             //     salesItem.addNote((SalesItemNoteEntity)entry);
                
//             // }

            
            
//             // UUID uuid = UUID.randomUUID();
//             SalesItemNoteEntity entity=new SalesItemNoteEntity();
//             entity.setText(Text);
//             entity.setKey(key);
//             // existItems.add(entity);
//             salesItem.addNote(entity);
//             // getNotes().add(entity);
//             // addNote(entity);
//             salesItem.setNotes(
//                 // existItems
//                 salesItem.getNotes()
//                 );
//             // salesItem.setMarkChanged(true);
//         }
        
//     }

    

//     public static void  RemoveNote(SalesItemEntity salesItem,String key)
//     {
        
//     }
//     public static void  SetNote(SalesItemEntity salesItem,String note)
//     {
//         // salesItem.Note
//         if(note==null)
//             salesItem.setNotes(null);
//             else
//             {
//                 ArrayList<SalesItemNoteEntity>  entries=new ArrayList<SalesItemNoteEntity>();

//                 UUID uuid = UUID.randomUUID();
//                 // com.sap.scco.ap.id
//                  SalesItemNoteEntity entity=new SalesItemNoteEntity();
//                  entity.setText(note);
//                  entity.setKey(uuid.toString());
//                 salesItem.setNotes(entries);
//             }
        
//         // salesItem.getNotes().clear();
//         //      if(note!=null)
//         //      {
//         //         UUID uuid = UUID.randomUUID();
//         //         // com.sap.scco.ap.id
//         //          SalesItemNoteEntity entity=new SalesItemNoteEntity();
//         //          entity.setText(note);
                 
//         //           entity.setKey(uuid.toString());
//         //          salesItem.getNotes().add(0,entity);
//         //     }
//         //     else
//         //     salesItem.setNotes(null);
//             // getNotes().notify();


//         // var item=salesItem.getNotes().stream().filter(a->a.getKey()==key).findAny();
//         // if(item.isEmpty())
//         // {
//         //     // com.sap.scco.ap.pos.not
//         //     SalesItemNoteEntity entity=new SalesItemNoteEntity();
//         //     entity.setText(note);
            
//         //     // entity.setKey(key);
//         //     salesItem.addNote(entity);
//         // }
//         // else
//         // {
            
//         //     if(note==null)
//         //         salesItem.getNotes().clear();
//         //         // item.get().setText(null);
//         //             // salesItem.getNotes().remove(item);
//         //         else
//         //             item.get().setText(note);
//         // }
//     }
//     public static void SetLineDiscount(SalesItemEntity salesItem,BigDecimal discount){
//         salesItem.setPercentageDiscount(false);
//         salesItem.setDiscountAmount(discount);
//         if(discount.compareTo(BigDecimal.ZERO)==0)
//         {
//             salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.NONPROMO_PROMO_DISCOUNT_CODE);
//             salesItem.setDiscountManuallyChanged(false);
//         }
//             else
//         {
//             salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
//             salesItem.setDiscountManuallyChanged(true);
//         }
//         salesItem.setMarkChanged(true);
//         salesItem.setItemDiscountChanged(true);
        
//     }
//     public static Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt)
//     {
//         return receipt.getSalesItems().stream().filter(b-> !b.getStatus().equalsIgnoreCase("3") );
//     }
//     public static void ClearCoupon(SalesItemEntity salesItem)
//     {
//         var discounts=salesItem.getDiscountElements();
//         if(!discounts.isEmpty())
//         {
//             salesItem.getDiscountElements().forEach(discount->
//             {
//                 salesItem.getReceipt().getCouponAssignments().remove(discount.getCouponAssignment());
//                 salesItem.getReceipt().getDiscountElements().remove(discount);    
//             });
//             for (DiscountElementEntity discount : discounts) {

                
//                 // var discount=discounts.get(0);
//                 discount.getAppliedRule().getCouponAssignments().clear();
//                 discount.getCouponAssignment().getAppliedRules().clear();
//                 discount.setCouponAssignment(null);

//                 // salesItem.getReceipt().getCouponAssignments().remove(discount.getCouponAssignment());
//                 // salesItem.getReceipt().getDiscountElements().remove(discount);    
            
//             }
//         salesItem.getDiscountElements().clear();

//         // salesItem.setDiscountAmount(BigDecimal.valueOf(3));
//         // salesItem.setDiscountManuallyChanged(true);
//         //  salesItem.setItemDiscountChanged(true);
//         //  salesItem.setUnitPriceChanged(true);
//         // salesItem.setMarkChanged(true);



//         // salesItem.setDiscountManuallyChanged(true);
//         // salesItem.setDiscountAmount(BigDecimal.ZERO);
//         // salesItem.setItemDiscountChanged(true);
//         // salesItem.setMarkChanged(true);
//         // salesItem.setUnitPriceChanged(true);
            
            

            


//             // receipt.getCouponAssignments().clear();
//             // receipt.getDiscountElements().clear();
//             // receipt.setDiscountElements(null);
//         }
//     }
//     public static int getPromoId(SalesItemEntity entry)
//     {
        
//         if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
//                 return Integer.valueOf(0);
//             else
//                 return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue());
//     }
//     public static void pushEvent(String eventName, Object payload) {
//         JSONObject oJSON = new JSONObject();
//         oJSON.put("type", "event");
//         oJSON.put("eventName", eventName);
//         if (payload != null) {
//             oJSON.put("eventPayload", payload);
//         }
//         if (BroadcasterHolder.INSTANCE.getBroadcaster() != null) {
//             BroadcasterHolder.INSTANCE.getBroadcaster().broadcastActionForPath("/ws/tech", "plugin", oJSON);
//         }
//     }
//     public static void showMessageToNewUI(String msg) {
//         JSONObject oJSON = new JSONObject();
//         oJSON.put("type", "event");
//         oJSON.put("eventName", "SHOW_MESSAGE_BOX");
//         oJSON.put("eventPayload", msg);

//         BroadcasterHolder.INSTANCE.getBroadcaster().broadcastActionForPath("/ws/tech", "plugin", oJSON);
//     }

    

// }
