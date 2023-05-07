package com.trc.ccopromo;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import org.slf4j.LoggerFactory;

import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.trc.ccopromo.services.ReturnService;

public class ReturnController {
    private ReturnService  trcReturnService;
    private CDBSession dbSession; 
    private org.slf4j.Logger logger;
    public ReturnController(TrcPromoAddon addon,CDBSession dbSession,ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,Boolean isStartReturn)
    {
        logger = LoggerFactory.getLogger(ReturnController.class);
        this.dbSession=dbSession;
        this.trcReturnService=new ReturnService(addon,dbSession,sourcereceipt,targetReceipt,isStartReturn);

    }

    public void startReturn(ReturnReceiptObject returnReceipt) throws IOException, InterruptedException, URISyntaxException{
        this.trcReturnService.ItemForReturn(returnReceipt, true);
    }
    public void moveSalesItemByQuantity(ReturnReceiptObject returnReceipt) throws IOException, InterruptedException, URISyntaxException{
        this.trcReturnService.ItemForReturn(returnReceipt, false);
    }
    public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,boolean isWholeReceipt,BigDecimal totalReminingAmount)
    {
        this.trcReturnService.moveReturnedReceiptToCurrentReceipt(sourcereceipt, targetReceipt, isWholeReceipt, totalReminingAmount);
    }

}
