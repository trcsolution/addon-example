// package com.trc.ccopromo;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.net.URISyntaxException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sap.scco.ap.pos.entity.ReceiptEntity;
// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.sap.scco.ap.pos.entity.discountrule.DiscountType;
// import com.sap.scco.ap.pos.service.CalculationPosService;
// import com.sap.scco.ap.pos.service.SalesItemPosService;
// import com.trc.ccopromo.TransactionTools;
// import com.trc.ccopromo.models.PromoResponse;
// import com.trc.ccopromo.models.session.DiscountSource;

// public class TransactionLogic {
//     private org.slf4j.Logger logger;
//     private CalculationPosService calculationPosService;
//     private SalesItemPosService salesItemPosService;
//     private TrcPromoAddon _addon;

//     public TransactionLogic(TrcPromoAddon addon, CalculationPosService _calculationPosService,SalesItemPosService _salesItemPosService) {
//         logger = LoggerFactory.getLogger(TransactionLogic.class);
//         _addon = addon;
//         calculationPosService = _calculationPosService;
//         salesItemPosService=_salesItemPosService;
//     }

    
    
    

//     public void SetLineDiscount(SalesItemEntity salesItem,BigDecimal discount){
//             salesItem.setPercentageDiscount(false);
//             salesItem.setDiscountAmount(discount);
//             if(discount.compareTo(BigDecimal.ZERO)==0)
//             {
//                 salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.NONPROMO_PROMO_DISCOUNT_CODE);
//                 salesItem.setDiscountManuallyChanged(false);
//             }
//                 else
//             {
//                 salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
//                 salesItem.setDiscountManuallyChanged(true);
//             }
//             salesItem.setMarkChanged(true);
//             salesItem.setItemDiscountChanged(true);
            
//         }

//     public void ResetSalesItems(final ReceiptEntity receipt)
//     {
//         receipt.getSalesItems().stream().filter(salesItem->
//         IsDiscountableItem(salesItem) 
//         && TransactionTools.HasPromo(salesItem)
//         ).forEach(salesItem->
//                 {
//                     TransactionTools.ClearPromo(salesItem);
//                     if(salesItem.getDiscountElements().isEmpty() )
//                      if(_addon.transactionState.getDiscountsource(salesItem.getKey())!=DiscountSource.Manualy)
//                         SetLineDiscount(salesItem,BigDecimal.ZERO);
//                 });
//     }
//     public Boolean IsDiscountableItem(SalesItemEntity salesItem)
//     {
//         return !salesItemPosService.isSalesItemInvalid(salesItem) 
//         && !salesItemPosService.isSalesItemVoid(salesItem)
//         && !salesItemPosService.isVoucherSalesItem(salesItem)
//         && salesItem.getQuantity().compareTo(BigDecimal.ZERO)>0;
//         // && salesItem.getDiscountElements().isEmpty()  
//     }
//     public com.trc.ccopromo.models.PromoRequest MakePromoRequest(ReceiptEntity receipt,ArrayList<Integer> refPromos)
//     {
//         com.trc.ccopromo.models.PromoRequest promorequest = new com.trc.ccopromo.models.PromoRequest();
//         promorequest.items=new ArrayList<com.trc.ccopromo.models.PromoRequestItem>(
//                 receipt.getSalesItems().stream().filter(a -> !a.getStatus().equals("3") &&  a.getMaterial() != null
//                 //&& a.getDiscountElements().isEmpty()
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


//     public void CalculatePromotios(ReceiptEntity receipt) throws IOException, InterruptedException, URISyntaxException {
//         ResetSalesItems(receipt);
//         if(receipt.getSalesItems().stream().anyMatch(salesItem->IsDiscountableItem(salesItem)
//         //  && salesItem.getDiscountElements().isEmpty()
//          ))
//         {
//             PromoResponse promos = RequestPromo(receipt,null);
//             if(promos!=null)
//                 if(promos.itemDiscounts!=null)
//                     if(!promos.itemDiscounts.isEmpty())
//             {
//                 ApplyPromoDiscountsToTransaction(promos,receipt);
//             }
//         }
//     }
//     public  void ApplyPromoDiscountsToTransaction(PromoResponse promoResp,ReceiptEntity receipt)
//         {
//             //if(AllowRecalculate)
//         //      calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
//             var promoDiscounts=promoResp.itemDiscounts.stream().collect(Collectors.groupingBy(a->a.promoId,Collectors.summingDouble(a->a.discount)));
//             promoDiscounts.keySet().forEach(a->
//                     {
//                         //promoResp.itemDiscounts.
//                         List<String> items=promoResp.itemDiscounts.stream().filter(b->b.promoId==a.intValue()).map(b->b.itemCode).collect(Collectors.toList());
//                         ApplyPromoDiscount(receipt,items,a,promoDiscounts.get(a));
//                     });
//         }

//     private void ApplyPromoDiscount(ReceiptEntity receipt,List<String> items,int PromoId,Double _discount) {
        
//         var totalAmount=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId()))
//             .mapToDouble(a->a.getUnitGrossAmount().multiply(a.getQuantity())
//             .doubleValue()).sum();

//         var totalcurrentDiscount=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId())).mapToDouble(a->a.getDiscountAmount().doubleValue()).sum();

//         final Double discount=BigDecimal.valueOf(totalcurrentDiscount).compareTo(BigDecimal.ZERO)==0?_discount:_discount+totalcurrentDiscount;

//         // if(BigDecimal.valueOf(totalcurrentDiscount).compareTo(BigDecimal.ZERO)!=0)
//         //     discount=discount+totalcurrentDiscount;

//         var salesItems=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId()));
//         salesItems.forEach(salesItem->
//         {
//             BigDecimal tineTotal=salesItem.getUnitGrossAmount().multiply(salesItem.getQuantity());

//             var k=tineTotal.doubleValue()/totalAmount;
//             salesItem.setPaymentGrossAmount(tineTotal);
//             var linediscount=BigDecimal.valueOf(k*discount);
            
//             // if(salesItem.getDiscountElements().isEmpty() 
//             // && !this._addon.transactionState.items.stream().anyMatch(a->a.Key.equals(salesItem.getKey()) 
//             // && a.discountsource==DiscountSource.Manualy 
//             // )
//             // && TransactionTools.IsPromoDiscount(salesItem)
//             // )  
//             {
//                 TransactionTools.ClearCoupon(salesItem);

//                 // receipt.getCouponAssignments().clear();
//                 // receipt.getDiscountElements().clear();

//                 // receipt.setDiscountElements(null);

//                 salesItem.getDiscountElements().forEach(a->
//                 {
//                     receipt.getCouponAssignments().remove(a.getCouponAssignment());
//                     receipt.getDiscountElements().remove(a);
//                 }
//                 );
//                 salesItem.setPaymentGrossAmountWithoutReceiptDiscount(tineTotal.subtract(linediscount));
//                 SetLineDiscount(salesItem,linediscount);
//                 salesItem.setUnitPriceChanged(true);

//                 TransactionTools.SetNote(salesItem,  "Promo:"+String.valueOf(PromoId));
                
//                 TransactionTools.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID,Integer.toString(PromoId));
//             }
//         //    setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_TYPE,Integer.toString(discount.promoType));

//             // logger.info(String.valueOf(k));
//         });
//         receipt.getCouponAssignments().clear();
//         receipt.getDiscountElements().clear();

//     }
    
// }
