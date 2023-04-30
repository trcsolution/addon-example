// package com.trc.old;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sap.scco.ap.pos.dao.CDBSession;
// import com.sap.scco.ap.pos.dao.CDBSessionFactory;
// // import com.sap.scco.ap.pos.dao.PersistenceManager;
// // import com.sap.scco.ap.pos.dao.ReceiptManager;
// import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
// import com.sap.scco.ap.pos.entity.ReceiptEntity;
// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
// import java.io.IOException;
// import java.math.BigDecimal;
// // import java.math.MathContext;
// import java.net.URISyntaxException;
// import java.util.ArrayList;
// // import java.util.Optional;
// import com.sap.scco.ap.pos.service.CalculationPosService;
// import com.sap.scco.ap.pos.service.ReceiptPosService;
// import com.sap.scco.ap.pos.service.ServiceFactory;
// import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
// // import com.sap.scco.cs.utilities.ReceiptHelper;
// import com.sap.scco.env.UIEventDispatcher;
// import com.sap.scco.util.CConst;
// import com.trc.ccopromo.TrcPromoAddon;
// import com.trc.ccopromo.TrcPromoAddonOld;
// import com.trc.ccopromo.WebRequest;
// import com.trc.ccopromo.models.PromoResponse;
// import com.trc.ccopromo.models.transaction.post.PostTransactionRequest;

// import org.slf4j.LoggerFactory;
// // import java.util.concurrent.Future;
// // import java.util.concurrent.TimeUnit;
// // import java.util.concurrent.TimeoutException;
// // import java.util.stream.Collector;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
// // import java.util.concurrent.CompletableFuture;
// // import java.util.concurrent.ExecutionException;
// // import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
// // import com.sap.scco.util.security.InsufficientPermissionException;
// // import com.trc.ccopromo.models.ItemDiscount;
// // import org.apache.xpath.operations.Bool;
// // import org.checkerframework.checker.units.qual.A;
// // import org.slf4j.Logger;
// import java.util.List;

// public class TransactionLogic_old {
//     private TrcPromoAddonOld _addon;
//     // private ReceiptEntity transaction = null;
//     // private ReceiptManager receiptManager;
//     private CalculationPosService calculationPosService;
//     // private CDBSession dbSession;

//     private org.slf4j.Logger logger;

//     public TransactionLogic_old(com.trc.ccopromo.TrcPromoAddonOld addon,
//     //  ReceiptManager _receiptManager,
//             CalculationPosService _calculationPosService
//             // , CDBSession _dbSession
//             ) {
                
//         logger = LoggerFactory.getLogger(TrcPromoAddon.class);
//         // receiptManager = _receiptManager;
//         _addon = addon;
//         calculationPosService = _calculationPosService;
//         // dbSession = _dbSession;
//     }

    
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

//     public  ReceiptEntity LoadReceipt(String id)
//     {
//         CDBSession localSession = CDBSessionFactory.instance.createSession();
//         ReceiptPosService localReceiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, localSession);
//         var rslt=localReceiptPosService.getReceipt(id);
//         localSession.close();
//         return rslt;

//     }
//     void ResetReceiptsSales(ReceiptEntity reciept)
//     {
//         reciept.getSalesItems().forEach(salesItem->
//         {
//             if(salesItem.getStatus()!="3")
//                 resetDiscountToSalesItem(salesItem);
//         });
//         calculationPosService.calculate(reciept, EntityActions.CHECK_CONS);
//         UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, reciept);

//     }
    

//     // private void SetUnitAmount(BigDecimal amount, SalesItemEntity adjustmentItem) {
//     //     adjustmentItem.setUnitNetAmount(amount);
//     //     adjustmentItem.setUnitNetAmountOrigin(amount);
//     //     adjustmentItem.setUnitGrossAmount(amount);
//     //     adjustmentItem.setUnitGrossAmountOrigin(amount);
//     // }
    
    
//     Boolean CheckAdjustItemChanging(ReceiptEntity sourceReceipt,ReceiptEntity targetReceipt)
//     {
//         Boolean AdjustItemChanging=Boolean.FALSE;
//         if(!sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && a.getMaterial()==null).findAny().isEmpty())
//         {
//             sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && a.getMaterial()==null).forEach(item ->
//             item.setStatus("3")
//              );
//             calculationPosService.calculate(sourceReceipt, com.sap.scco.ap.pos.entity.BaseEntity.EntityActions.CHECK_CONS);
//             UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);
//             AdjustItemChanging=Boolean.TRUE;
//         }

//         if(!targetReceipt.getSalesItems().stream().filter(a->a.getMaterial()==null).findAny().isEmpty())
//             if(targetReceipt.getSalesItems().stream().filter(a->a.getMaterial()==null).findFirst().get().getStatus().equals("3"))
//         {
//             var adjItem=targetReceipt.getSalesItems().stream().filter(a->a.getMaterial()==null).findFirst().get();
//             adjItem.setQuantity(BigDecimal.ONE);
//             adjItem.setStatus("1");
//             calculationPosService.calculate(targetReceipt, com.sap.scco.ap.pos.entity.BaseEntity.EntityActions.CHECK_CONS);
//             UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);
//             AdjustItemChanging=Boolean.TRUE;
//         }
//         return AdjustItemChanging;
//     }

//     public  ArrayList<java.lang.Integer> getRefPromos(ReceiptEntity receipt)
//     {
//         var rslt=new ArrayList<java.lang.Integer>();
//         receipt.getSalesItems().stream().forEach(a->
//         {
//             var addField=a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
//             if(addField!=null)
//             rslt.add(java.lang.Integer.valueOf(addField.getValue()));
//         });
//         return rslt;

//     }
//     public void PickUpPromoLine(ReturnReceiptObject returnReciept) throws IOException, InterruptedException, URISyntaxException
//     {
//         ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
//         ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
//         ReceiptEntity actualOriginalReceipt = LoadReceipt(returnReciept.getSourceReceipt().getId());
        

//         var refPromos=getRefPromos(actualOriginalReceipt);
//         if(!refPromos.isEmpty())
//         {
//             var promos = RequestPromo(sourceReceipt,refPromos);

//             targetReceipt.getSalesItems().forEach(salesItemEntry->{

//                 BigDecimal actualOrigAmount=BigDecimal.valueOf(actualOriginalReceipt.getSalesItems().stream()
//                                             .filter(a->a.getId().equals(salesItemEntry.getId()))
//                                                 .mapToDouble(a->a.getGrossAmount().subtract(a.getDiscountAmount()).doubleValue()).sum());

//                 BigDecimal sourceAmount=BigDecimal.valueOf(sourceReceipt.getSalesItems().stream()
//                                             .filter(a->a.getId().equals(salesItemEntry.getId()))
//                                                 .mapToDouble(a->a.getGrossAmount().doubleValue()).sum());
//                 BigDecimal targetAmount=salesItemEntry.getGrossAmount();
//                 BigDecimal newDiscount=BigDecimal.ZERO;
//                 var _disc=promos.itemDiscounts.stream().filter(a->a.itemCode.equals(salesItemEntry.getId())).findFirst();
//                 if(!_disc.isEmpty())
//                     newDiscount=BigDecimal.valueOf(_disc.get().discount);
                
//                 var promoAdjuction=actualOrigAmount
//                         .subtract(sourceAmount.
//                             subtract(newDiscount)).
//                                 subtract(targetAmount);
//                 SetLineDiscount(salesItemEntry, promoAdjuction.abs());
//                 salesItemEntry.setUnitPriceChanged(true);
//                 logger.info(promoAdjuction.toString());

//                 var _sourceSalesItem=sourceReceipt.getSalesItems().stream()
//                                             .filter(a->a.getId().equals(salesItemEntry.getId())).findFirst();
//                 if(!_sourceSalesItem.isEmpty())
//                 {
                    
//                     var sourceSalesItem=_sourceSalesItem.get();
//                     SetLineDiscount(sourceSalesItem, newDiscount);
//                     sourceSalesItem.setUnitPriceChanged(true);
//                 }


//             });
//             calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
//             UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);

//             calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);
//             UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);

//         }

//     }
//     public static Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt)
//     {
//         return receipt.getSalesItems().stream().filter(b-> !b.getStatus().equalsIgnoreCase("3") );
//     }

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

//     public void ResetSalesItems(final ReceiptEntity receipt)
//     {
//         receipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")
//         && HasPromo(a)).forEach(salesItem->
//                 {
                    
//                     // if(salesItem.getDiscountElements()==null
//                     //     || salesItem.getDiscountElements()!=null && 
//                     //     salesItem.getDiscountElements().size()==0)
//                     // if(salesItem.getDiscountElements().size()>0)
//                     //     if(salesItem.getDiscountElements().get(0).getDiscountAmount().compareTo(BigDecimal.ZERO)>0)
//                     //      return;
//                     SetLineDiscount(salesItem,BigDecimal.ZERO);
//                     ClearPromo(salesItem);
//                    // this.setAdditionalField(salesItem, null, null);
//                     // salesItem.setUnitPriceChanged(true);
//                 });
//     }
//     BigDecimal _correctionAmount=BigDecimal.ZERO;

//     void ApplyPromoDiscount(ReceiptEntity receipt,List<String> items,int PromoId,Double discount)
//     {
         
//         var salesItems=receipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && items.contains(a.getId()));
//         var totalAmount=salesItems.mapToDouble(a->a.getUnitGrossAmount().multiply(a.getQuantity()).doubleValue()).sum();
//         salesItems=receipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && items.contains(a.getId()));
//         salesItems.forEach(salesItem->
//         {
//             BigDecimal tineTotal=salesItem.getUnitGrossAmount().multiply(salesItem.getQuantity());
//             var k=tineTotal.doubleValue()/totalAmount;
//             salesItem.setPaymentGrossAmount(tineTotal);
//             var linediscount=BigDecimal.valueOf(k*discount);
//             salesItem.setPaymentGrossAmountWithoutReceiptDiscount(tineTotal.subtract(linediscount));
            
//             SetLineDiscount(salesItem,linediscount);

//             setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID,Integer.toString(PromoId));
//         //    setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_TYPE,Integer.toString(discount.promoType));

//             // logger.info(String.valueOf(k));
//         });
//     }
//     public  void ApplyPromoDiscountsToTransaction(PromoResponse promoResp,ReceiptEntity receipt)
//     {
//         //if(AllowRecalculate)
//       //      calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
//         var promoDiscounts=promoResp.itemDiscounts.stream().collect(Collectors.groupingBy(a->a.promoId,Collectors.summingDouble(a->a.discount)));
//         promoDiscounts.keySet().forEach(a->
//                 {
//                     List<String> items=promoResp.itemDiscounts.stream().filter(b->b.promoId==a.intValue()).map(b->b.itemCode).collect(Collectors.toList());
//                     ApplyPromoDiscount(receipt,items,a,promoDiscounts.get(a));
//                 });
//     }
//     public void CalculatePromotios(final ReceiptEntity receipt) throws IOException, InterruptedException, URISyntaxException {
        
//             ResetSalesItems(receipt);
//             boolean isAnySalesItem=receipt.getSalesItems().stream().anyMatch(a->!a.getStatus().equals("3") && a.getQuantity().compareTo(BigDecimal.ZERO)>0);
//             if(isAnySalesItem)
//             {
//             PromoResponse promos = RequestPromo(receipt,null);
            
//             if(promos!=null)
//                 if(promos.itemDiscounts!=null)
//                     if(!promos.itemDiscounts.isEmpty())
//             {
//                 ApplyPromoDiscountsToTransaction(promos,receipt);
//             }
//         }

        
//     }
//     // private void UpdateLines(ReceiptEntity receipt, ItemDiscount discount
//     // //BigDecimal discount
//     // , String[] lines) {
        
//     //     var _discount=BigDecimal.valueOf(discount.discount);
//     //     for(String id : lines)
//     //     {
//     //         var salesItem=receipt.getSalesItems().stream().filter(a->a.getExternalId().equals(id)).findFirst().get();
//     //          _discount=_discount.add(_correctionAmount);
//     //          _correctionAmount=BigDecimal.ZERO;

//     //         if(_discount.compareTo(
//     //             salesItem.getGrossAmount()
//     //             //salesItem.getUnitGrossAmount().multiply(salesItem.getQuantity())
//     //             )<=0)
//     //         {
//     //              var linediscount=_discount.setScale(2,java.math.RoundingMode.HALF_DOWN);
//     //             _correctionAmount=_discount.subtract(linediscount);
//     //             SetLineDiscount(salesItem,linediscount);

//     //             // com.trc.ccopromo.models.Constants.PROMO_ID
//     //             setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID,Integer.toString(discount.promoId));
//     //             setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_TYPE,Integer.toString(discount.promoType));
//     //             break;
//     //         }
//     //         else
//     //         {
//     //             _discount=_discount.subtract(salesItem.getGrossAmount());
//     //             SetLineDiscount(salesItem,salesItem.getGrossAmount());

//     //             setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID,Integer.toString(discount.promoId));
//     //             setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_TYPE,Integer.toString(discount.promoType));
//     //             // salesItem.setUnitPriceChanged(true);
//     //         }
//     //     }
//     //    //return discount;
//     // }

    
    
    
//     public void SetLineDiscount(SalesItemEntity salesItem,BigDecimal discount){
//     // Optional<ItemDiscount> promoitem) {

//         var elements=salesItem.getDiscountElements();
//         if(!elements.isEmpty())
//             return;
//           //  discount=discount.add(elements.get(0).getDiscountAmount());

//         salesItem.setPercentageDiscount(false);
//         salesItem.setDiscountAmount(discount);
//         salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
//         salesItem.setMarkChanged(true);
//         salesItem.setItemDiscountChanged(true);
//         salesItem.setDiscountManuallyChanged(true);

//     }

//     public void resetDiscountToSalesItem(SalesItemEntity salesItem) {

//         // if (salesItem.getDiscountAmount().compareTo(BigDecimal.ZERO) != 0) {
//             SetLineDiscount(salesItem,BigDecimal.ZERO);
//             salesItem.setUnitPriceChanged(true);
//         // }
//     }
//     public com.trc.ccopromo.models.PromoRequest MakePromoRequest(ReceiptEntity receipt,ArrayList<Integer> refPromos)
//     {
//         com.trc.ccopromo.models.PromoRequest promorequest = new com.trc.ccopromo.models.PromoRequest();
//         promorequest.items=new ArrayList<com.trc.ccopromo.models.PromoRequestItem>(
//                 receipt.getSalesItems().stream().filter(a -> !a.getStatus().equals("3") &&  a.getMaterial() != null
//                 && a.getDiscountElements().isEmpty()
//                 ).map(item -> 
//                     new com.trc.ccopromo.models.PromoRequestItem(item.getId(),item.getMaterial().getArticleGroup().getId(),item.getQuantity().intValue(),item.getUnitGrossAmount().doubleValue())
//                 ).collect(Collectors.toList()));
//         promorequest.transactionNumber=receipt.getId();
//         promorequest.refPromos=refPromos;
//         return promorequest;
//     }
//     public String PostCalculationRequest(com.trc.ccopromo.models.PromoRequest promorequest) throws IOException, InterruptedException, URISyntaxException
//     {
//         var request = new WebRequest(_addon.getPluginConfig());
//         var response=request.Post("/api/Promo/Calculate", promorequest,true);
//         return response;
//     }
//     public PromoResponse RequestPromo(ReceiptEntity _transaction,ArrayList<Integer> refPromos) throws IOException, InterruptedException, URISyntaxException {

//         ReceiptEntity receipt = _transaction;
//         logger.info("------RequestPromo------");
//         com.trc.ccopromo.models.PromoRequest promorequest = this.MakePromoRequest(receipt,refPromos);
//         var response=PostCalculationRequest(promorequest);
//         // var request = new WebRequest(_addon.getPluginConfig());
//         // var response=request.Post("/api/Promo/Calculate", promorequest);
//         ObjectMapper m = new ObjectMapper();
//         PromoResponse resp = m.readValue(response, PromoResponse.class);
//         return resp;//request.Request(receipt,refPromos);
//     }
    
//     void postReceipt(ReceiptEntity receipt) 
//         throws IOException, InterruptedException, URISyntaxException 
//     {
//         PostTransactionRequest requestObj=new PostTransactionRequest();
//         requestObj.data=new com.trc.ccopromo.models.transaction.post.Data();
//         requestObj.data.transactionNumber=receipt.getId();
//         requestObj.data.isPosted=false;
//         requestObj.data.items=receipt.getSalesItems().stream().
//             collect(
//                 Collectors.mapping(
//                 a->new com.trc.ccopromo.models.transaction.post.Item(a.getId()
//                 ,a.getMaterial().getArticleGroup().getId()
//                 ,a.getQuantity().doubleValue()
//                 ,a.getUnitGrossAmount().doubleValue()
//                 ,a.getDiscountAmount().doubleValue()
//                 ,a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)
//             ),Collectors.toList()));
//         var request = new WebRequest(_addon.getPluginConfig());
        
//         // try {
//             //save transaction online
//             String json = request.Post("/api/Promo/Save", requestObj,false);
//             //save promos into transaction
//             var mapper = new ObjectMapper();
//             var promos=mapper.readValue("{\"p\":"+json+"}", com.trc.ccopromo.models.storedpromo.StoredPromos.class);
//             for (int i = 0; i < promos.storedPromos.size(); i++) {
//                 var s1=mapper.writeValueAsString(promos.storedPromos.get(i));
//                 setTransactionAdditionalField(receipt,"Promo:"+String.valueOf(i) ,s1);
//             }
//         // } catch (IOException | InterruptedException e) {
//         //     
//         //     e.printStackTrace();
//         // }
//         // request.PostTransaction(receipt);
//     }


//     public void KeepCouponAsAdditionalField(ReceiptEntity receipt) {
//         receipt.getSalesItems().forEach(salesItem->{
//             if(salesItem.getDiscountElements().size()>0)
//             {
//                 // resetDiscountToSalesItem(salesItem);
//                 // salesItem.setDiscountElements(null);
//                 // receipt.setDiscountElements(null);
//                 // receipt.setCouponAssignments(null);
//                 //receipt.
//                 // salesItem.getDiscountAmount().divide(BigDecimal.valueOf(2));

//                 // resetDiscountToSalesItem(salesItem);
//                 // salesItem.setDiscountElements(null);
//                 // receipt.setDiscountElements(null);
//                 // receipt.setCouponAssignments(null);

//                 // SetLineDiscount(salesItem, BigDecimal.valueOf(2));


//                 // salesItem.setUnitPriceChanged(true);
//                 // calculationPosService.recalculateReceipt(receipt);
//                // SetLineDiscount(salesItem, BigDecimal.valueOf(3));

//                 //SetLineDiscount(salesItem, BigDecimal.valueOf(4));

//                 // salesItem.setDiscountElements(null);
//                 // SetLineDiscount(salesItem, BigDecimal.valueOf(2));
//                     // calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
//                 // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);
//             }
//         });
        

        
//     }
// }
