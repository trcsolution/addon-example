// /*
// * This Java source file was generated by the Gradle 'init' task.
//  */
// package com.trc.ccopromo;

// import com.sap.scco.ap.bl.calculation.DiscountElementFactory;
// // import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sap.scco.ap.plugin.BasePlugin;
// import com.sap.scco.ap.plugin.BreakExecutionException;
// import com.sap.scco.ap.pos.dao.ReceiptManager;

// // import org.eclipse.persistence.indirection.IndirectList;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// // import com.sap.scco.ap.plugin.annotation.ListenToExit;
// import com.sap.scco.ap.plugin.annotation.PluginAt;
// // import com.sap.scco.ap.plugin.annotation.PluginAt.POSITION;
// // import com.sap.scco.ap.plugin.annotation.ui.JSInject;
// // import com.sap.scco.ap.plugin.annotation.ui.JSInject;
// import com.sap.scco.ap.pos.service.*;
// import com.sap.scco.ap.pos.service.AccountCouponPosService;
// import com.sap.scco.ap.pos.service.ReceiptChangeNotifierPosService;
// import com.sap.scco.ap.pos.service.component.listener.ReceiptChangeListener;
// import com.sap.scco.ap.registry.UserRegistry;
// import com.sap.scco.ap.pos.service.PosService;
// // import com.sap.scco.ap.pos.service.RetxurnSalesItemPosService;
// // import com.sap.scco.ap.pos.service.impl.SalesItemPosServiceImpl;
// // import com.sap.scco.ap.pos.util.ui.BroadcasterHolder;
// // import com.sap.scco.ap.pos.service.impl.ReturnSalesItemPosServiceImpl;
// // import com.trc.ccopromo.models.*;

// // import ch.qos.logback.core.joran.conditional.ElseAction;

// import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
// // import com.sap.scco.ap.pos.exception.RequestProcessingException;
// // import com.sap.scco.ap.pos.entity.MaterialEntity;
// import com.sap.scco.ap.pos.entity.ReceiptEntity;
// // import com.sap.scco.ap.pos.entity.SalesItemEntity;
// // import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
// // import com.sap.scco.ap.pos.entity.coupon.DiscountElementEntity;
// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.sap.scco.ap.pos.entity.SalesItemNoteEntity;
// import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
// import com.sap.scco.ap.pos.entity.coupon.CouponAssignmentEntity;
// import com.sap.scco.ap.pos.entity.discountrule.DiscountType;

// // import java.util.ArrayList;
// import java.util.concurrent.atomic.AtomicBoolean;
// // import java.util.function.Function;
// // import java.util.stream.Collector;
// // import java.util.stream.Collectors;
// import java.util.HashMap;
// // import java.util.List;
// import java.util.Map;
// // import net.sf.json.JSONObject;

// import java.io.IOException;
// import java.math.BigDecimal;
// // import java.math.MathContext;
// // import java.net.URI;
// import java.net.URISyntaxException;
// // import java.net.http.HttpClient;
// // import java.net.http.HttpRequest;
// // import java.net.http.HttpResponse;
// // import java.time.Duration;
// // import com.sap.scco.env.UIEventDispatcher;
// import com.sap.scco.cs.utilities.ReceiptHelper;
// // import com.sap.scco.ap.pos.dao.IReceiptManager;
// import com.sap.scco.ap.pos.dao.*;
// import com.sap.scco.ap.pos.dto.coupon.DiscountElementDTO;
// // import com.sap.scco.ap.registry.UserRegistry;
// import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
// // import com.sap.scco.env.UIEventDispatcher;
// // import com.sap.scco.util.CConst;
// // import com.sap.scco.util.exception.ValidationException;
// // import com.sap.scco.util.types.LogGDT;
// // import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
// // import com.sap.scco.ap.pos.entity.BaseEntity;
// import com.sap.scco.env.UIEventDispatcher;
// import com.sap.scco.util.CConst;
// import com.trc.ccopromo.PluginConfig;
// import com.trc.ccopromo.models.session.DiscountSource;
// import com.trc.ccopromo.models.session.TransactionState;

// public class TrcPromoAddonOld {
// // extends BasePlugin implements ReceiptChangeListener {
//     // Data types
//     public static final String STRING = "string";
//     public static final String PASSWORD = "password";
//     public static final String TEXT = "text";
//     public static final String INT = "int";
//     public static final String BOOLEAN = "boolean";

//     public static final String SPECIAL_DISCOUNT_CALC_SERVICE_URL = "SPECIAL_DISCOUNT_CALC_SERVICE_URL";
//     public static final String SPECIAL_DISCOUNT_SERVICE_APIKEY = "SPECIAL_DISCOUNT_SERVICE_APIKEY";
//     public static final String SPECIAL_DISCOUNT_SERVICE_SECURE = "SPECIAL_DISCOUNT_SERVICE_SECURE";
//     public static final String SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN = "SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN";

//     private static Logger logger = LoggerFactory.getLogger(TrcPromoAddon.class);
//     private static AtomicBoolean currentlyCalculating = new AtomicBoolean(false);
//     // private static final HttpClient httpClient = HttpClient.newBuilder()
//     // .version(HttpClient.Version.HTTP_1_1)
//     // .connectTimeout(Duration.ofSeconds(10))
//     // .build();
//     private CDBSession dbSession;
//     // private PriceDiscountManager priceDiscountManager;
//     // private MaterialPosService materialPosService;
//     // private PosService posService;
//     private ReceiptPosService receiptPosService;
//     private ReceiptChangeNotifierPosService notifierService;

//     private ReceiptManager receiptManager;
//     // private SalesItemManager salesItemManager;
//     private CalculationPosService calculationPosService;
//     private AccountCouponPosService accountCouponService;

//     public static com.trc.ccopromo.models.session.TransactionState m_transactionState;
//     // private SalesItemPosService salesItemPosService;
//     // private EntityFactory entiryFactiry;
//     // static private Boolean ignoreCalculation=false;

//     // @Override
//     // public String getId() {
//     //     return "TRCPromo";
//     // }

//     // @Override
//     // public String getName() {
//     //     return "TRC Promo";
//     // }

//     // @Override
//     // public String getVersion() {
//     //     return "2.1.7";
//     // }

//     // @Override
//     public void startup() {
    
//         // currentlyCalculating.set(false);
//         m_transactionState=new TransactionState();

//         this.dbSession = CDBSessionFactory.instance.createSession();
//         this.receiptManager = new ReceiptManager(dbSession);
        
        
//         this.receiptPosService =
//         ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class,
//         dbSession);
        
//         // receiptPosService.getRegetDbSession().getSession().

//         this.notifierService =
//         ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptChangeNotifierPosService.class,
//         dbSession);
//         // notifierService.registerChangeListener(this);

//         this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,
//                 dbSession);
//         this.accountCouponService=ServiceFactory.INSTANCE.getOrCreateServiceInstance(AccountCouponPosService.class,
//         dbSession);
//         // this.salesItemPosService =
//         // Serv
//         // var diiscountPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(Disco,
//         //         dbSession);
//         // this.salesItemPosService =
//         // ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemPosService.class,dbSession);

//         currentlyCalculating.set(false);
//         // super.startup();
//     }

//     // @Override
//     // public boolean persistPropertiesToDB() {
//     //     return true;
//     // }

//     public PluginConfig getPluginConfig() {
//         PluginConfig pluginConfig = new PluginConfig();

//         // pluginConfig.setBaseUrl(getProperty(SPECIAL_DISCOUNT_CALC_SERVICE_URL, "")); // "http://192.168.0.17:5261/api/Promo/Calculate"
//         // pluginConfig.setAPIKey(getProperty(SPECIAL_DISCOUNT_SERVICE_APIKEY, null));
//         // pluginConfig.setSecure(getProperty(SPECIAL_DISCOUNT_SERVICE_SECURE, false));
//         // pluginConfig.setAdvreturn(getProperty(SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN, false));
//         return pluginConfig;
//     }

//     // @Override
//     // public Map<String, String> getPluginPropertyConfig() {
//     //     Map<String, String> propConfig = new HashMap<>();
//     //     propConfig.put(SPECIAL_DISCOUNT_CALC_SERVICE_URL, TEXT);
//     //     propConfig.put(SPECIAL_DISCOUNT_SERVICE_APIKEY, STRING);
//     //     propConfig.put(SPECIAL_DISCOUNT_SERVICE_SECURE, BOOLEAN);
//     //     propConfig.put(SPECIAL_DISCOUNT_SERVICE_ADVANCED_RETURN, BOOLEAN);
//     //     return propConfig;
//     // }

//     public Boolean LockCalculation() {
//         return (currentlyCalculating.compareAndSet(false, true));
//     }

//     public void unlockCalculation() {
//         currentlyCalculating.set(false);
//     }

//     // @PluginAt(pluginClass = CalculationPosService.class, method = "calculate", where = PluginAt.POSITION.BEFORE)
//     // public void applyPromotions(Object proxy, Object[] args, StackTraceElement caller)
//     //         throws InconsistentReceiptStateException, IOException, InterruptedException, URISyntaxException {
//     //     // try {
//     //     ReceiptEntity receipt = (ReceiptEntity) args[0];

//     //     if (args.length == 4 && ReceiptEntity.Status.NEW.equals(receipt.getStatus())
//     //             && receipt.getPaymentItems().isEmpty()) {
//     //         boolean isAnySalesItem = receipt.getSalesItems().stream()
//     //                 .anyMatch(a -> !a.getStatus().equals("3") && a.getQuantity().compareTo(BigDecimal.ZERO) > 0);
//     //         if (isAnySalesItem)
//     //             if (LockCalculation()) {
//     //                 try {
//     //                     logger.info("---------------- Calculation -------------------");
//     //                     TransactionLogic transactionLogic = new TransactionLogic(this, //receiptManager,
//     //                             calculationPosService);
//     //                     transactionLogic.CalculatePromotios(receipt);
//     //                     unlockCalculation();
//     //                 } catch (Exception e) {
//     //                     unlockCalculation();
//     //                 }

//     //             }

//     //     }

//     // }
    

//     // @PluginAt(pluginClass = ReceiptPosService.class, method = "updateSalesItem", where = PluginAt.POSITION.AFTER)
//     // public Object updateSalesItem(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException, IOException, InterruptedException, URISyntaxException

//     // {
//     //     logger.info("updateSalesItem!!!!!!!!!!!!!!!!!!!!!!!");
//     //     return ret;
//     // }
//     // @PluginAt(pluginClass = ReceiptPosService.class, method = "voidReceipt", where = PluginAt.POSITION.AFTER)
//     // public Object updateSalesItem(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException, IOException, InterruptedException, URISyntaxException

//     // {
//     //     logger.info("updateSalesItem!!!!!!!!!!!!!!!!!!!!!!!");
//     //     return ret;
//     // }

//     // @PluginAt(pluginClass = ReceiptPosService.class, method = "addSalesItem", where = PluginAt.POSITION.AFTER)
//     // public void addSalesItem(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //     ReceiptEntity receipt = (ReceiptEntity) args[0];
//     //     TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //     transactionLogic.CalculatePromotios(receipt);
//     //     // sendMessageToUi("sasa","description");
//     // }

//     // @PluginAt(pluginClass = ReceiptPosService.class, method = "updateSalesItem", where = PluginAt.POSITION.AFTER)
//     // public void updateSalesItem(Object proxy, Object[] args, StackTraceElement caller, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //     ReceiptEntity receipt = (ReceiptEntity) args[0];
//     //     TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //     transactionLogic.CalculatePromotios(receipt);
//     // }



    
//     // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnSalesItemUpdated", where = PluginAt.POSITION.AFTER)
//     // public void notifyListenersOnSalesItemUpdated(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>1)
//     //                 if (((Object) args[1]).getClass().equals(SalesItemEntity.class)) 
//     //             {
//     //                 var receipt=((SalesItemEntity)args[1]).getReceipt();
//     //                 TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                 transactionLogic.CalculatePromotios(receipt);
//     //             }
//     // }
//     // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnSalesItemAdded", where = PluginAt.POSITION.AFTER)
//     // public void notifyListenersOnSalesItnotifyListenersOnSalesItemAddedemVoided(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>1)
//     //                 if (((Object) args[0]).getClass().equals(ReceiptEntity.class)) 
//     //             {
//     //                 var receipt=(ReceiptEntity)args[0];
//     //                 TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                 transactionLogic.CalculatePromotios(receipt);
//     //             }
//     // }
//     // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnSalesItemVoided", where = PluginAt.POSITION.AFTER)
//     // public void notifyListenersOnSalesItemVoided(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>1)
//     //                 if (((Object) args[1]).getClass().equals(SalesItemEntity.class)) 
//     //             {
//     //                 var receipt=((SalesItemEntity)args[1]).getReceipt();
//     //                 TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                 transactionLogic.CalculatePromotios(receipt);
//     //             }
//     // }





//     // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnCouponAdded", where = PluginAt.POSITION.BEFORE)
//     // public void notifyListenersOnCouponAdded(Object proxy, Object[] args, Object objItem)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>0)
//     //                 if (((Object) args[0]).getClass().equals(ReceiptEntity.class)) 
//     //             {
//     //                 var receipt=(ReceiptEntity)args[0];
//     //                 var salesItem=receipt.getSalesItems().get(0);
//     //                 salesItem.setGrossAmount(BigDecimal.valueOf(20));
//     //                 salesItem.setUnitGrossAmount(BigDecimal.valueOf(10));
//     //                 salesItem.setUnitPriceChanged(true);
//     //                 salesItem.setGrossAmount(BigDecimal.valueOf(10));
                    
//     //                 // salesItem.getDiscountElements().get(0).setDiscountAmount(BigDecimal.valueOf(2));
//     //             //     salesItem.setDiscountElements(null);
//     //             //      // resetDiscountToSalesItem(salesItem);
//     //             // salesItem.setDiscountElements(null);
//     //             // receipt.getCouponAssignments().get(0).clearBaseValues();
//     //             // receipt.getCouponAssignments().get(0).setDiscountElements(null);
//     //             // receipt.setDiscountElements(null);
//     //             // receipt.setCouponAssignments(null);
                

//     //                 // TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                 // transactionLogic.KeepCouponAsAdditionalField((ReceiptEntity)args[0]);
//     //             }
//     // }

    

//     // public void onCouponAdded(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
//     //     this.Calculate(receipt);

//     //     // transactionLogic.SetLineDiscount(salesItem, BigDecimal.valueOf(2));
//     //     // salesItem.setUnitPriceChanged(true);
//     //     // // salesItem.setDescription("11111111");

//     //     // // this.receiptManager.update(receipt);
//     //     // calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);

//     //     // // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);
//     //     // logger.info("222222222");
//     // }
//     // public void onCouponRemoved(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {

//     //     this.Calculate(receipt);

//     // }

//     void SetNote(SalesItemEntity salesItem,String key,String note)
//     {
//         var item=salesItem.getNotes().stream().filter(a->a.getKey()==key).findAny();
//         if(item.isEmpty())
//         {
//             SalesItemNoteEntity entity=new SalesItemNoteEntity();
//             entity.setText(note);
//             entity.setKey(key);
//             salesItem.addNote(entity);
//         }
//         else
//         {
//             if(note==null)
//                 item.get().setText(null);
//                     // salesItem.getNotes().remove(item);
//                 else
//                     item.get().setText(note);
//         }
//     }
//     void Calculate(com.sap.scco.ap.pos.entity.ReceiptEntity receipt){
//         var salesItem=receipt.getSalesItems().get(0);
//             // SetNote(salesItem,"1111",String.valueOf(System.currentTimeMillis()));
            
            

//         // try {
//         //     TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//         //     transactionLogic.CalculatePromotios(receipt);
//         //     calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
            
//         // } catch (IOException e) {
//         //     // TODO Auto-generated catch block
//         //     e.printStackTrace();
//         // } catch (InterruptedException e) {
//         //     // TODO Auto-generated catch block
//         //     e.printStackTrace();
//         // } catch (URISyntaxException e) {
//         //     // TODO Auto-generated catch block
//         //     e.printStackTrace();
//         // }
//     }

//     public  void onSalesItemAddedToReceipt(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, java.util.List<com.sap.scco.ap.pos.entity.SalesItemEntity> salesItems, java.math.BigDecimal quantity) {
//         // logger.info("-------- ITEM ADD ------");
//         this.Calculate(receipt);
//     }

    
//     public void onSalesItemVoided(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem) {
//         // logger.info("-------- ITEM VOIDED ------");
//         this.Calculate(receipt);
        
//     }
    
//     // public void onSalesItemUpdated(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity newSalesItem, java.math.BigDecimal quantity) {
//     //     // logger.info("-------- ITEM UPDATE ------");

//     //     if(this.m_transactionState.HasDiscountChanged(newSalesItem.getKey(), newSalesItem.getDiscountAmount()))
//     //     {
//     //         this.m_transactionState.setDiscountsource(newSalesItem.getKey(), com.trc.ccopromo.models.session.DiscountSource.Manualy, newSalesItem.getDiscountAmount());
//     //         SetNote(newSalesItem, "111","Manually changed");
//     //     }
//     //     this.Calculate(receipt);
//     // }
//     public void onCouponAdded(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {

//         // logger.info("-------- COUPON ADD ------");
//         // TransactionLogic_old transactionLogic = new TransactionLogic_old(this, calculationPosService);

//         // receipt.getSalesItems().forEach(salesItem->{
//         //     if(salesItem.getDiscountElements().size()>0)
//         //     {
//         //         var existingSource=m_transactionState.getDiscountsource(salesItem.getKey());
//         //         if(existingSource!=DiscountSource.Coupon) ///apply coupon - > apply promo for this item and then apply coupon 
//         //         {

//         //         }
//         //         m_transactionState.setDiscountsource(salesItem.getKey(),DiscountSource.Coupon, salesItem.getDiscountElements().get(0).getDiscountAmount());
//         //     }
//         //     else
//         //     {
//         //         m_transactionState.setDiscountsource(salesItem.getKey(),m_transactionState.getDiscountsource(salesItem.getKey()), salesItem.getDiscountAmount());
//         //     }
//         // });
//         //apply promotions

//         var salesItem=receipt.getSalesItems().get(0);
//         //SetNote(salesItem,"1111",String.valueOf(System.currentTimeMillis()));

//         // transactionLogic.SetLineDiscount(salesItem, BigDecimal.valueOf(4));
//         salesItem.setUnitPriceChanged(true);

//         var discount=salesItem.getDiscountElements().get(0);

//         SetNote(salesItem, "111",discount.getCouponAssignment().getCoupon().getCode());
//         discount.getAppliedRule().getCouponAssignments().clear();
//         discount.getCouponAssignment().getAppliedRules().clear();
//         discount.setCouponAssignment(null);
//         salesItem.getDiscountElements().clear();
//         receipt.getCouponAssignments().clear();
//         receipt.getDiscountElements().clear();
//         receipt.setDiscountElements(null);
//         // transactionLogic.SetLineDiscount(salesItem, BigDecimal.valueOf(4.5));
//         salesItem.setUnitPriceChanged(true);

        
//         // this.accountCouponService.removeAllCoupons(receipt);
//        this.Calculate(receipt);
//        receiptManager.update(receipt);
//     //    salesItem.getNotes().clear();
//        calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
//            UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);
        
//     }
    
//     public void onCouponRemoved(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
//         logger.info("-------- COUPON REMOVED ------");
        
//     }

  
    

    
    

    
//     // // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnCouponAdded", where = PluginAt.POSITION.AFTER)
//     // public void notifyListenersOnCouponAddedAfter(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>0)
//     //                 if (((Object) args[0]).getClass().equals(ReceiptEntity.class)) 
//     //             {
//     //                 TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                 var receipt=(ReceiptEntity)args[0];
//     //                 var salesItem=receipt.getSalesItems().get(0);
//     //                 transactionLogic.SetLineDiscount(salesItem, BigDecimal.valueOf(2));
//     //                 salesItem.setUnitPriceChanged(true);
//     //                 // salesItem.setDescription("kllmlmlmklmlk");
//     //                 this.receiptManager.update(receipt);
//     //                 calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
//     //                 // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);
//     //             }
//     // }
//     // // @PluginAt(pluginClass = ReceiptChangeNotifierPosService.class, method = "notifyListenersOnCouponRemoved", where = PluginAt.POSITION.AFTER)
//     // public void notifyListenersOnCouponRemoved(Object proxy, Object[] args, Object objItem, Object obj)
//     //         throws IOException, InterruptedException, URISyntaxException {
//     //             if(args.length>0)
//     //                 if (((Object) args[0]).getClass().equals(ReceiptEntity.class)) 
//     //             {
//     //                 var receipt=(ReceiptEntity)args[0];
//     //                 TransactionLogic transactionLogic = new TransactionLogic(this, calculationPosService);
//     //                // transactionLogic.CalculatePromotios(receipt);
//     //             }
//     // }


//     // @PluginAt(pluginClass = ReceiptPosService.class, method = "postReceipt",
//     // where = PluginAt.POSITION.AFTER)
//     // public Object postReceipt(Object proxy, Object[] args, Object ret,
//     // StackTraceElement caller)
    


    
//     @PluginAt(pluginClass = ReceiptPosService.class, method = "postReceipt", where = PluginAt.POSITION.BEFORE)
//     public Object postReceipt(Object proxy, Object[] args, Object ret)
//             throws IOException, InterruptedException, URISyntaxException {
//         ReceiptEntity transaction = (ReceiptEntity) args[0];
//         m_transactionState=new TransactionState();
//         if (transaction.getSalesItems().stream().anyMatch(a -> {
//             if (a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID) == null)
//                 return false;
//             else
//                 return Integer.valueOf(a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue()) > 0;

//         })) {
//             // TransactionLogic transactionLogic = new TransactionLogic(this, 
//             // // receiptManager, 
//             // calculationPosService);
//             // transactionLogic.postReceipt(transaction);

//         }
//         return ret;
//     }

//     // @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "startReturn", where = PluginAt.POSITION.AFTER)
//     // public Object startReturn(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException, IOException, InterruptedException, URISyntaxException

//     // {
//     //     // logger.info("00000000");
//     //     // totalReminingAmount=BigDecimal.ZERO;
//     //     ReturnReceiptObject result = (ReturnReceiptObject) ret;
//     //     if (this.getPluginConfig().getAdvreturn()) {

//     //         ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
//     //         // ReceiptEntity sourceReceipt = result.getSourceReceipt();
//     //         if (targetReceipt != null) {
//     //             TransactionLogic_old transactionLogic = new TransactionLogic_old(this, //receiptManager,
//     //              calculationPosService);
//     //             ReturnTransactionLogic_old logic = new ReturnTransactionLogic_old(this, 
//     //             // receiptManager,
//     //              calculationPosService,
//     //                     transactionLogic);
//     //             // totalReminingAmount=
//     //             logic.ItemForReturn(result, true);
//     //         }
//     //     }
//     //     return result;
//     // }
//     boolean returnWholeReceipt=false;
//     BigDecimal totalReminingAmount=BigDecimal.ZERO;
//     @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "returnWholeReceipt", where = PluginAt.POSITION.BEFORE)
//     public Object returnWholeReceiptBefore(Object proxy, Object[] args, Object ret) {
//         // logger.info("-1-1-1-1-1-1-11");
//         returnWholeReceipt=true;
//         return null;
//     }

//     // @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "returnWholeReceipt", where = PluginAt.POSITION.AFTER)
//     // public Object returnWholeReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException, IOException, InterruptedException, URISyntaxException
//     // {
//     //     logger.info("11111111");
//     //     ReturnReceiptObject result = (ReturnReceiptObject) ret;
//     //     // if (this.getPluginConfig().getAdvreturn()) {
//     //     // }
//     //     return result;
//     // }

//     // @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveSalesItemByQuantity", where = PluginAt.POSITION.AFTER)
//     // public Object moveSalesItemByQuantity(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException, IOException, InterruptedException, URISyntaxException {
//     //     ReturnReceiptObject result = (ReturnReceiptObject) ret;
//     //     if (this.getPluginConfig().getAdvreturn()) {

//     //         ReceiptEntity targetReceipt = result.getIndividualItemsReceipt();
//     //         // ReceiptEntity sourceReceipt = result.getSourceReceipt();
//     //         if (targetReceipt != null) {
//     //             TransactionLogic_old transactionLogic = new TransactionLogic_old(this, //receiptManager, 
//     //             calculationPosService);
//     //             ReturnTransactionLogic_old logic = new ReturnTransactionLogic_old(this//, receiptManager
//     //             , calculationPosService,
//     //                     transactionLogic);
//     //             logic.ItemForReturn(result, false);
//     //         }
//     //     }
//     //     return result;
//     // }

//     // @PluginAt(pluginClass = ReturnReceiptPosService.class, method = "moveReturnedReceiptToCurrentReceipt", where = PluginAt.POSITION.AFTER)
//     // public Object moveReturnedReceiptToCurrentReceipt(Object proxy, Object[] args, Object ret, StackTraceElement caller)
//     //         throws BreakExecutionException {
//     //     // logger.info("22222222");
//     //     ReceiptEntity result = (ReceiptEntity) ret;
//     //     if (args.length == 8)
//     //         if (this.getPluginConfig().getAdvreturn()) {
//     //             ReceiptEntity receiptWithAdjustmentItems = (ReceiptEntity) args[1];
//     //             TransactionLogic_old transactionLogic = new TransactionLogic_old(this, 
//     //             // receiptManager, 
//     //             calculationPosService);

//     //             // this.receiptManager

//     //             ReturnTransactionLogic logic = new ReturnTransactionLogic_old(this, //receiptManager, 
//     //             calculationPosService,
//     //                     transactionLogic);
//     //             // String transactionId=(args[2]==null)?null:(String) args[2];
//     //             logic.moveReturnedReceiptToCurrentReceipt(receiptWithAdjustmentItems, result,returnWholeReceipt,totalReminingAmount);
//     //         }
//     //     returnWholeReceipt=false;
//     //     return result;
//     // }

// }