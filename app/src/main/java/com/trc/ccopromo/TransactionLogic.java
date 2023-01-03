package com.trc.ccopromo;

import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.dao.PersistenceManager;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
import com.sap.scco.util.security.InsufficientPermissionException;
import com.trc.ccopromo.models.PromoResponse;

import org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionLogic {
    private TrcPromoAddon _addon;
    private ReceiptEntity transaction = null;
    private ReceiptManager receiptManager;
    private CalculationPosService calculationPosService;
    private CDBSession dbSession;

    private org.slf4j.Logger logger;

    public TransactionLogic(TrcPromoAddon addon, ReceiptManager _receiptManager,
            CalculationPosService _calculationPosService, CDBSession _dbSession) {
                
        logger = LoggerFactory.getLogger(TrcPromoAddon.class);
        receiptManager = _receiptManager;
        _addon = addon;
        calculationPosService = _calculationPosService;
        dbSession = _dbSession;
    }

    public void setAdditionalField(SalesItemEntity salesItem, String key, String value) {
        AdditionalFieldEntity additionalField2 = salesItem.getAdditionalField(key);
        if (additionalField2 == null) {
            if (value == null)
                return;
            additionalField2 = new AdditionalFieldEntity();
            salesItem.addAdditionalField(additionalField2);
        }
        additionalField2.setFieldName(key);
        additionalField2.setGroupName(com.trc.ccopromo.models.Constants.PROMO_GROUP);
        additionalField2.setValue(value);
    }

    ReceiptEntity LoadReceipt(String id)
    {
        CDBSession localSession = CDBSessionFactory.instance.createSession();
        ReceiptPosService localReceiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, localSession);
        return localReceiptPosService.getReceipt(id);

    }
    void ResetReceiptsSales(ReceiptEntity reciept)
    {
        reciept.getSalesItems().forEach(salesItem->
        {
            salesItem.setPercentageDiscount(false);
            salesItem.setDiscountAmount(BigDecimal.ZERO);
            salesItem.setDiscountPurposeCode("1000");
            salesItem.setMarkChanged(true);
            salesItem.setItemDiscountChanged(true);
            salesItem.setDiscountManuallyChanged(true);
            salesItem.setUnitPriceChanged(true);
        });
        calculationPosService.calculate(reciept, EntityActions.CHECK_CONS);
            // calculationPosService.recalculateReceipt(receipt);

        // receiptManager.update(reciept);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, reciept);

    }
    void addAdjustmentItem(ReceiptEntity targetReceipt,BigDecimal amount)
    {
        var _adjustmentItem=targetReceipt.getSalesItems().stream().filter(a->a.getId().equals("PROMO_ADJUSTMENT")).findFirst();

            if(_adjustmentItem!=null)
                if(!_adjustmentItem.isEmpty())
            {
                SalesItemEntity adjustmentItem=_adjustmentItem.get();
                // var qty=targetReceipt.getSalesItems().get(0).getQuantity();
                // var finalPrice=adjustmentItem.getGrossAmount();
                
                // finalPrice=finalPrice.add(finalPrice);
                // finalPrice=finalPrice.add(BigDecimal.ONE);
                // finalPrice+=BigDecimal.valueOf(BigDecimal.valueOf(finalPrice.doubleValue()+1) );
                // finalPrice=
                adjustmentItem.setUnitNetAmount(amount);
                adjustmentItem.setUnitNetAmountOrigin(amount);
                adjustmentItem.setUnitGrossAmount(amount);
                adjustmentItem.setUnitGrossAmountOrigin(amount);
                // EntityActions.CHECK_CONS
                calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);

                return;
                // adjustmentItem.setQuantity(BigDecimal.valueOf( adjustmentItem.getQuantity().doubleValue()+qty.doubleValue()));
            }

        SalesItemEntity adjustmentItem=com.sap.scco.ap.pos.dao.EntityFactory.INSTANCE.createSpecialSalesItemEntity("Promo Adjustment", BigDecimal.valueOf(10), 
            targetReceipt.getSalesItems().get(0).getTaxRateTypeCode(), null);
            // adjustmentItem.setGrossAmount(null);
            // BigDecimal finalPrice = BigDecimal.valueOf(-5);
            // var finalPrice=BigDecimal.valueOf(targetReceipt.getSalesItems().stream().filter(a->!a.getId().equals("PROMO_ADJUSTMENT")).
            //     mapToDouble(a->a.getGrossAmount().doubleValue()).sum());
            
            // var finalPrice=BigDecimal.valueOf(targetReceipt.getSalesItems().stream().mapToDouble(a->a.getGrossAmount().doubleValue()).sum());
            // price != null ? price.negate() : BigDecimal.ZERO;
                adjustmentItem.setUnitNetAmount(amount);
                adjustmentItem.setUnitNetAmountOrigin(amount);
                adjustmentItem.setUnitGrossAmount(amount);
                adjustmentItem.setUnitGrossAmountOrigin(amount);
                adjustmentItem.setUnitPriceChanged(true);
                adjustmentItem.setId("PROMO_ADJUSTMENT");
                adjustmentItem.setQuantity(BigDecimal.ONE);
                targetReceipt.addSalesItem(adjustmentItem);
                calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
                UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);


    }
    public void PickUpPromoTransaction(ReturnReceiptObject returnReciept) throws IOException, InterruptedException
    {
        
        ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
        ReceiptEntity actualOriginalReceipt = LoadReceipt(returnReciept.getSourceReceipt().getId());
        var refPromos=new ArrayList<Integer>();
        actualOriginalReceipt.getSalesItems().stream().forEach(a->
        {
            var addField=a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
            if(addField!=null)
                refPromos.add(Integer.valueOf(addField.getValue()));
        });



        
        //has any promo
        if(!refPromos.isEmpty())
        {
            var promos = RequestPromo(sourceReceipt,refPromos);
            
            
            var actualOrigAmount=actualOriginalReceipt.getPaymentGrossAmount();

            


            var newDiscount=BigDecimal.valueOf(Double.parseDouble(promos.discount));
            var sourceAmount=sourceReceipt.getTotalGrossAmount();
            var existingadjItem=targetReceipt.getSalesItems().stream().filter(a->a.getId().equals("PROMO_ADJUSTMENT")).findFirst();
            if(!existingadjItem.isEmpty())
                {
                    
                    var adjustmentItem=existingadjItem.get();
                    adjustmentItem.setUnitNetAmount(BigDecimal.ZERO);
                    adjustmentItem.setUnitNetAmountOrigin(BigDecimal.ZERO);
                    adjustmentItem.setUnitGrossAmount(BigDecimal.ZERO);
                    adjustmentItem.setUnitGrossAmountOrigin(BigDecimal.ZERO);

                    calculationPosService.calculate(targetReceipt, EntityActions.CHECK_CONS);
                    
                }

            var targetAmount=targetReceipt.getTotalGrossAmount();
            

            var promoAdjuction=actualOrigAmount
                .subtract(sourceAmount.
                    subtract(newDiscount)).
                        subtract(targetAmount);
            logger.info(promoAdjuction.toString());

            ResetReceiptsSales(sourceReceipt);
            ResetReceiptsSales(targetReceipt);
            addAdjustmentItem(targetReceipt,promoAdjuction);

            // actualOrigAmount sourceAmount.add(targetAmount).subtract(newDiscount)
        
            // var newamount=(sourceAmount-newDiscount+targetAmount);

            // actualOrigAmount-

            
        
            
            logger.info(promos.discount.toString());
            



            // sourceReceipt.getSalesItems().stream().
            // _promos.forEach(a->
            // {
            //     var salesItem=sourceReceipt.getSalesItems().stream().filter(b-> 
            //         b.getExternalId().equalsIgnoreCase(a.externalId)).findFirst();
            //     //salesItem.addAdditionalField()
            // }
            // );
            // sourceReceipt
            // _promos.filter(a->a.externalId)
        }

/* 
        var salesItem=sourceReceipt.getSalesItems().get(0);
        salesItem.setPercentageDiscount(false);
        salesItem.setDiscountAmount(BigDecimal.ZERO);
        salesItem.setDiscountPurposeCode("1000");
        salesItem.setMarkChanged(true);
        salesItem.setItemDiscountChanged(true);
        salesItem.setDiscountManuallyChanged(true);
        salesItem.setUnitPriceChanged(true);


        calculationPosService.calculate(sourceReceipt, EntityActions.UPDATE);
            // calculationPosService.recalculateReceipt(receipt);

        receiptManager.update(sourceReceipt);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, sourceReceipt);



        salesItem=targetReceipt.getSalesItems().get(0);
        salesItem.setPercentageDiscount(false);
        salesItem.setDiscountAmount(BigDecimal.ZERO);
        salesItem.setDiscountPurposeCode("1000");
        salesItem.setMarkChanged(true);
        salesItem.setItemDiscountChanged(true);
        salesItem.setDiscountManuallyChanged(true);
        salesItem.setUnitPriceChanged(true);


        calculationPosService.calculate(targetReceipt, EntityActions.UPDATE);
            // calculationPosService.recalculateReceipt(receipt);

        receiptManager.update(targetReceipt);
        UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, targetReceipt);

*/
        

        // actualOriginalReceipt.getSalesItems()
        // // actualOriginalReceipt.getSalesItems()
        // .forEach(entry->{
        //     var extrafields=entry.getAdditionalFields();
        // });

    }
    public void CalculatePromotios(final ReceiptEntity receipt) throws IOException, InterruptedException {
        logger.info("CalculatePromotios");
        // new Thread(new Runnable() {

        // @Override
        // public void run() {

        Boolean hasdiscountChanges = Boolean.FALSE;
        try {
            var promos = RequestPromo(receipt,null);
            for (var salesItem : receipt.getSalesItems()) {

                var promoitem = promos.itemDiscounts.stream().filter(a -> a.itemCode.equals(salesItem.getId()))
                        .findFirst();

                if (promoitem != null)
                    if (!promoitem.isEmpty()) {
                        hasdiscountChanges = Boolean.TRUE;
                        salesItem.setPercentageDiscount(false);
                        salesItem.setDiscountAmount(BigDecimal.valueOf(promoitem.get().discount));
                        // salesItem.setDiscountPercentage(discountPercentage.multiply(new
                        // BigDecimal(100)));
                        salesItem.setDiscountPurposeCode("1000");
                        salesItem.setMarkChanged(true);
                        salesItem.setItemDiscountChanged(true);
                        salesItem.setDiscountManuallyChanged(true);

                        setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.PROMO_ID,
                                Integer.toString(promoitem.get().promoId));
                        setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.PROMO_NAME,
                                promoitem.get().promoName);

                        continue;
                    }

                AdditionalFieldEntity promoid = salesItem
                        .getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
                if (promoid != null) {
                    resetDiscountToSalesItem(salesItem);
                    setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.PROMO_ID,null);
                        setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.PROMO_NAME,null);
                    hasdiscountChanges = Boolean.TRUE;
                }
            }
            // }

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.info("-------Thread-------");
        if (hasdiscountChanges) {
            calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
            // calculationPosService.recalculateReceipt(receipt);

            receiptManager.update(receipt);
            UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);

        }
        // }
        // }).start();
    }

    public void resetDiscountToSalesItem(SalesItemEntity salesItem) {

        if (salesItem.getDiscountAmount().compareTo(BigDecimal.ZERO) != 0) {
            salesItem.setPercentageDiscount(false);
            salesItem.setDiscountAmount(BigDecimal.ZERO);
            salesItem.setDiscountPurposeCode("1000");
            salesItem.setMarkChanged(true);
            salesItem.setItemDiscountChanged(true);
            salesItem.setDiscountManuallyChanged(true);
            salesItem.setUnitPriceChanged(true);
        }
    }

    public PromoResponse RequestPromo(ReceiptEntity _transaction,ArrayList<Integer> refPromos) throws IOException, InterruptedException {

        ReceiptEntity transaction = _transaction;
        logger.info("------RequestPromo------");
        var request = new WebRequest(_addon.getPluginConfig());
        return request.Request(transaction,refPromos);
    }

}
