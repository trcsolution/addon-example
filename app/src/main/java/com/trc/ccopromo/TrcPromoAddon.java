package com.trc.ccopromo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.BreakExecutionException;
import com.sap.scco.ap.plugin.annotation.ListenToExit;
import com.sap.scco.ap.plugin.annotation.PluginAt;
import com.sap.scco.ap.plugin.annotation.Schedulable;
import com.sap.scco.ap.plugin.annotation.ui.JSInject;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.dao.ReceiptPosDAO;
import com.sap.scco.ap.pos.dao.impl.ReceiptPosDAOImpl;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.SalesItemNoteEntity;
import com.sap.scco.ap.pos.service.SalesItemNotePosService;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.entity.coupon.DiscountElementEntity;
import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
import com.sap.scco.ap.pos.i14y.util.context.I14YContext;
import com.sap.scco.ap.pos.job.PluginJob;
import com.sap.scco.ap.pos.entity.BusinessPartnerEntity;
import com.sap.scco.ap.pos.entity.MonitoringEntry;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptChangeNotifierPosService;
import com.sap.scco.ap.pos.service.AccountCouponPosService;
import com.sap.scco.ap.pos.service.impl.BusinessPartnerPosServiceImpl;
import com.sap.scco.ap.pos.service.impl.MonitoringPosServiceImpl;
import com.sap.scco.ap.pos.util.TriggerParameter;
import com.sap.scco.ap.pos.util.ui.BroadcasterHolder;
import com.sap.scco.ap.pos.util.ui.UIEventChannelListener;
import com.sap.scco.ap.pos.util.ui.UIMessageBroadcaster;
import com.sap.scco.ap.registry.UserRegistry;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.ReturnReceiptPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.pos.service.component.listener.ReceiptChangeListener;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
import com.sap.scco.util.exception.ValidationException;
import com.trc.ccopromo.models.session.DiscountSource;
// import com.trc.ccopromo.models.session.TransactionState;
import com.trc.ccopromo.services.Misc;
import com.trc.ccopromo.services.SalesService;

import net.sf.json.JSONObject;

// import com.trc.ccopromo.TransactionTools;

///NOTE!!!!!!!!
import com.sap.scco.ap.pos.service.SalesItemNotePosService;
import com.sap.scco.ap.pos.service.SalesItemPosService;

import com.sap.scco.ap.pos.service.SalesItemNotePosService;
import com.sap.scco.ap.pos.service.SalesItemPosService;

public class TrcPromoAddon extends BasePlugin implements ReceiptChangeListener {
    
    public static final String STRING = "string";
    public static final String PASSWORD = "password";
    public static final String TEXT = "text";
    public static final String INT = "int";
    public static final String BOOLEAN = "boolean";

    public static final String SPECIAL_DISCOUNT_CALC_SERVICE_URL = "SPECIAL_DISCOUNT_CALC_SERVICE_URL";
    public static final String SPECIAL_DISCOUNT_SERVICE_APIKEY = "SPECIAL_DISCOUNT_SERVICE_APIKEY";
    public static final String SPECIAL_DISCOUNT_SERVICE_SECURE = "SPECIAL_DISCOUNT_SERVICE_SECURE";
    public static final String SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN = "SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN";
    private static Logger logger = LoggerFactory.getLogger(TrcPromoAddon.class);
    private static AtomicBoolean currentlyCalculating = new AtomicBoolean(false);
    private CDBSession dbSession;
    private ReceiptManager receiptManager;
    private ReceiptPosService receiptPosService;
    private ReceiptChangeNotifierPosService notifierService;
    private CalculationPosService calculationPosService;
    private SalesItemPosService salesItemPosService;
    private SalesItemNotePosService salesItemNotePosService;
    // private TrcPromoController trcPromoController;
    

    // public  TransactionState transactionState;


    @Override
    public String getId() {
        return "TRCPromo";
    }

    @Override
    public String getName() {
        return "TRC Promo";
    }

    @Override
    public String getVersion() {
        return "2.4.3";
    } 
    @Override
    public boolean persistPropertiesToDB() {
        return true;
    }
    public PluginConfig getPluginConfig() {
        PluginConfig pluginConfig = new PluginConfig();

        pluginConfig.setBaseUrl(getProperty(SPECIAL_DISCOUNT_CALC_SERVICE_URL, "")); // "http://192.168.0.17:5261/api/Promo/Calculate"
        pluginConfig.setAPIKey(getProperty(SPECIAL_DISCOUNT_SERVICE_APIKEY, null));
        pluginConfig.setSecure(getProperty(SPECIAL_DISCOUNT_SERVICE_SECURE, false));
        pluginConfig.setAdvreturn(getProperty(SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN, false));
        return pluginConfig;
    }
    public Boolean LockCalculation() {
        return (currentlyCalculating.compareAndSet(false, true));
    }

    public void unlockCalculation() {
        currentlyCalculating.set(false);
    }

    @Override
    public Map<String, String> getPluginPropertyConfig() {
        Map<String, String> propConfig = new HashMap<>();
        propConfig.put(SPECIAL_DISCOUNT_CALC_SERVICE_URL, TEXT);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_APIKEY, STRING);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_SECURE, BOOLEAN);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN, BOOLEAN);
        return propConfig;
    }
    @Override
    public void startup() {
        this.dbSession = CDBSessionFactory.instance.createSession();
        this.receiptManager = new ReceiptManager(dbSession);
        this.receiptPosService =ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class,dbSession);
        this.notifierService =ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptChangeNotifierPosService.class,dbSession);
        this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
        this.salesItemPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemPosService.class,dbSession);
        this.salesItemNotePosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemNotePosService.class,dbSession);


        // transactionState=new TransactionState();
        // trcPromoController=new TrcPromoController();

        currentlyCalculating.set(false);
        notifierService.registerChangeListener(this);
        BroadcasterHolder.INSTANCE.addEventChannelListener(new UIEventChannelListener() {
            @Override
            public void handleEvent(String eventId, JSONObject payload) {
                try {
                    switch (eventId) {
                        case "TRC_APPLY_PROMOTIONS":
                                // pushEvent("TRC_CALCULATE_PROMO",null);
                                // CalculateCurrent();
                            break;
                        // case "TRC_CALCULATE_PROMO":
                        //         CalculateCurrent();
                        //     // showMessageToNewUI("ssssssss");
                        //     break;
                    }
                } catch (Exception e) {
                    if (StringUtils.isNotEmpty(e.getLocalizedMessage())) {
                        Misc.showMessageToNewUI(e.getLocalizedMessage());
                    } else if (StringUtils.isNotEmpty(e.getMessage())) {
                        Misc.showMessageToNewUI(e.getMessage());
                    } else {
                        Misc.showMessageToNewUI("Error: " + e.getClass().getSimpleName());
                    }
                    logger.info("Exception during coupon adding", e);
                }
            }
        });
        
        super.startup();
    }
    
    
    
    public void onReceiptPost(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {

        new SalesController(this,dbSession).postReceipt(receipt);
    }
    public void onReceiptCanceled(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
        // transactionState.Reset();
    }
    public void onVoidReceiptCompleted  (com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
        // transactionState.Reset();
    }
    public void onDiscountChange(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
    }


    
    public  void onSalesItemAddedToReceipt(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, java.util.List<com.sap.scco.ap.pos.entity.SalesItemEntity> salesItems, java.math.BigDecimal quantity) {
        new SalesController(this,dbSession).onSalesItemAddedToReceipt(receipt, salesItems, quantity);
    }
    
    public void onSalesItemVoided(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem) {
        new SalesController(this,dbSession).onSalesItemVoided(receipt, salesItem);
    }
    
    public void onSalesItemUpdated(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity newSalesItem, java.math.BigDecimal quantity) {
        new SalesController(this,dbSession).onSalesItemUpdated(receipt, newSalesItem);

    }
    public void onCouponRemoved(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) 
    {
        new SalesController(this,dbSession).onCouponRemoved(receipt);
    }
    public void onCouponAdded(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {

        new SalesController(this,dbSession).onCouponAdded(receipt);
        

        
    }

    

    // void CalculateCurrent() throws InconsistentReceiptStateException{
    //     receiptPosService.getDbSession().getEM().clear();
    //     ReceiptPosService receiptService = this.receiptPosService;
    //     ReceiptEntity receipt = receiptService.findOrCreate(UserRegistry.INSTANCE.getCurrentUser(), null, false);
    //     // ReceiptEntity receipt = receiptService.findOrCreate(UserRegistry.INSTANCE.getCurrentUser(), null, false);
    //     if (ReceiptEntity.Status.NEW.equals(receipt.getStatus())) {
    //         // Calculate(receipt);
    //         receiptManager.update(receipt);
    //         calculationPosService.recalculateReceipt(receipt);
    //         // LogGDT log = receiptManager.update(receipt);
    //         receiptPosService.getDbSession().getEM().clear();
    //         receiptManager.manualFlush();

    //         Misc.pushEvent("RECEIPT_REFRESH", null);
    //     }
    // }
    

    @JSInject(targetScreen = "NGUI")
    public InputStream[] jsInject() {
        return new InputStream[]{this.getClass().getResourceAsStream("/TrcPromoAddon.js")};
    }

    

    @PluginAt(pluginClass = SalesItemNotePosService.class, method = "removeSalesItemNote", where = PluginAt.POSITION.AFTER)
    public Object removeSalesItemNote(Object proxy, Object[] args, Object ret, StackTraceElement caller)
    {
        ReceiptEntity receipt=(ReceiptEntity)args[0];
        var salesItem=receipt.getSalesItems().stream().filter(a->a.getKey().equals(args[2])).findFirst().get();
        new SalesController(this,((SalesItemNotePosService)proxy).getDbSession())
            .removeSalesItemNote(receipt,salesItem);
        return null;
    }

    @PluginAt(pluginClass = ReceiptPosService.class, method = "setBusinessPartner", where = PluginAt.POSITION.AFTER)
    public Object setBusinessPartner(Object proxy, Object[] args, Object ret, StackTraceElement caller)
    {
        new SalesController(this,((ReceiptPosService)proxy).getDbSession()).setBusinessPartner((ReceiptEntity)args[0],(BusinessPartnerEntity)args[1]);
        return null;
    }

    

    
    @PluginAt(pluginClass = ReceiptPosService.class, method = "updateSalesItem", where = PluginAt.POSITION.BEFORE)
    public void updateSalesItemBefore(Object proxy, Object[] args, Object ret)
    {
        if(args.length>3)
        {
            SalesController controller=new SalesController(this,((ReceiptPosService)proxy).getDbSession());
            HashSet hashset=(HashSet)args[3];
            if(hashset.contains("discountAmount") || hashset.contains("discountPercentage"))
                controller.onManuallyUpdateItemDiscount((ReceiptEntity)args[0],(SalesItemEntity)args[1]);
        }
    }



    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "startReturn", where = PluginAt.POSITION.AFTER)
    public Object startReturn(Object proxy, Object[] args, Object ret, StackTraceElement caller)
            throws BreakExecutionException, IOException, InterruptedException, URISyntaxException

    {
        ReturnReceiptObject result = (ReturnReceiptObject) ret;
        ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = result.getSourceReceipt();

        ReturnController controller=new ReturnController(this,((ReturnReceiptPosService)proxy).getDbSession(),sourceReceipt,targetReceipt,true);
        controller.startReturn(result);
        return result;
    }

    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveSalesItemByQuantity", where = PluginAt.POSITION.AFTER)
    public Object moveSalesItemByQuantity(Object proxy, Object[] args, Object ret, StackTraceElement caller)
            throws BreakExecutionException, IOException, InterruptedException, URISyntaxException {
        ReturnReceiptObject returnReceipts = (ReturnReceiptObject) ret;
        if (this.getPluginConfig().getAdvreturn()) {
            ReceiptEntity targetReceipt = returnReceipts.getIndividualItemsReceipt();
            ReceiptEntity sourceReceipt = returnReceipts.getSourceReceipt();
            ReturnController controller=new ReturnController(this,((ReturnReceiptPosService)proxy).getDbSession(),sourceReceipt,targetReceipt,false);
                controller.moveSalesItemByQuantity(returnReceipts);

        }
        return returnReceipts;
    }

        boolean returnWholeReceipt=false;
        BigDecimal totalReminingAmount=BigDecimal.ZERO;
        @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "returnWholeReceipt", where = PluginAt.POSITION.BEFORE)
        public Object returnWholeReceiptBefore(Object proxy, Object[] args, Object ret) {
            // logger.info("-1-1-1-1-1-1-11");
            returnWholeReceipt=true;
            return null;
        }



    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveReturnedReceiptToCurrentReceipt", where = PluginAt.POSITION.AFTER)
    public Object moveReturnedReceiptToCurrentReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller)
            throws BreakExecutionException {
        // logger.info("22222222");
        ReceiptEntity targetReceipt = (ReceiptEntity) ret;
        if (args.length == 8)
            // if (this.getPluginConfig().getAdvreturn()) 
            {
                ReceiptEntity sourceReceipt = (ReceiptEntity) args[1];
                ReturnController controller=new ReturnController(this,((ReturnReceiptPosService)proxy).getDbSession(),sourceReceipt,targetReceipt,false);

                // targetReceipt = returnReciept.getIndividualItemsReceipt();
                // sourceReceipt = returnReciept.getSourceReceipt();

                controller.moveReturnedReceiptToCurrentReceipt(sourceReceipt, targetReceipt,returnWholeReceipt,totalReminingAmount);
            }
        returnWholeReceipt=false;
        return targetReceipt;
    }

    // @PluginAt(pluginClass = AccountCouponPosService.class, method = "addCoupon", where = PluginAt.POSITION.BEFORE)
    //     public Object addCouponBefore(Object proxy, Object[] args, Object ret) {
    //         // logger.info("-1-1-1-1-1-1-11");
            
    //         return null;
    //     }


    // @PluginAt(pluginClass = AccountCouponPosService.class, method = "addCoupon", where = PluginAt.POSITION.AFTER)
    // public Object AddCoupon(Object proxy, Object[] args, Object ret, StackTraceElement caller)
    // {
    //     return null;
    // }

    

    
}
