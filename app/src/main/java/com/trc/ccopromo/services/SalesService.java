package com.trc.ccopromo.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.dto.AdditionalFieldDTO;
import com.sap.scco.ap.pos.dto.ReceiptPrintDTO;
import com.sap.scco.ap.pos.dto.SalesItemDTO;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.entity.BusinessPartnerEntity;
import com.sap.scco.ap.pos.entity.PrintTemplateEntity;
import com.sap.scco.ap.pos.exception.InconsistentReceiptStateException;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.SalesItemPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.registry.UserRegistry;
import com.sap.scco.env.UIEventDispatcher;
import com.sap.scco.util.CConst;
// import com.trc.ccopromo.TransactionTools;
import com.trc.ccopromo.TrcPromoAddon;
import com.trc.ccopromo.models.Constants;
import com.trc.ccopromo.models.Coupon;
import com.trc.ccopromo.models.Coupons;
import com.trc.ccopromo.models.PromoResponse;
import com.trc.ccopromo.models.receipt.Promo;
import com.trc.ccopromo.models.receipt.PromoItem;
import com.trc.ccopromo.models.storedpromo.StoredPromo;
import com.trc.ccopromo.models.transaction.post.PostTransactionRequest;
import org.apache.commons.lang3.StringUtils;

public class SalesService extends BasePromoService {

    private CDBSession dbSession;

    public SalesService(TrcPromoAddon addon,CDBSession dbSession)
    {
        super(addon, dbSession);

    }


    void CalculateCurrent() throws InconsistentReceiptStateException{
        receiptPosService.getDbSession().getEM().clear();
        ReceiptPosService receiptService = this.receiptPosService;
        ReceiptEntity receipt = receiptService.findOrCreate(UserRegistry.INSTANCE.getCurrentUser(), null, false);
        // ReceiptEntity receipt = receiptService.findOrCreate(UserRegistry.INSTANCE.getCurrentUser(), null, false);
        if (ReceiptEntity.Status.NEW.equals(receipt.getStatus())) {

            // Calculate(receipt);

            receiptManager.update(receipt);
            calculationPosService.recalculateReceipt(receipt);
            // LogGDT log = receiptManager.update(receipt);
            receiptPosService.getDbSession().getEM().clear();
            receiptManager.manualFlush();

            Misc.pushEvent("RECEIPT_REFRESH", null);
        }
    }
    
    public PromoResponse RequestPromo(ReceiptEntity _transaction,ArrayList<Integer> refPromos) throws IOException, InterruptedException, URISyntaxException {

        ReceiptEntity receipt = _transaction;
        logger.info("------RequestPromo------");
        com.trc.ccopromo.models.PromoRequest promorequest = Misc.MakePromoRequest(receipt,refPromos);
        var response=webPromoService.PostCalculationRequest(promorequest);
        // var request = new WebRequest(_addon.getPluginConfig());
        // var response=request.Post("/api/Promo/Calculate", promorequest);
        ObjectMapper m = new ObjectMapper();
        PromoResponse resp = m.readValue(response, PromoResponse.class);
        return resp;//request.Request(receipt,refPromos);
    }
    
    public void MarkItemAsManualDiscounted(SalesItemEntity salesItem,boolean IsOn)
    {
        if(IsOn)
            Misc.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE,com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE_MANUAL); 
            else
            Misc.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE,null); 
    }
    public void setBusinessPartner(ReceiptEntity receipt,BusinessPartnerEntity businessPartner)
    {
        this.Calculate(receipt);
        calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        receiptManager.update(receipt);
    }
    public void onSalesItemAddedToReceipt(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, java.util.List<com.sap.scco.ap.pos.entity.SalesItemEntity> salesItems, java.math.BigDecimal quantity)
    {
        this.Calculate(receipt);
        calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        
    }

    public void onSalesItemVoided(com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity salesItem)  {
        Misc.ClearPromo(salesItem, true);
        SetLineDiscount(salesItem, BigDecimal.ZERO);

        Calculate(receipt);
        var calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,this.dbSession);
            calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        receiptManager.update(receipt);

    }    
    public void Calculate(ReceiptEntity receipt)  {
        ResetSalesItems(receipt);
        if(receipt.getSalesItems().stream().anyMatch(salesItem->IsDiscountableItem(salesItem)
        //  && salesItem.getDiscountElements().isEmpty()
         ))
         {
            PromoResponse promos;
            try {
                promos = RequestPromo(receipt,null);
                if(promos!=null)
                if(promos.itemDiscounts!=null)
                    if(!promos.itemDiscounts.isEmpty())
                {
                    ApplyPromoDiscountsToTransaction(promos,receipt,BigDecimal.ZERO);
                }
                //coupons
                if(promos.coupons!=null)
                    if(promos.coupons.size()>0)
                {
                    // Coupon
                    ObjectMapper m = new ObjectMapper();
                    Coupons coupons=new Coupons(promos.coupons);
                    setTransactionAdditionalField(receipt,com.trc.ccopromo.models.Constants.TRC_COUPONS,m.writeValueAsString(coupons));
                }



            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
         }
    }

    public void onCouponAdded(com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
       this.getSalesItems(receipt).forEach(a->{
        if(!a.getDiscountElements().isEmpty())
        {
            MarkItemAsManualDiscounted(a,false);
            Misc.ClearPromo(a, true);

        }
       });
    ///    Calculate(receipt);
        receiptManager.update(receipt);
    }
    public void onCouponRemoved(com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
        // MarkItemAsManualDiscounted()
        Calculate(receipt);
        calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        // UIEventDispatcher.INSTANCE.dispatchAction(CConst.UIEventsIds.RECEIPT_REFRESH, null, receipt);
    }


    


    public void postReceipt(ReceiptEntity receipt) throws IOException, InterruptedException, URISyntaxException 
    {
        if(receipt.getStatus()!="2")
            return;
        PostTransactionRequest requestObj=new PostTransactionRequest();
        requestObj.data=new com.trc.ccopromo.models.transaction.post.Data();
        requestObj.data.transactionNumber=receipt.getId();
        requestObj.data.isPosted=false;
        requestObj.data.items=receipt.getSalesItems().stream().
            collect(
                Collectors.mapping(
                a->new com.trc.ccopromo.models.transaction.post.Item(a.getId()
                ,a.getMaterial().getArticleGroup().getId()
                ,a.getQuantity().doubleValue()
                ,a.getUnitGrossAmount().doubleValue()
                ,a.getDiscountAmount().doubleValue()
                ,a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)
            ),Collectors.toList()));
        
        var promos=webPromoService.PostTransaction(requestObj);
        var mapper = new ObjectMapper();
        for (int i = 0; i < promos.storedPromos.size(); i++) {
            var s1=mapper.writeValueAsString(promos.storedPromos.get(i));
            setTransactionAdditionalField(receipt,"Promo:"+String.valueOf(i) ,s1);
        }




        // var request = new WebRequest(_addon.getPluginConfig());
        
        // // try {
        //     //save transaction online
        //     String json = request.Post("/api/Promo/Save", requestObj,false);
        //     //save promos into transaction
        //     var mapper = new ObjectMapper();
        //     var promos=mapper.readValue("{\"p\":"+json+"}", com.trc.ccopromo.models.storedpromo.StoredPromos.class);
        //     for (int i = 0; i < promos.storedPromos.size(); i++) {
        //         var s1=mapper.writeValueAsString(promos.storedPromos.get(i));
        //         setTransactionAdditionalField(receipt,"Promo:"+String.valueOf(i) ,s1);
        //     }
        // } catch (IOException | InterruptedException e) {
        //     
        //     e.printStackTrace();
        // }
        // request.PostTransaction(receipt);
    }



    public static int getPromoId(SalesItemDTO entry)
    {
        
        if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)==null)
                return Integer.valueOf(0);
            else
            if(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue().length()==0)
                return Integer.valueOf(0);
            else
                return Integer.parseInt(entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue());
    }

    

    public static String getTransactiontAdditionalField(ReceiptPrintDTO receipt,String fieldName)
        {
            AdditionalFieldDTO additionalField = receipt.getAdditionalField(fieldName);
            if(additionalField==null)
                return null;
            return additionalField.getValue();
        }


    public void OnPrintReceipt(ReceiptPrintDTO receiptprint,PrintTemplateEntity template,Map<String, Object> rootMap) {
        if(receiptprint.getAdditionalFields()!=null)
        {
            var mapper = new ObjectMapper();
            List<com.trc.ccopromo.models.receipt.Promo> promos = new ArrayList<>();
            promos.addAll(
            receiptprint.getAdditionalFields().stream().filter(a->StringUtils.isNotEmpty(a.getValue()) 
            && a.getFieldName().startsWith("Promo:")).map(AdditionalFieldDTO::getValue).
            map(a->
            {
                try {
                    var p=mapper.readValue(a,StoredPromo.class);
                    if(p==null)
                     return null;
                     else
                     {
                        var prods=p.products.stream().map(itemcode->{
                            var anyItem=receiptprint.getSalesItems().stream().filter(b->b.getId().compareTo(itemcode)==0 && getPromoId(b)==p.promoId).findAny();
                            if(anyItem.isEmpty())
                                anyItem=receiptprint.getSalesItems().stream().filter(b->b.getId().compareTo(itemcode)==0).findAny();
                            var totalAmountWithoutReceiptDiscount=receiptprint.getSalesItems().stream().filter(b->b.getId().compareTo(itemcode)==0 )//&& getPromoId(b)==p.promoId
                                .collect(Collectors.reducing(BigDecimal.ZERO,x->BigDecimal.valueOf(x.getPaymentGrossAmountWithoutReceiptDiscount()),BigDecimal::add));
                            var totalPaymentGrossAmount=receiptprint.getSalesItems().stream().filter(b->b.getId().compareTo(itemcode)==0 )//&& getPromoId(b)==p.promoId
                                .collect(Collectors.reducing(BigDecimal.ZERO,x->BigDecimal.valueOf(x.getGrossAmount()),BigDecimal::add));
                            var totalQty=receiptprint.getSalesItems().stream().filter(b->b.getId().compareTo(itemcode)==0 )//&& getPromoId(b)==p.promoId
                                .collect(Collectors.reducing(BigDecimal.ZERO,x->BigDecimal.valueOf(x.getQuantity()),BigDecimal::add));
                            var discountAmount=totalPaymentGrossAmount.subtract(totalAmountWithoutReceiptDiscount);
                            return new com.trc.ccopromo.models.receipt.PromoItem(itemcode, anyItem.get().getDescription(), totalPaymentGrossAmount.doubleValue(),discountAmount.doubleValue(), totalQty.doubleValue());
                        }).collect(Collectors.toList());
                        
                       ArrayList<PromoItem> products=new ArrayList<PromoItem>();
                       products.addAll(prods);
                       
                       return new com.trc.ccopromo.models.receipt.Promo(p.promoId,p.promoName,
                       products.stream().mapToDouble(a1->a1.getDiscount()).sum()
                       ,products);
                     }

                } catch (JsonMappingException e) {
                    return null;
                    // e.printStackTrace();
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    return null;
                }
            }
            )
            .filter(a->a!=null)
            .collect(Collectors.toList())
            );

            
            
            String couponsField=getTransactiontAdditionalField(receiptprint,com.trc.ccopromo.models.Constants.TRC_COUPONS);
            if(couponsField!=null)
            {   
                ObjectMapper m = new ObjectMapper();
                // Coupons coupons=new Coupons(promos.coupons);
                try {
                    Coupons coupons= m.readValue(couponsField,Coupons.class);
                    for (Coupon coupon : coupons.coupons) {
                        logger.info(coupon.code);
                        
                    }
                    

                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // setTransactionAdditionalField(receipt,com.trc.ccopromo.models.Constants.TRC_COUPONS,m.writeValueAsString(coupons));

                // coupons.si
            }
            
            


            for (SalesItemDTO saleitem : receiptprint.getSalesItems()) {
                var promoid=saleitem.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
                if(promoid!=null)
                {
                    var promoId=promoid.getValue();
                    if(promoId!=null)
                      if(!promoId.isEmpty())
                      {
                          var p=promos.stream().filter(a->promoId.compareTo(String.valueOf(a.getPromoId()))==0).findFirst();
                          if(!p.isEmpty())
                          {
                            saleitem.addAdditionalField(new AdditionalFieldDTO("promoname", "trcpromo", p.get().getPromoName()));
                          }
                      }
                }
                
            }
            rootMap.put("trcPromos", promos);
            
            
            // for (AdditionalFieldDTO addField : receiptprint.getAdditionalFields()) {
            //     if(addField.getFieldName().startsWith("Promo:")){
            //         var promo=mapper.readValue(addField.getValue(),StoredPromo.class);
            //         promos.add(0, null);

            //     }
            // }
        }
    }


    
}
