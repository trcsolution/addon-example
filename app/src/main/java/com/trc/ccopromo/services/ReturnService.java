package com.trc.ccopromo.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.trc.ccopromo.TrcPromoAddon;

public class ReturnService extends BasePromoService {
    public ReturnService(TrcPromoAddon addon,CDBSession dbSession){
        super(addon, dbSession);
    }

        static Map<String, Double> m_spent;

        private Map<String, Double> getPromoSpend(ReceiptEntity receipt)
        {
            return this.getSalesItems(receipt)//.filter(a->this.getPromoId(a)!=null)
                .collect(Collectors.groupingBy(a->a.getId(),Collectors.summingDouble(a->
                a.getUnitGrossAmount().multiply(a.getQuantity()).subtract(a.getDiscountAmount()).setScale(2,RoundingMode.HALF_UP).doubleValue()
            )));
        }
        

        void CopyPromotions(ReceiptEntity actualOriginalReceipt,ReceiptEntity sourceReceipt)
        {
            getSalesItems(sourceReceipt).forEach(item->
            {
                var originalItem=actualOriginalReceipt.getSalesItems().stream().filter(a->a.getKey()==item.getKey()).findFirst().get();
                var PromoId=getPromoId(originalItem);
                if(PromoId!=null && !PromoId.isBlank())
                {
                    MarkAsPromo(item,getPromoId(originalItem));
                    SetItemAsInitiallyPromo(item,true);
                }

               // Misc.setAdditionalField(item,com.trc.ccopromo.models.Constants.PROMO_ID,PromoId);
            });
        }
        public void InitReturn(ReceiptEntity actualOriginalReceipt,ReceiptEntity sourceReceipt)
        {
            m_spent=getPromoSpend(sourceReceipt);

        }

        List<com.trc.ccopromo.models.storedpromo.StoredPromo> getPromotionsFromAdditionalItms(ReceiptEntity receipt)
        {   
            if(receipt.getAdditionalFields()==null)
              return new ArrayList<com.trc.ccopromo.models.storedpromo.StoredPromo>();
              else
            //   new List<com.trc.ccopromo.models.storedpromo.StoredPromo>();
            return receipt.getAdditionalFields().stream().filter(a->a.getFieldName().startsWith("Promo:")).map(a->
                    {
                        try {
                            ObjectMapper mapper=new ObjectMapper();
                            return mapper.readValue(a.getValue(),  com.trc.ccopromo.models.storedpromo.StoredPromo.class);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    ).
                    collect(Collectors.toList()).stream().filter(a->a!=null).collect(Collectors.toList());
        }

        com.trc.ccopromo.models.PromoResponse getTransactionPromoDiscounts(ReceiptEntity receipt,List<com.trc.ccopromo.models.storedpromo.StoredPromo> usedpromos) throws IOException, InterruptedException, URISyntaxException
        {
            var request=Misc.MakePromoRequest(receipt, null);
            request.promotions=usedpromos;
            var response=webPromoService.PostCalculationRequest(request);

            ObjectMapper m = new ObjectMapper();
            return m.readValue(response, com.trc.ccopromo.models.PromoResponse.class);
        }

        BigDecimal getHeaderDiscountPercent(ReceiptEntity sourceReceipt)
        {
            BigDecimal rslt=BigDecimal.valueOf(
                sourceReceipt.getCouponAssignments().stream().filter(ca->
            {
                if(ca.getCoupon()!=null)
                  if(ca.getCoupon().getCouponItem()!=null)
                    if(ca.getCoupon().getCouponItem().getDiscountRules()!=null)
                    if(!ca.getCoupon().getCouponItem().getDiscountRules().isEmpty())
                        //     ca.getCoupon().getCouponItem().getDiscountRules().stream().filter(a->a.isDiscountLevelOnHeader()).forEach(rule->{
                        // if(rule.getDiscountType().name().compareTo("PERCENTAGE")==0)
                                return true;
                return false;
            }
            ).mapToDouble(a->
                a.getCoupon().getCouponItem().getDiscountRules().stream().filter(b->b.getDiscountType().name().compareTo("PERCENTAGE")==0).mapToDouble(b->b.getDiscount().doubleValue()).sum()
            ).sum()
            );
            return rslt;
        }
        public void ItemForReturn(ReturnReceiptObject returnReciept,boolean isStartReturn) throws IOException, InterruptedException, URISyntaxException
        {
            ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
            ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
            ReceiptEntity actualOriginalReceipt = Misc.LoadReceipt(returnReciept.getSourceReceipt().getId());
            //Get Promotions used for Original transaction
            if(isStartReturn)
              CopyPromotions(actualOriginalReceipt,sourceReceipt);
            BigDecimal headerDiscountPercent=getHeaderDiscountPercent(sourceReceipt);
            
                
            var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt);
            //calculate promo discounts
            var reminingDescounts=getTransactionPromoDiscounts(sourceReceipt,promotions);
            //Apply remining discount to the left side
            ResetSalesItems(sourceReceipt);
            ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt,headerDiscountPercent);

            if(headerDiscountPercent.compareTo(BigDecimal.ZERO)>0)
            {
                getSalesItems(sourceReceipt).filter(a->IsItemInitiallyPromo(a) && !Misc.HasPromo(a)).forEach(salesItem->{
                    var newDiscount=
                    salesItem.getUnitGrossAmount().multiply(salesItem.getQuantity())
                    .setScale(2, RoundingMode.HALF_UP)
                    //.subtract(salesItem.getDiscountAmount())
                    .multiply(
                        headerDiscountPercent.divide(BigDecimal.valueOf(100))
                    ).setScale(2, RoundingMode.HALF_UP);

                    var linediscount=newDiscount;//.add(salesItem.getDiscountAmount());

                    SetLineDiscount(salesItem,linediscount);
                    salesItem.setUnitPriceChanged(true);

                    
                    // a.getDiscountAmount()
                }); 
            }


            
            
            

            if(isStartReturn)
                InitReturn(actualOriginalReceipt,sourceReceipt);
            //Modify Target Items

             var remining=getPromoSpend(sourceReceipt);

             m_spent.keySet().forEach(itemCode->{
             var spendBefore=m_spent.get(itemCode);
             Double reminingAmount=Double.valueOf(0);
             if(remining.keySet().contains(itemCode))
                reminingAmount=remining.get(itemCode);

              var valueForReturn=BigDecimal.valueOf(spendBefore).subtract(BigDecimal.valueOf(reminingAmount)).setScale(2,RoundingMode.HALF_UP);
              BigDecimal returnedAmount=BigDecimal.ZERO;
              //var totalDiscount=targetReceipt.getSalesItems().stream().filter(a->IsDiscountableItem(a)).mapToDouble(a->a.);

             for(int i=0;i<targetReceipt.getSalesItems().size();i++){
                 var salesItem=targetReceipt.getSalesItems().get(i);
                 if(salesItem.getId().equals(itemCode))
                    if(IsDiscountableItem(salesItem))
                        {
                            Misc.ClearPromo(salesItem, true);
                            salesItem.setGrossAmount(salesItem.getQuantity().multiply(salesItem.getUnitGrossAmount()));
                            salesItem.setPaymentGrossAmountWithoutReceiptDiscount(salesItem.getQuantity().multiply(salesItem.getUnitGrossAmount()));

                            var toReturn=valueForReturn.subtract(returnedAmount).setScale(2,RoundingMode.HALF_UP);
                            if(toReturn.compareTo(returnedAmount)>=0)
                            {
                                var linetotal=salesItem.getQuantity().multiply(salesItem.getUnitGrossAmount()).setScale(2,RoundingMode.HALF_UP);
                                BigDecimal linediscount=BigDecimal.ZERO;

                                if(toReturn.compareTo(linetotal)<=0)
                                {
                                    linediscount=linetotal.subtract(toReturn).setScale(2,RoundingMode.HALF_UP);
                                    returnedAmount=returnedAmount.add(toReturn).setScale(2,RoundingMode.HALF_UP);
                                    toReturn=BigDecimal.ZERO;
                                }
                                else
                                {
                                    linediscount=linetotal;
                                    returnedAmount=returnedAmount.add(linediscount).setScale(2,RoundingMode.HALF_UP);
                                    toReturn=BigDecimal.ZERO;
                                }

                                if(linediscount.compareTo(BigDecimal.ZERO)>0)
                                {
                                    Misc.setAdditionalField(salesItem, "TRC_Discount",linediscount.toString());
                                    SetLineDiscount(salesItem, linediscount);
                                    logger.info("Ok");
                                }
                            }

                        }
                }
            });
            // sourceReceipt.setDiscountPercentage(headerDiscountPercent);
            // if(headerDiscountPercent!=null)
            // {
            //     var subTotal=BigDecimal.valueOf(
            //         getSalesItems(sourceReceipt).mapToDouble(a->a.getPaymentGrossAmountWithoutReceiptDiscount().doubleValue()).sum()
            //     ).setScale(2, RoundingMode.HALF_UP);
            //     var discountAmount=subTotal.multiply(headerDiscountPercent.divide(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP);
            //     sourceReceipt.setPaymentGrossAmountWithoutReceiptDiscount(subTotal);
            //     sourceReceipt.setDiscountAmount(discountAmount);
            //     sourceReceipt.setPercentageDiscount(false);
            //     sourceReceipt.setPaymentGrossAmount(subTotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP));
            //     sourceReceipt.setDiscountPurposeCode("1000");
            // }
            // else
                calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);


            calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);

            //  var promoDiscount=new BigDecimal(reminingDescounts.discount);
        }

        public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,boolean isWholeReceipt,BigDecimal totalReminingAmount)
        {
            int i=0;
            if(isWholeReceipt)
            {
                        // // var originalReceipt=transactionlogic.LoadReceipt(TransactionId);
                
                        //     for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
                        //         var targetEntry=targetReceipt.getSalesItems().get(i++);
                        //     if(!Misc.HasPromo(targetEntry))
                        //         continue;
                        //     targetEntry.setReferenceSalesItem(null);
                        //     targetEntry.setDiscountAmount(BigDecimal.ZERO);
                        //     targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(entry.getDiscountAmount()) .divide(entry.getQuantity()));
            
                        // }
                        // if(sourcereceipt.getDiscountAmount().compareTo(BigDecimal.ZERO)>0)
                        // {
                        //     //totalReminingAmount
                        //     var _total=BigDecimal.valueOf(sourcereceipt.getSalesItems().stream().mapToDouble(a->
                        //     a.getUnitGrossAmount()
                        //     .multiply(a.getQuantity())
                        //     .subtract(a.getDiscountAmount())
                        //     .doubleValue()
                        //     ).sum());

                        //     // targetReceipt.setDiscountAmount(sourcereceipt.getTotalGrossAmount().subtract(totalReminingAmount).negate());
                        //     targetReceipt.setDiscountAmount(_total.subtract(totalReminingAmount).negate());

                        //     // targetReceipt.setDiscountAmount(sourcereceipt.getDiscountAmount().negate());

                        //     targetReceipt.setPercentageDiscount(false);
                            
                        // }
                        

            }
            else
            {
                

                    for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
                        var targetEntry=targetReceipt.getSalesItems().get(i++);
                        var discountStr=entry.getAdditionalField("TRC_Discount");
                        if(discountStr!=null)
                            if(discountStr.getValue()!=null)
                            {
                                var discount=new BigDecimal(discountStr.getValue());
                                targetEntry.setReferenceSalesItem(null);
                                targetEntry.setDiscountAmount(BigDecimal.ZERO);
                                targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(discount).divide(entry.getQuantity().abs()));
                                targetEntry.setUnitPriceChanged(true);
                                if(Misc.HasPromo(targetEntry))
                                    Misc.ClearPromo(targetEntry, true);
                                continue;
                            }
                            
                            if(Misc.HasPromo(targetEntry))
                            {
                                // Misc.ClearPromo(entry, true);
                                Misc.ClearPromo(targetEntry, true);
                                targetEntry.setReferenceSalesItem(null);
                                targetEntry.setDiscountAmount(BigDecimal.ZERO);
                                targetEntry.setUnitGrossAmount(entry.getGrossAmount().divide(entry.getQuantity().abs()));
                                targetEntry.setUnitPriceChanged(true);

                                // targetEntry.setGrossAmount(entry.getGrossAmount());
                                
                            }
                    }
                    calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
            }
        }


}
