package com.trc.ccopromo;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.LoggerFactory;

import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.entity.BusinessPartnerEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.util.CConst;
import com.trc.ccopromo.services.SalesService;
import com.trc.ccopromo.services.Misc;
import com.trc.ccopromo.services.ReturnService;
import java.math.BigDecimal;

public class SalesController {

    private  SalesService trcPromoService;
    
    //private  Ret trcPromoService;
    private CDBSession dbSession; 
    private org.slf4j.Logger logger;

    public SalesController(TrcPromoAddon addon,CDBSession dbSession)
    {
        logger = LoggerFactory.getLogger(SalesController.class);
        this.dbSession=dbSession;
        this.trcPromoService=new SalesService(addon,dbSession);
        

    }
    
    public void setBusinessPartner(ReceiptEntity receipt,BusinessPartnerEntity businessPartner)
    {
        this.trcPromoService.setBusinessPartner(receipt,businessPartner);
        
    }
    public void onSalesItemAddedToReceipt(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, java.util.List<com.sap.scco.ap.pos.entity.SalesItemEntity> salesItems, java.math.BigDecimal quantity)
    {
        this.trcPromoService.onSalesItemAddedToReceipt(receipt,salesItems,quantity);
        
    }
    public void onSalesItemUpdated(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem)//, java.math.BigDecimal quantity 
    {

        if(IsDISCOUNT_SOURCEManual)
        {
            IsDISCOUNT_SOURCEManual=false;
            salesItem.setDiscountAmount(manualDIscountAmount);
            var item=receipt.getSalesItems().stream().filter(a->a.getKey()==salesItem.getKey()).findFirst().get();
            item.setDiscountAmount(manualDIscountAmount);
            trcPromoService.MarkItemAsManualDiscounted(salesItem, true);
            Misc.ClearPromo(salesItem,false);
            Misc.AddNote(salesItem, "Manually discounted");
            // this.trcPromoService.Calculate(receipt);

            // var calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
            // calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        }
        else
        {
            
            trcPromoService.MarkItemAsManualDiscounted(salesItem, false);

            this.trcPromoService.Calculate(receipt);
            // receipt.getAdditionalFields()
            // new ReceiptManager(dbSession).update(receipt);
            var calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
            calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        }
    }
    public void onSalesItemVoided(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem) {
        this.trcPromoService.Calculate(receipt);

    }
    static boolean IsDISCOUNT_SOURCEManual =false;
    static BigDecimal manualDIscountAmount;
    public void onManuallyUpdateItemDiscount(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem) {
        IsDISCOUNT_SOURCEManual=true;
        manualDIscountAmount=salesItem.getDiscountAmount();
    }

    public void removeSalesItemNote(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem)//, java.math.BigDecimal quantity
    {
        IsDISCOUNT_SOURCEManual=false;
        if(salesItem.getAdditionalField(com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE)!=null)
        {
            trcPromoService.MarkItemAsManualDiscounted(salesItem,false);
            this.trcPromoService.SetLineDiscount(salesItem,BigDecimal.ZERO);
            salesItem.setDiscountManuallyChanged(false);
            this.trcPromoService.Calculate(receipt);
            var calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,this.dbSession);
            calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        }
        
        else
        if(salesItem.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)!=null)
        {
            this.trcPromoService.Calculate(receipt);
            var PromoId=salesItem.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue();
            Misc.AddNote(salesItem,  "Promo:"+PromoId);
            var calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,this.dbSession);
            calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        }

    }
    public void onCouponRemoved(com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
       
        this.trcPromoService.onCouponRemoved(receipt);
        // Calculate(receipt);
        //         calculationPosService.calculate(reciept, EntityActions.CHECK_CONS);
        // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, reciept);
    }
    public void onCouponAdded(com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
       
        this.trcPromoService.onCouponAdded(receipt);
       //new ReceiptManager(dbSession).update(receipt);
    }

    public void postReceipt(ReceiptEntity receipt) {
        try {
            this.trcPromoService.postReceipt(receipt);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    


}
