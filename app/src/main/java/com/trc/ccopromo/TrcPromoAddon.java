/*
* This Java source file was generated by the Gradle 'init' task.
 */
package com.trc.ccopromo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.BreakExecutionException;
import com.sap.scco.ap.pos.dao.ReceiptManager;

import org.eclipse.persistence.indirection.IndirectList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.scco.ap.plugin.annotation.ListenToExit;
import com.sap.scco.ap.plugin.annotation.PluginAt;
import com.sap.scco.ap.plugin.annotation.PluginAt.POSITION;
import com.sap.scco.ap.plugin.annotation.ui.JSInject;
// import com.sap.scco.ap.plugin.annotation.ui.JSInject;
import com.sap.scco.ap.pos.service.*;
import com.sap.scco.ap.pos.service.ReceiptChangeNotifierPosService;
import com.sap.scco.ap.pos.service.PosService;
import com.sap.scco.ap.pos.service.ReturnSalesItemPosService;
import com.sap.scco.ap.pos.service.impl.SalesItemPosServiceImpl;
import com.sap.scco.ap.pos.util.ui.BroadcasterHolder;
import com.sap.scco.ap.pos.service.impl.ReturnSalesItemPosServiceImpl;
import com.trc.ccopromo.models.*;

import ch.qos.logback.core.joran.conditional.ElseAction;

import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
import com.sap.scco.ap.pos.exception.RequestProcessingException;
import com.sap.scco.ap.pos.entity.MaterialEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;
// import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import com.google.gson.Gson; 
// import com.google.gson.GsonBuilder;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.cs.utilities.ReceiptHelper;
import com.sap.scco.ap.pos.dao.IReceiptManager;
import com.sap.scco.ap.pos.dao.*;
import com.sap.scco.ap.registry.UserRegistry;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
import com.sap.scco.util.exception.ValidationException;
import com.sap.scco.util.types.LogGDT;
import com.sap.scco.ap.pos.entity.BaseEntity;

public class TrcPromoAddon extends BasePlugin {
    // Data types
    public static final String STRING = "string";
    public static final String PASSWORD = "password";
    public static final String TEXT = "text";
    public static final String INT = "int";
    public static final String BOOLEAN = "boolean";

    public static final String SPECIAL_DISCOUNT_CALC_SERVICE_URL = "SPECIAL_DISCOUNT_CALC_SERVICE_URL";
    public static final String SPECIAL_DISCOUNT_SERVICE_USERNAME = "SPECIAL_DISCOUNT_SERVICE_USERNAME";
    public static final String SPECIAL_DISCOUNT_SERVICE_PASSWORD = "SPECIAL_DISCOUNT_SERVICE_PASSWORD";
    public static final String TRC_AUTOMATIC_DISCOUNTS_APPLIED = "TRC_AUTOMATIC_DISCOUNTS_APPLIED";
    public static final String TRC_DISCOUNT_MANUALLY_CHANGED = "TRC_DISCOUNT_MANUALLY_CHANGED";
    public static final String TRC_DISCOUNT_ID = "TRC_DISCOUNT_ID";
    public static final String TRC_DISCOUNT_NAME = "TRC_DISCOUNT_NAME";

    private static Logger logger = LoggerFactory.getLogger(TrcPromoAddon.class);
    private static AtomicBoolean currentlyCalculating = new AtomicBoolean(false);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private CDBSession dbSession;
    // private PriceDiscountManager priceDiscountManager;
    // private MaterialPosService materialPosService;
    private PosService posService;
    private ReceiptPosService receiptPosService;
    private ReceiptManager receiptManager;
    // private SalesItemManager salesItemManager;
    private CalculationPosService calculationPosService;
    private SalesItemPosService salesItemPosService;
    private EntityFactory entiryFactiry;
    static private  Boolean ignoreCalculation=false;

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
        return "2.0.0";
    }

    @Override
    public void startup() {
        currentlyCalculating.set(false);
        this.dbSession = CDBSessionFactory.instance.createSession();
        this.receiptManager = new ReceiptManager(dbSession);
        this.receiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, dbSession);
        this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
        this.salesItemPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemPosService.class,dbSession);
        super.startup();
    }
    
    @Override
    public boolean persistPropertiesToDB() {
        return true;
    }

    public PluginConfig getPluginConfig() {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setBaseUrl(getProperty(SPECIAL_DISCOUNT_CALC_SERVICE_URL, "")); // "http://192.168.0.17:5261/api/Promo/Calculate"
        pluginConfig.setUser(getProperty(SPECIAL_DISCOUNT_SERVICE_USERNAME, null));
        pluginConfig.setPassword(getProperty(SPECIAL_DISCOUNT_SERVICE_PASSWORD, null));
        return pluginConfig;
    }

    @Override
    public Map<String, String> getPluginPropertyConfig() {
        Map<String, String> propConfig = new HashMap<>();
        propConfig.put(SPECIAL_DISCOUNT_CALC_SERVICE_URL, TEXT);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_USERNAME, STRING);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_PASSWORD, PASSWORD);
        return propConfig;
    }
    //aggregateReceipt
    //validateReceipt
    public Boolean LockCalculation()
    {
        
        return (currentlyCalculating.compareAndSet(false, true));
    }
    public void unlockCalculation()
    {
        currentlyCalculating.set(false);
    }

    @PluginAt(pluginClass = CalculationPosService.class, method = "calculate", where = PluginAt.POSITION.BEFORE)
    public void applyPromotions(Object proxy, Object[] args, StackTraceElement caller) throws InconsistentReceiptStateException {
        // logger.info("CalculationPosService.calculate");
        try {
            
            if(LockCalculation()) {
                ReceiptEntity receipt = (ReceiptEntity) args[0];
                if (args.length == 4 &&
                        ReceiptEntity.Status.NEW.equals(receipt.getStatus()) && receipt.getPaymentItems().isEmpty()) {
                    // ReceiptHelper.markSalesItemsAsChanged(receipt);
                    // first recalculate to make sure its not strange stuff in here
                    // calculationPosService.recalculateReceipt(receipt);

                    // if(ignoreCalculation)
                    // {
                    //     ignoreCalculation=!ignoreCalculation;
                    //     unlockCalculation();
                    //     return;
                    // }
                                // calculate(receipt, false);
                   logger.info("---------------- Calculation -------------------");
                       TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
                    transactionLogic.CalculatePromotios(receipt);
                

                    // ReceiptHelper.markSalesItemsAsChanged(receipt);
                } else if(args.length == 4 && !ReceiptEntity.Status.NEW.equals(receipt.getStatus())){
                    logger.info("This is not a new receipt, not calculating discounts");
                }
                unlockCalculation();
                // currentlyCalculating.set(false);
                // logger.info("CalculationPosService.calculate - end");
            } else {
                // logger.info("Calculation run missed");
            }
        } catch (Exception e) {
            unlockCalculation();
            logger.error("ERROR CALCULATING", e);
            // currentlyCalculating.set(false);
        }
    }


    // @PluginAt(pluginClass = CalculationPosService.class, method = "calculate", where = PluginAt.POSITION.BEFORE)
    // public void applyPromotions(Object proxy, Object[] args, StackTraceElement caller) throws InconsistentReceiptStateException {
    //     // logger.info("CalculationPosService.calculate");
    //     try {
    //         // if (currentlyCalculating.compareAndSet(false, true)) {
    //         if(LockCalculation()) {
    //             ReceiptEntity receipt = (ReceiptEntity) args[0];
    //             if (args.length == 4 &&
    //                     ReceiptEntity.Status.NEW.equals(receipt.getStatus()) && receipt.getPaymentItems().isEmpty()) {
    //                 ReceiptHelper.markSalesItemsAsChanged(receipt);
    //                 // first recalculate to make sure its not strange stuff in here
    //                 calculationPosService.recalculateReceipt(receipt);
    //                // calculate(receipt, false);
    //                logger.info("---------------- Calculation -------------------");
    //                    TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
    //                 transactionLogic.CalculatePromotios(receipt);
                

    //                 ReceiptHelper.markSalesItemsAsChanged(receipt);
    //             } else if(args.length == 4 && !ReceiptEntity.Status.NEW.equals(receipt.getStatus())){
    //                 logger.info("This is not a new receipt, not calculating discounts");
    //             }
    //             unlocakCalculation();
    //             // currentlyCalculating.set(false);
    //             // logger.info("CalculationPosService.calculate - end");
    //         } else {
    //             // logger.info("Calculation run missed");
    //         }
    //     } catch (Exception e) {
    //         unlocakCalculation();
    //         logger.error("ERROR CALCULATING", e);
    //         // currentlyCalculating.set(false);
    //     }
    // }


    // @PluginAt(pluginClass = ReceiptPosService.class, method = "voidReceipt", where = PluginAt.POSITION.BEFORE)
    // public void voidReceipt(Object proxy, Object[] args, StackTraceElement caller) {
    //     logger.info("voidReceipt");
        
    //     // currentlyCalculating.set(false);

    // }

    // @PluginAt(pluginClass = ReceiptPosService.class, method = "aggregateReceipt", where = PluginAt.POSITION.AFTER)
    // public void aggregateReceipt(Object proxy, Object[] args, Object objItem, Object obj) {
    //     logger.info("aggregateReceipt");
        
    //     // currentlyCalculating.set(false);

    // }
    // @PluginAt(pluginClass = ReceiptPosService.class, method = "validateReceipt", where = PluginAt.POSITION.AFTER)
    // public void validateReceipt(Object proxy, Object[] args, Object objItem, Object obj) {
    //     logger.info("validateReceipt");
    //     ReceiptEntity receipt = (ReceiptEntity) args[0];
    //     receiptManager.update(receipt);
    //     // calculationPosService.update()
        
    //     // currentlyCalculating.set(false);

    // }


    // @PluginAt(pluginClass = ReceiptPosService.class, method = "addSalesItem", where = PluginAt.POSITION.AFTER)
    // public void addSalesItem(Object proxy, Object[] args, Object objItem, Object obj)
    //         throws IOException, InterruptedException {
    //     ReceiptEntity receipt = (ReceiptEntity) args[0];
    //     TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
    //     transactionLogic.CalculatePromotios(receipt);
    //     // sendMessageToUi("sasa","description");
    // }

    // @PluginAt(pluginClass = ReceiptPosService.class, method = "updateSalesItem", where = PluginAt.POSITION.AFTER)
    // public void updateSalesItem(Object proxy, Object[] args, StackTraceElement caller, Object obj)
    //         throws IOException, InterruptedException {
    //     ReceiptEntity receipt = (ReceiptEntity) args[0];
    //     TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
    //     transactionLogic.CalculatePromotios(receipt);
    // }
    
    // void SalesItem
    // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnSalesItemVoided", where = PluginAt.POSITION.AFTER)
    // public void notifyListenersOnSalesItemVoided(Object proxy, Object[] args, Object objItem, Object obj)
    //         throws IOException, InterruptedException {
    //             if(args.length>1)
    //                 if (((Object) args[1]).getClass().equals(SalesItemEntity.class)) 
    //             {
    //                 TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
    //                 transactionLogic.CalculatePromotios(((SalesItemEntity)args[1]).getReceipt());
    //             }
    // }
    

    
    

    
    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveSalesItemByQuantity", where = PluginAt.POSITION.AFTER)
    public Object moveSalesItemByQuantity(Object proxy, Object[] args, Object ret, StackTraceElement caller)
            throws BreakExecutionException, IOException, InterruptedException {
        ReturnReceiptObject result = (ReturnReceiptObject) ret;
        ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
        ReceiptEntity sourceReceipt = result.getSourceReceipt();
        if (targetReceipt != null) {
            TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
            transactionLogic.PickUpPromoTransaction(result);
        }
        return result;
    }

    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveReturnedReceiptToCurrentReceipt", where = PluginAt.POSITION.AFTER)
    public Object moveReturnedReceiptToCurrentReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller) throws BreakExecutionException {
        ReceiptEntity result = (ReceiptEntity) ret;
        if (args.length == 8) {
            ReceiptEntity receiptWithAdjustmentItems = (ReceiptEntity) args[1];
            // if(LockCalculation())
            {
                TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
                transactionLogic.copyAdjustmentItems(receiptWithAdjustmentItems, result);
                // unlockCalculation();
                // ignoreCalculation=true;
            }
        }
        return result;
    }

    // @PluginAt(pluginClass=IReceiptManager.class, method="voidSalesItem", where=POSITION.AFTER)
	// public void pluginAtAfterExample(Object proxy, Object[] args, Object returnValue, StackTraceElement callStack) {
	// 	//Your code
    //     logger.info("ok");
	// }

    @JSInject(targetScreen="sales")
	public String injectJSToSalesScreenExample(){
		return "alert(\"Hello World from Plugin!\");";
	}
    @PluginAt(pluginClass=IReceiptManager.class, method="finishReceipt", where=POSITION.BEFORE)
	public void pluginAtBeforeExample(Object proxy, Object[] args, StackTraceElement callStack) throws BreakExecutionException {
		//Your code
        logger.info("ok");
	}
    
    private void sendMessageToUi(String titleKey, String titleMessage) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", titleKey);
        jsonObject.put("message", titleMessage);
        BroadcasterHolder.INSTANCE.getBroadcaster().broadcastPluginEventForPath("SHOW_MESSAGE_BOX", jsonObject);    
    }

	
	@PluginAt(pluginClass=IReceiptManager.class, method="finishReceipt", where=POSITION.AFTER)
	public void pluginAtAfterExample(Object proxy, Object[] args, Object returnValue, StackTraceElement callStack) {
		//Your code
        logger.info("ok");
	}


}
