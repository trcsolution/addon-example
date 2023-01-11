package com.trc.ccopromo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
import com.trc.ccopromo.models.ItemDiscount;

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

    public void PickUpPromoLine(ReturnReceiptObject returnReciept) throws IOException, InterruptedException
    {
        ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
        ReceiptEntity actualOriginalReceipt = transactionlogic.LoadReceipt(returnReciept.getSourceReceipt().getId());

        Map<String, Double> discounts = 
        actualOriginalReceipt.getSalesItems().stream().filter(a->a.isDiscountable() && !a.getStatus().equalsIgnoreCase("3") 
            && a.getDiscountAmount().compareTo(BigDecimal.ZERO)>0
        )
        .collect(
            Collectors.groupingBy(
                map->map.getId(),
                Collectors.summingDouble(a->a.getDiscountAmount().doubleValue())
            )
        );
        targetReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).forEach(salesItem->
        {
            transactionlogic.SetLineDiscount(salesItem,BigDecimal.ZERO);
            salesItem.setUnitPriceChanged(true);
        });

        sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).forEach(salesItem->
        {
            transactionlogic.SetLineDiscount(salesItem,BigDecimal.ZERO);
            salesItem.setUnitPriceChanged(true);
        });


        for (String name : discounts.keySet()) {
             BigDecimal discount=BigDecimal.valueOf(discounts.get(name));
            var lines=targetReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && a.getId().equals(name)).map(a->a.getExternalId()).toArray(String[]::new);
            discount = UpdateLines(targetReceipt, discount, lines);
            if(discount.compareTo(BigDecimal.ZERO)>0)
            {
                var lines1=sourceReceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3") && a.getId().equals(name)).map(a->a.getExternalId()).toArray(String[]::new);
                discount = UpdateLines(sourceReceipt,discount,lines1);
            }
        }

        calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);

        calculationPosService.calculate(sourceReceipt, EntityActions.CHECK_CONS);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);


    }

    private BigDecimal UpdateLines(ReceiptEntity receipt, BigDecimal discount, String[] lines) {
        for(String id : lines)
        {
            var salesItem=receipt.getSalesItems().stream().filter(a->a.getExternalId().equals(id)).findFirst().get();
            if(discount.compareTo(salesItem.getGrossAmount())<=0)
            {
                transactionlogic.SetLineDiscount(salesItem,discount);
            salesItem.setUnitPriceChanged(true);

                discount=BigDecimal.ZERO;
                break;
            }
            else
            {
                discount=discount.subtract(salesItem.getGrossAmount());
                if(discount.compareTo(BigDecimal.ZERO)<0)
                    discount=BigDecimal.ZERO;
                 
                transactionlogic.SetLineDiscount(salesItem,salesItem.getGrossAmount());
            salesItem.setUnitPriceChanged(true);

                // salesItem.setUnitPriceChanged(true);
            }
        }
        return discount;
    }

    public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt)
    {
        targetReceipt.getSalesItems().stream().filter(a->a.getMaterial()!=null && !a.getStatus().equals("3") ).forEach(salesItem->{
            BigDecimal descountAmount=salesItem.getDiscountAmount();
             var refSalesItem=salesItem.getReferenceSalesItem();
             var refUnitPrice=refSalesItem.getUnitGrossAmount();
             var qty=salesItem.getQuantity();
             BigDecimal amount=refUnitPrice.multiply(qty).subtract(descountAmount).divide(qty);
             var unitGrossAmount=amount;
             var unitNetAmount=amount;
             salesItem.setReferenceSalesItem(null);
             salesItem.setUnitNetAmount(unitNetAmount);
             salesItem.setUnitGrossAmount(unitGrossAmount);
             transactionlogic.SetLineDiscount(salesItem,BigDecimal.ZERO);
              salesItem.setUnitPriceChanged(true);
        });

        // sourcereceipt.getSalesItems().stream().forEach(a->{
        //     transactionlogic.resetDiscountToSalesItem(a);
            
        // });
        // receiptManager.update(sourcereceipt);


    }
}
