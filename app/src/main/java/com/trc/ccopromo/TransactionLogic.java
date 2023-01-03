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

    public void PickUpPromoTransaction(ReturnReceiptObject returnReciept)
    {
        ReceiptEntity actualOriginalReceipt = null;
            try(CDBSession localSession = CDBSessionFactory.instance.createSession()) {
                ReceiptPosService localReceiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, localSession);
                actualOriginalReceipt = localReceiptPosService.getReceipt(returnReciept.getSourceReceipt().getId());
            }
            
        ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
        actualOriginalReceipt.getSalesItems()
        // actualOriginalReceipt.getSalesItems()
        .forEach(entry->{
            var extrafields=entry.getAdditionalFields();
        });

    }
    public void CalculatePromotios(final ReceiptEntity receipt) throws IOException, InterruptedException {

        logger.info("CalculatePromotios");
        // for (var salesItem : receipt.getSalesItems()) {
        //     setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.PROMO_ID,"sasa");
        // }
        // calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        //     // calculationPosService.recalculateReceipt(receipt);

        // receiptManager.update(receipt);
        // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);

        
        // return;
        // new Thread(new Runnable() {

        // @Override
        // public void run() {

        Boolean hasdiscountChanges = Boolean.FALSE;
        try {
            var promos = RequestPromo(receipt);
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

    public PromoResponse RequestPromo(ReceiptEntity _transaction) throws IOException, InterruptedException {

        ReceiptEntity transaction = _transaction;
        logger.info("------RequestPromo------");
        var request = new WebRequest(_addon.getPluginConfig());
        return request.Request(transaction);
    }

}
