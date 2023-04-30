// package com.trc.old;

// // import org.apache.poi.hpsf.Decimal;
// // import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// // 
// import java.io.IOException;
// import java.math.BigDecimal;
// import java.math.MathContext;
// import java.math.RoundingMode;
// import java.net.URISyntaxException;
// import java.util.ArrayList;
// // import java.util.ArrayList;
// // import java.util.Collections;
// // import java.util.Comparator;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collector;
// // import java.util.Map;
// // import java.util.stream.Collector;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// // import com.google.common.base.Function;
// // import com.sap.scco.ap.pos.dao.ReceiptManager;
// // import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
// // import com.sap.scco.ap.pos.entity.BaseWithAdditionalFieldsEntity;
// // import com.sap.scco.ap.pos.entity.ReceiptCalculationMetaData;
// import com.sap.scco.ap.pos.entity.ReceiptEntity;
// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
// import com.sap.scco.ap.pos.service.CalculationPosService;
// import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
// import com.sap.scco.env.UIEventDispatcher;
// import com.sap.scco.util.CConst;
// import com.trc.ccopromo.TrcPromoAddon;



// public class ReturnTransactionLogic_old {
//     // private TrcPromoAddon _addon;
//     // private ReceiptManager receiptManager;
//     private CalculationPosService calculationPosService;
//     private org.slf4j.Logger logger;
//     private TransactionLogic_old transactionlogic;
//     public ReturnTransactionLogic_old(TrcPromoAddon addon, 
//     // ReceiptManager _receiptManager,
//     CalculationPosService _calculationPosService,
//     TransactionLogic_old _transactionlogic) {
//         logger = LoggerFactory.getLogger(ReturnTransactionLogic_old.class);
//         // receiptManager = _receiptManager;
//         // _addon = addon;
//         calculationPosService = _calculationPosService;
//         transactionlogic=_transactionlogic;
//     }

//     // static <T,K> Collector<T,?,Map<K,List<T>>> 
//     //  groupingBy(Function<? super T,? extends K> classifier)
//     class PROMOTYPE {
//         static final int NONE = 0,SIMPLE = 1, BUYANDGET = 2, FIXEDPRIXEFIXEDQTY = 3,SPENDANDGET=4;
//         }
        
//     int getPromoType(SalesItemEntity entry)
//     {
        
//         if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_TYPE)==null)
//                 return PROMOTYPE.NONE;
//             else
//                 return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_TYPE).getValue());
//     }
//     int getPromoId(SalesItemEntity entry)
//     {
        
//         if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
//                 return Integer.valueOf(0);
//             else
//                 return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue());
//     }
//     Boolean NotCanceled(SalesItemEntity item)
//     {
//          return !item.getStatus().equals("3");
//     }

    
//     List<com.trc.ccopromo.models.storedpromo.StoredPromo> getPromotionsFromAdditionalItms(ReceiptEntity receipt)
//     {   
//         return receipt.getAdditionalFields().stream().filter(a->a.getFieldName().startsWith("Promo:")).map(a->
//                 {
//                     try {
//                         ObjectMapper mapper=new ObjectMapper();
//                         return mapper.readValue(a.getValue(),  com.trc.ccopromo.models.storedpromo.StoredPromo.class);
//                     } catch (JsonProcessingException e) {
//                         e.printStackTrace();
//                     }
//                     return null;
//                 }
//                 ).
//                 collect(Collectors.toList()).stream().filter(a->a!=null).collect(Collectors.toList());
//     }

//     com.trc.ccopromo.models.PromoResponse getTransactionPromoDiscounts(ReceiptEntity receipt,List<com.trc.ccopromo.models.storedpromo.StoredPromo> usedpromos) throws IOException, InterruptedException, URISyntaxException
//     {
//         var request=transactionlogic.MakePromoRequest(receipt, null);
//         request.promotions=usedpromos;
//         var response=transactionlogic.PostCalculationRequest(request);
//         ObjectMapper m = new ObjectMapper();
//         return m.readValue(response, com.trc.ccopromo.models.PromoResponse.class);
//     }

//     Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt)
//     {
//         return receipt.getSalesItems().stream().filter(b-> !b.getStatus().equalsIgnoreCase("3") );
//     }
//     Stream<SalesItemEntity> getSalesItem(ReceiptEntity receipt,String itemCode)
//     {
//         return receipt.getSalesItems().stream().filter(b->itemCode.equalsIgnoreCase(b.getId()) && !b.getStatus().equalsIgnoreCase("3") );
//     }
//     static BigDecimal OriginaSpentAmount=BigDecimal.ZERO;
//     static Boolean IsOriginaHeaderLevelDiscountPercentage=Boolean.FALSE;
//     static BigDecimal OriginaHeaderLevelDiscountPercentage=BigDecimal.ZERO;
//     static BigDecimal OriginaHeaderLevelDiscountAmount=BigDecimal.ZERO;

//     static List<SalesItemEntity> OriginaSpentAmounts=new ArrayList<SalesItemEntity>() {};



//     //
//     void ResetOriginalStates(ReceiptEntity sourceReceipt,ReceiptEntity actualOriginalReceipt,BigDecimal promoDiscount)
//     {
//         IsOriginaHeaderLevelDiscountPercentage=actualOriginalReceipt.isPercentageDiscount();
//         OriginaHeaderLevelDiscountPercentage=actualOriginalReceipt.getDiscountPercentage();
//         OriginaHeaderLevelDiscountAmount=actualOriginalReceipt.getDiscountAmount();

//         OriginaSpentAmount=BigDecimal.valueOf(getSalesItems(sourceReceipt).mapToDouble(a->
//         a.getUnitGrossAmount().multiply(a.getQuantity()).setScale(2,RoundingMode.HALF_UP).doubleValue()).sum());
//         OriginaSpentAmount=OriginaSpentAmount.subtract(promoDiscount);
//         if(IsOriginaHeaderLevelDiscountPercentage)
//             OriginaHeaderLevelDiscountAmount=PercentageToAmount(OriginaSpentAmount,OriginaHeaderLevelDiscountPercentage);
//         OriginaSpentAmount=OriginaSpentAmount.subtract(OriginaHeaderLevelDiscountAmount);

        

//     }

//     BigDecimal PercentageToAmount(BigDecimal amount,BigDecimal percentage)
//     {
//         return amount.multiply(percentage.divide(BigDecimal.valueOf(100)).setScale(2,RoundingMode.HALF_UP).setScale(2,RoundingMode.HALF_UP));
//     }

//     // Orogonal promos with spended amounts
//     static Map<String, Double> m_promos;
//     static Map<String, Map<String, List<SalesItemEntity>>> m_itemPromoMap;
//     public void InitReturn(ReceiptEntity actualOriginalReceipt)
//     {
//         IsOriginaHeaderLevelDiscountPercentage=actualOriginalReceipt.isPercentageDiscount();
//         OriginaHeaderLevelDiscountPercentage=actualOriginalReceipt.getDiscountPercentage();
//         OriginaHeaderLevelDiscountAmount=actualOriginalReceipt.getDiscountAmount();
        
//          m_promos=this.getSalesItems(actualOriginalReceipt).filter(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)!=null)
//                 .collect(Collectors.groupingBy(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue(),Collectors.summingDouble(a->
//                 a.getUnitGrossAmount().multiply(a.getQuantity()).subtract(a.getDiscountAmount()).setScale(2,RoundingMode.HALF_UP).doubleValue()
//             )));

//         m_itemPromoMap=this.getSalesItems(actualOriginalReceipt).filter(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)!=null)
//             .collect(Collectors.groupingBy(a->a.getId(),
//                 Collectors.groupingBy(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue())
//                 ));

//     }
    
//     public void ItemForReturn(ReturnReceiptObject returnReciept,boolean isStartReturn) throws IOException, InterruptedException, URISyntaxException
//     {
//         ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
//         ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
//         ReceiptEntity actualOriginalReceipt = transactionlogic.LoadReceipt(returnReciept.getSourceReceipt().getId());
//          //Get Promotions used for Original transaction
//          if(isStartReturn)
//             InitReturn(actualOriginalReceipt);
//          var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt);
//          //calculate promo discounts
//          var reminingDescounts=getTransactionPromoDiscounts(sourceReceipt,promotions);
//          //Apply remining discount to the left side
//          transactionlogic.ResetSalesItems(sourceReceipt);
//             transactionlogic.ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt);



//         //  var promoDiscount=new BigDecimal(reminingDescounts.discount);


//     }
//     public BigDecimal ItemForReturnOLD(ReturnReceiptObject returnReciept,boolean isStartReturn) throws IOException, InterruptedException, URISyntaxException
//     {
//         ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
//         ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
//         ReceiptEntity actualOriginalReceipt1 = transactionlogic.LoadReceipt(returnReciept.getSourceReceipt().getId());

//         //Get Promotions used for that transaction
//         var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt1);

//         //calculate promo discounts
//         var reminingDescounts=getTransactionPromoDiscounts(sourceReceipt,promotions);
//         var promoDiscount=new BigDecimal(reminingDescounts.discount);
        
//         //On the 'start return' get Spended Amount and Header Discount parameters
//         if(isStartReturn)
//             ResetOriginalStates(sourceReceipt,actualOriginalReceipt1,promoDiscount);


        

//         //Update sourceReceipt /left side list and totals/
//             transactionlogic.ResetSalesItems(sourceReceipt);
//             transactionlogic.ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt);
//             calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);

//             //Calculate Total remining Amount
//             BigDecimal totalReminingAmount=BigDecimal.valueOf( getSalesItems(sourceReceipt).mapToDouble(a->{
//                 return a.getGrossAmount().subtract(a.getDiscountAmount()).setScale(2,RoundingMode.HALF_UP).doubleValue();
//             }).sum()).setScale(2,RoundingMode.HALF_UP);
//             sourceReceipt.setPaymentGrossAmountWithoutReceiptDiscount(totalReminingAmount);
//             if(IsOriginaHeaderLevelDiscountPercentage)
//             {
//                 var discount=PercentageToAmount(totalReminingAmount,OriginaHeaderLevelDiscountPercentage).setScale(2,RoundingMode.HALF_UP);
//                 sourceReceipt.setDiscountAmount(discount);
//                 totalReminingAmount=totalReminingAmount.subtract(discount).setScale(2,RoundingMode.HALF_UP);
//             }
//             else
//             {
//                 totalReminingAmount=totalReminingAmount.subtract(OriginaHeaderLevelDiscountAmount).setScale(2,RoundingMode.HALF_UP);
//             }
//             sourceReceipt.setPaymentGrossAmount(totalReminingAmount);

//         if(targetReceipt.getSalesItems().size()>0)
//         {
//                 BigDecimal refundingAmount=OriginaSpentAmount.subtract(totalReminingAmount).setScale(2,RoundingMode.HALF_DOWN);
//                 if(refundingAmount.compareTo(BigDecimal.ZERO)<0)
//                     refundingAmount=BigDecimal.ZERO;
//                 BigDecimal planingRefundingAmount=BigDecimal.valueOf(getSalesItems(targetReceipt).filter(a->!a.getStatus().equalsIgnoreCase("3")).mapToDouble(a->a.getGrossAmount().doubleValue()).sum());
//                 BigDecimal refAmount=refundingAmount;
//                 for (SalesItemEntity entry : targetReceipt.getSalesItems()) {
//                     if(!entry.getStatus().equalsIgnoreCase("3"))
//                     {
//                         if(refundingAmount.compareTo(BigDecimal.ZERO)==0)
//                         {
//                             this.transactionlogic.SetLineDiscount(entry,entry.getGrossAmount());
//                             TransactionLogic_old.setAdditionalField(entry, "TRC_Discount",entry.getGrossAmount().toString());

//                         }
//                         else
//                         {
//                             var k=entry.getGrossAmount().divide(planingRefundingAmount,MathContext.DECIMAL32);
//                             var linerefundamount=refundingAmount.multiply(k).setScale(2,RoundingMode.HALF_UP);
//                             // linerefundamount=linerefundamount
//                             if(refAmount.compareTo(linerefundamount)<0)
//                             {
//                                 linerefundamount=linerefundamount.subtract(linerefundamount.subtract(refAmount));
//                                 refAmount=BigDecimal.ZERO;
//                             }
//                             refAmount=refAmount.subtract(linerefundamount);
//                             if(refAmount.compareTo(entry.getUnitGrossAmount())<0)
//                              {
//                                 linerefundamount=linerefundamount.add(refAmount);
//                                 refAmount=BigDecimal.ZERO;
//                              }
                             

//                             var linediscount=entry.getGrossAmount().subtract(linerefundamount);

//                             TransactionLogic_old.setAdditionalField(entry, "TRC_Discount",linediscount.toString());
//                             // if(linediscount.compareTo(BigDecimal.ZERO)>0)
//                             {
//                                 this.transactionlogic.SetLineDiscount(entry,linediscount);

//                             }
//                         }

//                     }
                    
//                 }
//                 calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
//                 UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);
//         }
//         UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);
//         return totalReminingAmount;
//     }

//     public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,boolean isWholeReceipt,BigDecimal totalReminingAmount)
//     {
//         int i=0;
//         if(isWholeReceipt)
//         {
//                     // var originalReceipt=transactionlogic.LoadReceipt(TransactionId);
            
//                     for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
//                         var targetEntry=targetReceipt.getSalesItems().get(i++);
        
//                        // var promoId=targetEntry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
//                         //var promoId=entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
//                         // if(promoId==null)
//                         //     continue;
                        
//                         targetEntry.setReferenceSalesItem(null);
//                         targetEntry.setDiscountAmount(BigDecimal.ZERO);
//                         targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(entry.getDiscountAmount()) .divide(entry.getQuantity()));
        
//                     }
//                     if(sourcereceipt.getDiscountAmount().compareTo(BigDecimal.ZERO)>0)
//                     {
//                         //totalReminingAmount
//                         var _total=BigDecimal.valueOf(sourcereceipt.getSalesItems().stream().mapToDouble(a->
//                         a.getUnitGrossAmount()
//                         .multiply(a.getQuantity())
//                         .subtract(a.getDiscountAmount())
//                          .doubleValue()
//                         ).sum());

//                         // targetReceipt.setDiscountAmount(sourcereceipt.getTotalGrossAmount().subtract(totalReminingAmount).negate());
//                         targetReceipt.setDiscountAmount(_total.subtract(totalReminingAmount).negate());

//                         // targetReceipt.setDiscountAmount(sourcereceipt.getDiscountAmount().negate());

//                         targetReceipt.setPercentageDiscount(false);
                        
//                     }
                    

//         }
//         else
//         {
//                 for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
//                     var targetEntry=targetReceipt.getSalesItems().get(i++);
//                     var discountStr=entry.getAdditionalField("TRC_Discount");
//                     if(discountStr!=null)
//                         if(discountStr.getValue()!=null)
//                         {
//                             var discount=new BigDecimal(discountStr.getValue());
//                             targetEntry.setReferenceSalesItem(null);
//                             targetEntry.setDiscountAmount(BigDecimal.ZERO);
//                             targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(discount).divide(entry.getQuantity()));
//                             // targetEntry.setUnitPriceChanged(true);
//                             continue;
//                         }
//                         targetEntry.setGrossAmount(entry.getGrossAmount());
//                         targetEntry.setUnitPriceChanged(true);
//                 }
//         }
//     }
// }
