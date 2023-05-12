package com.trc.ccopromo.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
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
    public ReturnService(TrcPromoAddon addon,CDBSession dbSession,ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,Boolean isStartReturn){
        super(addon, dbSession);
        this.isStartReturn=isStartReturn;
        this.targetReceipt =targetReceipt;
        this.sourceReceipt = sourcereceipt;
        // targetReceipt = returnReciept.getIndividualItemsReceipt();
        // sourceReceipt = returnReciept.getSourceReceipt();
        // actualOriginalReceipt = Misc.LoadReceipt(sourcereceipt.getId());
        
    }
    // public ReturnService(TrcPromoAddon addon,CDBSession dbSession){
    //     super(addon, dbSession);
    // }
        Boolean isStartReturn;
        private ReceiptEntity targetReceipt;
        private ReceiptEntity sourceReceipt;
        // private ReceiptEntity actualOriginalReceipt;
        static Map<String, BigDecimal> m_spent;
        //static Map<String, String> m_itemsToPromo;

        private Map<String, BigDecimal> getPromoSpend(ReceiptEntity receipt)
        {
            return  this.getSalesItems(receipt)
            //.filter(a->this.getPromoId(a)!=null)
                .collect(Collectors.groupingBy(a->
                getInitiallyPromo(a),Collectors.reducing(BigDecimal.ZERO,x->GetLineTotal(x,Boolean.TRUE),BigDecimal::add)));
        }
        
        
        

        void CopyPromotions(ReceiptEntity actualOriginalReceipt,ReceiptEntity sourceReceipt)
        {
            getSalesItems(sourceReceipt).forEach(item->
            {
                var originalItem=actualOriginalReceipt.getSalesItems().stream().filter(a->a.getKey().compareTo(item.getKey())==0).findFirst().get();
                var PromoId=getPromoId(originalItem);
                if(PromoId!=null && !PromoId.isBlank())
                {
                    MarkAsPromo(item,PromoId);
                   // setInitiallyPromo(item,PromoId);
                }

               // Misc.setAdditionalField(item,com.trc.ccopromo.models.Constants.PROMO_ID,PromoId);
            });
        }
        public void InitReturn(ReceiptEntity actualOriginalReceipt,ReceiptEntity sourceReceipt)
        {
            m_spent=getPromoSpend(sourceReceipt);
            // m_itemsToPromo=getSalesItems(sourceReceipt).filter(a->Misc.HasPromo(a)).collect(Collectors.toMap(a->a.getId(),a->getPromoId(a)));
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
            
            // ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
            // ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
            ReceiptEntity actualOriginalReceipt = Misc.LoadReceipt(returnReciept.getSourceReceipt().getId());
            //Get Promotions used for Original transaction
            if(isStartReturn)
              CopyPromotions(actualOriginalReceipt,sourceReceipt);
                else
              CheckModifications(sourceReceipt);
            BigDecimal headerDiscountPercent=getHeaderDiscountPercent(sourceReceipt);
            
                
            var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt);
            if(isStartReturn)
                getSalesItems(sourceReceipt).forEach(item->
                {
                    promotions.forEach(promo->{
                        if(promo.products.contains(item.getId()))
                        {
                            setInitiallyPromo(item,String.valueOf(promo.promoId));
                        }
                    });
                }
                );
            
            
                


            //calculate promo discounts
            var reminingDescounts=getTransactionPromoDiscounts(sourceReceipt,promotions);
            //Apply remining discount to the left side
            ResetSalesItems(sourceReceipt);
            ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt,headerDiscountPercent);

            //Apply System Discounts to the NonDiscounted Items
            getSalesItems(sourceReceipt).filter(a->!getInitiallyPromo(a).isBlank() && getPromoId(a)==null).forEach(salesItem->{

                BigDecimal tineTotal=GetLineTotal(salesItem, false);
                BigDecimal linediscount=BigDecimal.ZERO;

                var customer=sourceReceipt.getBusinessPartner();
                final BigDecimal customerDiscount=customer==null?BigDecimal.ZERO:customer.getDiscountPercentage();
                // .add(headerDiscountPercent);
                if(headerDiscountPercent.compareTo(BigDecimal.ZERO)!=0)
                {
                    linediscount=tineTotal.multiply(customerDiscount).divide(BigDecimal.valueOf(100));

                }

                if(headerDiscountPercent.compareTo(BigDecimal.ZERO)!=0)
                {
                    linediscount=linediscount.add(tineTotal.subtract(linediscount).multiply(headerDiscountPercent).divide(BigDecimal.valueOf(100)));
                }
                salesItem.setPaymentGrossAmountWithoutReceiptDiscount(tineTotal.subtract(linediscount));
                SetLineDiscount(salesItem,linediscount);
                salesItem.setUnitPriceChanged(true);

            });


            if(isStartReturn)
                InitReturn(actualOriginalReceipt,sourceReceipt);
            //Modify Target Items
             var remining=getPromoSpend(sourceReceipt);

             var toReturns=m_spent.keySet().stream().collect(Collectors.toMap(a->a,b->remining.get(b)==null?m_spent.get(b):m_spent.get(b).subtract(remining.get(b))));
             toReturns.forEach((promoId,valueForReturn)->{
                BigDecimal amount=valueForReturn;

                for(int i=0;i<targetReceipt.getSalesItems().size();i++){
                 var salesItem=targetReceipt.getSalesItems().get(i);
                 if(IsDiscountableItem(salesItem))
                 {
                    
                    if(promotions.stream().anyMatch( a->String.valueOf(a.promoId).equals(promoId) && a.products.contains(salesItem.getId()) ))
                    {
                        Misc.ClearPromo(salesItem, true);
                        salesItem.setGrossAmount(salesItem.getQuantity().multiply(salesItem.getUnitGrossAmount()));
                        salesItem.setPaymentGrossAmountWithoutReceiptDiscount(salesItem.getQuantity().multiply(salesItem.getUnitGrossAmount()));

                        var linetotal=GetLineTotal(salesItem, false);
                        BigDecimal linediscount=BigDecimal.ZERO;
                        if(linetotal.compareTo(amount)<0)
                        {   
                            linediscount=BigDecimal.ZERO;
                            amount=amount.subtract(linetotal);
                        }
                        else
                        {
                            
                            linediscount=linetotal.subtract(amount);
                            amount=BigDecimal.ZERO;
                        }
                        if(linediscount.compareTo(BigDecimal.ZERO)>=0)
                                {
                                    Misc.setAdditionalField(salesItem, "TRC_Discount",linediscount.toString());
                                    SetLineDiscount(salesItem, linediscount);
                                }

                    }
                 }
                }
             });
            calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);
            calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
            //  var promoDiscount=new BigDecimal(reminingDescounts.discount);
        }

        private void CheckModifications(ReceiptEntity sourceReceipt) {
            getSalesItems(sourceReceipt).forEach(item->
            {
                if(item.getStatus().compareTo("5")==0 && getInitiallyPromo(item).isBlank())
                {
                    var _originalItem=getSalesItems(sourceReceipt).filter(a->a.getId().compareTo(item.getId())==0 && !getInitiallyPromo(a).isBlank()).findFirst();
                    if(!_originalItem.isPresent())
                        _originalItem=sourceReceipt.getSalesItems().stream().filter(a->
                        {

                            return a.getId().compareTo(item.getId())==0 && !getInitiallyPromo(a).isBlank();
                        }
                        ).findFirst();
                    if(_originalItem.isPresent())
                    {
                        var originalItem=_originalItem.get();
                        setInitiallyPromo(item,getInitiallyPromo(originalItem));
                    }
                }
            });
        }




        public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,boolean isWholeReceipt,BigDecimal totalReminingAmount)
        {
            int i=0;
            if(isWholeReceipt)
            {
                
                for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) 
                {
                    var targetEntry=targetReceipt.getSalesItems().get(i++);
                    if(!Misc.HasPromo(targetEntry))
                        continue;
                    targetEntry.setReferenceSalesItem(null);
                     targetEntry.setUnitGrossAmount(entry.getUnitGrossAmount());
                     targetEntry.setDiscountAmount(entry.getDiscountAmount().negate());
                    

                }
                if(sourcereceipt.getDiscountAmount().compareTo(BigDecimal.ZERO)>0)
                {
                    //totalReminingAmount
                    var _total=BigDecimal.valueOf(sourcereceipt.getSalesItems().stream().mapToDouble(a->
                    a.getUnitGrossAmount()
                    .multiply(a.getQuantity())
                    .subtract(a.getDiscountAmount())
                    .doubleValue()
                    ).sum());

                    // targetReceipt.setDiscountAmount(sourcereceipt.getTotalGrossAmount().subtract(totalReminingAmount).negate());
                    targetReceipt.setDiscountAmount(_total.subtract(totalReminingAmount).negate());

                    // targetReceipt.setDiscountAmount(sourcereceipt.getDiscountAmount().negate());

                    targetReceipt.setPercentageDiscount(false);
                    
                }
                        

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
