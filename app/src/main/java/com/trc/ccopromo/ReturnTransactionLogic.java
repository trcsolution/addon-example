package com.trc.ccopromo;

import org.apache.poi.hpsf.Decimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.ReceiptCalculationMetaData;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
import com.trc.ccopromo.models.ItemDiscount;
import com.trc.ccopromo.models.ReturnItemDiscount;



public class ReturnTransactionLogic {
    private TrcPromoAddon _addon;
    private ReceiptManager receiptManager;
    private CalculationPosService calculationPosService;
    private org.slf4j.Logger logger;
    private TransactionLogic transactionlogic;
    public ReturnTransactionLogic(TrcPromoAddon addon, ReceiptManager _receiptManager,CalculationPosService _calculationPosService,
    TransactionLogic _transactionlogic) {
        logger = LoggerFactory.getLogger(ReturnTransactionLogic.class);
        receiptManager = _receiptManager;
        _addon = addon;
        calculationPosService = _calculationPosService;
        transactionlogic=_transactionlogic;
    }

    // static <T,K> Collector<T,?,Map<K,List<T>>> 
    //  groupingBy(Function<? super T,? extends K> classifier)
    class PROMOTYPE {
        static final int NONE = 0,SIMPLE = 1, BUYANDGET = 2, FIXEDPRIXEFIXEDQTY = 3,SPENDANDGET=4;
        }
        
    int getPromoType(SalesItemEntity entry)
    {
        
        if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_TYPE)==null)
                return PROMOTYPE.NONE;
            else
                return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_TYPE).getValue());
    }
    int getPromoId(SalesItemEntity entry)
    {
        if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
                return Integer.valueOf(0);
            else
                return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue());
    }
    Boolean NotCanceled(SalesItemEntity item)
    {
         return !item.getStatus().equals("3");
    }

    
    List<com.trc.ccopromo.models.storedpromo.StoredPromo> getPromotionsFromAdditionalItms(ReceiptEntity receipt)
    {   
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

    com.trc.ccopromo.models.PromoResponse getTransactionDiscounts(ReceiptEntity receipt,List<com.trc.ccopromo.models.storedpromo.StoredPromo> usedpromos) throws IOException, InterruptedException, URISyntaxException
    {
        var request=transactionlogic.MakePromoRequest(receipt, null);
        request.promotions=usedpromos;
        var response=transactionlogic.PostCalculationRequest(request);
        ObjectMapper m = new ObjectMapper();
        return m.readValue(response, com.trc.ccopromo.models.PromoResponse.class);
    }

    Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt,String itemCode)
    {
        return receipt.getSalesItems().stream().filter(b->itemCode.equalsIgnoreCase(b.getId()) && !b.getStatus().equalsIgnoreCase("3") );
    }
    static double OriginaSpentAmount=0;
    public void ItemForReturn(ReturnReceiptObject returnReciept,boolean isStartReturn) throws IOException, InterruptedException, URISyntaxException
    {
        ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
        ReceiptEntity actualOriginalReceipt1 = transactionlogic.LoadReceipt(returnReciept.getSourceReceipt().getId());

        

        
        var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt1);
        var reminingDescounts=getTransactionDiscounts(sourceReceipt,promotions);
        var promoDiscount=Double.parseDouble(reminingDescounts.discount);
        
        if(isStartReturn)
        {
            OriginaSpentAmount=sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equalsIgnoreCase("3") ).mapToDouble(a->a.getGrossAmount().doubleValue()).sum();
            OriginaSpentAmount-=promoDiscount;
            OriginaSpentAmount-=actualOriginalReceipt1.getDiscountAmount().doubleValue();
        }



        if(targetReceipt.getSalesItems().size()>0)
        {
            
                double spentAmount=OriginaSpentAmount;
                double reminingAmount=sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equalsIgnoreCase("3") ).mapToDouble(a->a.getGrossAmount().doubleValue()).sum()
                    -promoDiscount;

                BigDecimal refundingAmount=BigDecimal.valueOf(spentAmount-reminingAmount).setScale(2,RoundingMode.HALF_UP);
                if(refundingAmount.compareTo(BigDecimal.ZERO)<0)
                    refundingAmount=BigDecimal.ZERO;
                BigDecimal planingRefundingAmount=BigDecimal.valueOf(targetReceipt.getSalesItems().stream().filter(a->!a.getStatus().equalsIgnoreCase("3")).mapToDouble(a->a.getGrossAmount().doubleValue()).sum());
                BigDecimal refAmount=refundingAmount;
                for (SalesItemEntity entry : targetReceipt.getSalesItems()) {
                    if(!entry.getStatus().equalsIgnoreCase("3"))
                    {
                        if(refundingAmount.compareTo(BigDecimal.ZERO)==0)
                        {
                            this.transactionlogic.SetLineDiscount(entry,entry.getGrossAmount());
                            TransactionLogic.setAdditionalField(entry, "TRC_Discount",entry.getGrossAmount().toString());

                        }
                        else
                        {
                            var k=entry.getGrossAmount().divide(planingRefundingAmount,MathContext.DECIMAL32);
                            var linerefundamount=refundingAmount.multiply(k).setScale(2,RoundingMode.HALF_UP);
                            // linerefundamount=linerefundamount
                            if(refAmount.compareTo(linerefundamount)<0)
                            {
                                linerefundamount=linerefundamount.subtract(linerefundamount.subtract(refAmount));
                                refAmount=BigDecimal.ZERO;
                            }
                            refAmount=refAmount.subtract(linerefundamount);

                            //rounding
                            if(refAmount.equals(BigDecimal.valueOf(0.01)))
                             {
                                linerefundamount=linerefundamount.add(refAmount);
                                refAmount=BigDecimal.ZERO;
                             }
                             else
                             if(refAmount.equals(BigDecimal.valueOf(-0.01)))
                             {
                                linerefundamount=linerefundamount.subtract(refAmount);
                                refAmount=BigDecimal.ZERO;
                             }

                            var linediscount=entry.getGrossAmount().subtract(linerefundamount);

                            TransactionLogic.setAdditionalField(entry, "TRC_Discount",linediscount.toString());
                            // if(linediscount.compareTo(BigDecimal.ZERO)>0)
                            {
                                this.transactionlogic.SetLineDiscount(entry,linediscount);

                            }
                        }

                    }
                    
                }
                calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
                UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);
        }
        transactionlogic.ResetSalesItems(sourceReceipt);
        transactionlogic.ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt);
        calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);
        var totalrows=BigDecimal.valueOf(sourceReceipt.getSalesItems().stream().filter(a->a.getStatus().compareTo("3")!=0).mapToDouble(a->{
            return a.getGrossAmount().subtract(a.getDiscountAmount()).doubleValue();
        }).sum());
        var discountOriginal=actualOriginalReceipt1.getDiscountAmount();
        // sourceReceipt.setDiscountAmount(discountOriginal);
        sourceReceipt.setPercentageDiscount(false);
        if(totalrows.compareTo(BigDecimal.ZERO)==0)
            discountOriginal=BigDecimal.ZERO;
        // sourceReceipt.setDiscountPurposeCode("1000");
        sourceReceipt.setDiscountAmount(discountOriginal);
        sourceReceipt.setPaymentGrossAmountWithoutReceiptDiscount(totalrows);
        sourceReceipt.setPaymentGrossAmount(totalrows.subtract(discountOriginal));
        // sourceReceipt.setPaymentGrossAmountWithoutReceiptDiscount(BigDecimal.valueOf(16));
        // sourceReceipt.setPaymentGrossAmount(BigDecimal.valueOf(19));
        // var newgrossAmount=sourceReceipt.getTotalGrossAmount().subtract(actualOriginalReceipt.getDiscountAmount());
        //  sourceReceipt.setTotalGrossAmount(BigDecimal.valueOf(15));
        // setTotalGrossAmount(BigDecimal.valueOf(15));
        // sourceReceipt.prin
        // calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);
    }

    public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt)
    {
        int i=0;
        for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
            var targetEntry=targetReceipt.getSalesItems().get(i++);
            var discountStr=entry.getAdditionalField("TRC_Discount");
            if(discountStr!=null)
                if(discountStr.getValue()!=null)
                {
                    var discount=new BigDecimal(discountStr.getValue());
                    targetEntry.setReferenceSalesItem(null);
                    targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(discount));
                    continue;
                }
                targetEntry.setGrossAmount(entry.getGrossAmount());
                targetEntry.setUnitPriceChanged(true);
            
        }
    }
}
