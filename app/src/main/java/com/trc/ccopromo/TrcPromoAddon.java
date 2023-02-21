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
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.entity.coupon.DiscountElementEntity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
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
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.BaseEntity;

public class TrcPromoAddon extends BasePlugin {
    // Data types
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
    // private static final HttpClient httpClient = HttpClient.newBuilder()
    //         .version(HttpClient.Version.HTTP_1_1)
    //         .connectTimeout(Duration.ofSeconds(10))
    //         .build();
    private CDBSession dbSession;
    // private PriceDiscountManager priceDiscountManager;
    // private MaterialPosService materialPosService;
    // private PosService posService;
    // private ReceiptPosService receiptPosService;
    private ReceiptManager receiptManager;
    // private SalesItemManager salesItemManager;
    private CalculationPosService calculationPosService;
    // private SalesItemPosService salesItemPosService;
    // private EntityFactory entiryFactiry;
    // static private  Boolean ignoreCalculation=false;

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
        return "2.0.11";
    }

    @Override
    public void startup() {
        currentlyCalculating.set(false);
        this.dbSession = CDBSessionFactory.instance.createSession();
        this.receiptManager = new ReceiptManager(dbSession);
        // this.receiptPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class, dbSession);
        this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
        // this.salesItemPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemPosService.class,dbSession);
        super.startup();
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

    @Override
    public Map<String, String> getPluginPropertyConfig() {
        Map<String, String> propConfig = new HashMap<>  ();
        propConfig.put(SPECIAL_DISCOUNT_CALC_SERVICE_URL, TEXT);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_APIKEY, STRING);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_SECURE, BOOLEAN);
        propConfig.put(SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN, BOOLEAN);
        return propConfig;
    }
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
        try {
            ReceiptEntity receipt = (ReceiptEntity) args[0];
            if (args.length == 4 && ReceiptEntity.Status.NEW.equals(receipt.getStatus()) && receipt.getPaymentItems().isEmpty()) 
            {
            if(LockCalculation()) 
            {
                if (args.length == 4 && ReceiptEntity.Status.NEW.equals(receipt.getStatus()) && receipt.getPaymentItems().isEmpty()) 
                {
                    logger.info("---------------- Calculation -------------------");
                    TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
                    transactionLogic.CalculatePromotios(receipt);
                    // ReceiptHelper.markSalesItemsAsChanged(receipt);
                } else if(args.length == 4 && !ReceiptEntity.Status.NEW.equals(receipt.getStatus())){
                    logger.info("This is not a new receipt, not calculating discounts");
                }
                unlockCalculation();
            } else {
                logger.info("Calculation run missed");
            }
        }
        } catch (Exception e) {
            unlockCalculation();
            logger.error("ERROR CALCULATING", e);
            // currentlyCalculating.set(false);
        }
    }

    @PluginAt(pluginClass = ReceiptPosService.class, method = "postReceipt", where = PluginAt.POSITION.BEFORE)
    public Object postReceipt(Object proxy, Object[] args, Object ret)
    // @PluginAt(pluginClass = ReceiptPosService.class, method = "postReceipt", where = PluginAt.POSITION.AFTER)
    // public Object postReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller)
    {
        ReceiptEntity transaction = (ReceiptEntity)args[0];
        if(transaction.getSalesItems().stream().anyMatch(a->
        {
            if(a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
              return false;
              else
            return Integer.valueOf(a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue())>0;
            
        }
        ))
        {
            TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
            transactionLogic.postReceipt(transaction);
            
        }
        return ret;
    }




    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "startReturn", where = PluginAt.POSITION.AFTER)
    public Object startReturn(Object proxy, Object[] args, Object ret, StackTraceElement caller)
    throws BreakExecutionException, IOException, InterruptedException {
        ReturnReceiptObject result = (ReturnReceiptObject) ret;
        if(this.getPluginConfig().getAdvreturn())
        {

            ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
            ReceiptEntity sourceReceipt = result.getSourceReceipt();
            if (targetReceipt != null) {
                TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
                ReturnTransactionLogic logic=new ReturnTransactionLogic(this, receiptManager, calculationPosService,transactionLogic);
                logic.ItemForReturn(result);
            }
        }
        return result;
    }
    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveSalesItemByQuantity", where = PluginAt.POSITION.AFTER)
    public Object moveSalesItemByQuantity(Object proxy, Object[] args, Object ret, StackTraceElement caller)
            throws BreakExecutionException, IOException, InterruptedException {
        ReturnReceiptObject result = (ReturnReceiptObject) ret;
        if(this.getPluginConfig().getAdvreturn())
        {

            ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
            ReceiptEntity sourceReceipt = result.getSourceReceipt();
            if (targetReceipt != null) {
                TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
                ReturnTransactionLogic logic=new ReturnTransactionLogic(this, receiptManager, calculationPosService,transactionLogic);
                logic.ItemForReturn(result);
            }
        }
        return result;
    }
    
    @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveReturnedReceiptToCurrentReceipt", where = PluginAt.POSITION.AFTER)
    public Object moveReturnedReceiptToCurrentReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller) throws BreakExecutionException {
        ReceiptEntity result = (ReceiptEntity) ret;
        if (args.length == 8) 
            if(this.getPluginConfig().getAdvreturn())
            {
            ReceiptEntity receiptWithAdjustmentItems = (ReceiptEntity) args[1];
            TransactionLogic transactionLogic = new TransactionLogic(this, receiptManager, calculationPosService);
            ReturnTransactionLogic logic=new ReturnTransactionLogic(this, receiptManager, calculationPosService,transactionLogic);
            logic.moveReturnedReceiptToCurrentReceipt(receiptWithAdjustmentItems, result);
        }
        return result;
    }

}
