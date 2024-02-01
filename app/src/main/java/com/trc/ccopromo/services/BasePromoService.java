package com.trc.ccopromo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.dao.ReceiptManager;
import com.sap.scco.ap.pos.entity.AdditionalFieldEntity;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptPosService;
import com.sap.scco.ap.pos.service.SalesItemPosService;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.trc.ccopromo.TrcPromoAddon;
import com.trc.ccopromo.models.PromoResponse;

public class BasePromoService {
    protected TrcPromoAddon addon;
    protected WebPromoService webPromoService;
    protected Logger logger;
    protected CDBSession dbSession;

    protected ReceiptPosService receiptPosService;
    protected ReceiptManager receiptManager;
    protected CalculationPosService calculationPosService;
    protected SalesItemPosService salesItemPosService;
    public BasePromoService(TrcPromoAddon addon,CDBSession dbSessio)
    {
        this.addon=addon;
        webPromoService=new WebPromoService(addon);
        logger = LoggerFactory.getLogger(this.getClass());
        this.dbSession=dbSession==null?CDBSessionFactory.instance.createSession():dbSession;
        this.receiptPosService =ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptPosService.class,this.dbSession);
        this.receiptManager = new ReceiptManager(this.dbSession);
        this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,this.dbSession);
        this.salesItemPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(SalesItemPosService.class,dbSession);

    }
    public  Boolean IsDiscountableItem(SalesItemEntity salesItem)
    {
        return 
        // !salesItemPosService.isSalesItemInvalid(salesItem) && 
        !salesItemPosService.isSalesItemVoid(salesItem)
        && !salesItemPosService.isVoucherSalesItem(salesItem)
        && salesItem.getQuantity().compareTo(BigDecimal.ZERO)>0;
        // && salesItem.getDiscountElements().isEmpty()  
    }

    public Stream<SalesItemEntity> getSalesItems(ReceiptEntity receipt)
    {
        return receipt.getSalesItems().stream().filter(b-> IsDiscountableItem(b) );
    }
    public BigDecimal GetLineTotal(SalesItemEntity entry,Boolean includeDiscount)
        {
            if(com.trc.ccopromo.TrcPromoAddon.isUSTaxSystem)
                return includeDiscount?entry.getUnitNetAmount().multiply(entry.getQuantity()).subtract(entry.getDiscountNetAmount()).setScale(2,RoundingMode.HALF_UP)
                    :entry.getUnitNetAmount().multiply(entry.getQuantity()).setScale(2,RoundingMode.HALF_UP);
                else
                return includeDiscount?entry.getUnitGrossAmount().multiply(entry.getQuantity()).subtract(entry.getDiscountAmount()).setScale(2,RoundingMode.HALF_UP)
                    :entry.getUnitGrossAmount().multiply(entry.getQuantity()).setScale(2,RoundingMode.HALF_UP);
        }

    // public Stream SalesItemEntity
    public void ApplyDiscountAmount(SalesItemEntity salesItem,BigDecimal discount)
    {
        if(com.trc.ccopromo.TrcPromoAddon.isUSTaxSystem)
        {

            salesItem.setDiscountNetAmount(discount);
            // BigDecimal subTotal=salesItem.getUnitNetAmount().multiply(salesItem.getQuantity()).subtract(discount);
            //  BigDecimal total=subTotal.multiply(salesItem.getTaxRate().add(BigDecimal.valueOf(1)));//.setScale(2,RoundingMode.HALF_UP);
            //  salesItem.setGrossAmount(total);
            //  salesItem.setPaymentGrossAmount(total);
            // logger.info("--SUBTOTAL :"+subTotal.toString());
            // logger.info("--APPLYED DISCOUNT--"+discount.toString());
            // logger.info("--TOTAL :"+total.toString());
        }
        else
        {
            salesItem.setDiscountAmount(discount);
        }
    }
    public void SetLineDiscount(SalesItemEntity salesItem,BigDecimal discount){
        salesItem.setPercentageDiscount(false);
        ApplyDiscountAmount(salesItem,discount);
        if(discount.compareTo(BigDecimal.ZERO)==0)
        {
            salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.NONPROMO_PROMO_DISCOUNT_CODE);
            salesItem.setDiscountManuallyChanged(false);
        }
            else
        {
            salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
            salesItem.setDiscountManuallyChanged(true);
        }
        salesItem.setMarkChanged(true);
        salesItem.setItemDiscountChanged(true);
    }
    

    public void ResetSalesItems(final ReceiptEntity receipt)
    {
        receipt.getSalesItems().stream().filter(salesItem->
        IsDiscountableItem(salesItem) 
        && Misc.HasPromo(salesItem)
        ).forEach(salesItem->
                {
                    Misc.ClearPromo(salesItem,true);
                    if(salesItem.getDiscountElements().isEmpty() )
                    //  if(_addon.transactionState.getDiscountsource(salesItem.getKey())!=DiscountSource.Manualy)
                        salesItem.setPaymentGrossAmountWithoutReceiptDiscount(GetLineTotal(salesItem, false).setScale(2,RoundingMode.HALF_UP));
                        SetLineDiscount(salesItem,BigDecimal.ZERO);
                });
    }
    public  void ApplyPromoDiscountsToTransaction(PromoResponse promoResp,ReceiptEntity receipt,BigDecimal headerDiscountPercent)
        {
            var promoDiscounts=promoResp.itemDiscounts.stream().collect(Collectors.groupingBy(a->a.promoId,Collectors.summingDouble(a->a.discount)));
                promoDiscounts.keySet().forEach(a->
                    {
                        //promoResp.itemDiscounts.
                        List<String> items=promoResp.itemDiscounts.stream().filter(b->b.promoId==a.intValue()).map(b->b.itemCode).collect(Collectors.toList());
                        ApplyPromoDiscount(receipt,items,a,BigDecimal.valueOf(promoDiscounts.get(a)),headerDiscountPercent);
                    });
        }
        protected void setInitiallyPromo(SalesItemEntity salesItem,String PromoId)
        {
            Misc.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.IS_PROMOITEM,PromoId);
        }
        protected String getInitiallyPromo(SalesItemEntity salesItem)
        {
            var rslt=Misc.getAdditionalField(salesItem,com.trc.ccopromo.models.Constants.IS_PROMOITEM);
            return rslt==null?"":rslt;
        }

        protected void setIsDiscountedPromoItem(SalesItemEntity salesItem,Boolean isDiscounted)
        {
            Misc.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.IS_DISCOUNTED,String.valueOf(isDiscounted));
        }
        protected Boolean getIsDiscountedPromoItem(SalesItemEntity salesItem)
        {
            var rslt=Misc.getAdditionalField(salesItem,com.trc.ccopromo.models.Constants.IS_DISCOUNTED);
            return rslt==null?false:Boolean.valueOf(rslt);
        }



        // protected Boolean IsItemInitiallyPromo(SalesItemEntity salesItem)
        // {
        //     String value=Misc.getAdditionalField(salesItem,com.trc.ccopromo.models.Constants.IS_PROMOITEM);
        //     if(value==null)
        //         return false;
        //         else
        //         return Boolean.parseBoolean(value);
        // }
        

        public boolean HasCoupon(SalesItemEntity salesItem)
        {
            if (salesItem.getDiscountElements()==null)
             return false;
             else
             return !salesItem.getDiscountElements().isEmpty();
        }

        public boolean IsManualDiscounted(SalesItemEntity salesItem)
        {
            String value=Misc.getAdditionalField(salesItem,com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE);
            if(value==null)
                return false;
                else
                return value==com.trc.ccopromo.models.Constants.DISCOUNT_SOURCE_MANUAL;
        }

        protected String getPromoId(SalesItemEntity salesItem)
        {
            return Misc.getAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID);
        }

        protected void MarkAsPromo(SalesItemEntity salesItem,String PromoId)
        {
            Misc.setAdditionalField(salesItem,com.trc.ccopromo.models.Constants.PROMO_ID,PromoId);
        }
        protected void ApplyPromoDiscount(ReceiptEntity receipt,List<String> items,int PromoId,BigDecimal _discount,BigDecimal headerDiscountPercent) {
        
            var totalAmount=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId()))
                .collect(Collectors.reducing(BigDecimal.ZERO,x->GetLineTotal(x, false),BigDecimal::add));

                // .mapToDouble(a->a.getUnitGrossAmount().multiply(a.getQuantity())
                // .doubleValue()).sum();
                
            var customer=receipt.getBusinessPartner();
            final BigDecimal customerDiscount=(customer==null?BigDecimal.ZERO:customer.getDiscountPercentage());

            //final BigDecimal customerDiscount=(customer==null?BigDecimal.ZERO:customer.getDiscountPercentage()).add(headerDiscountPercent);

            final BigDecimal discount=_discount;
            long itemCount=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId())).count();
            var salesItems=receipt.getSalesItems().stream().filter(a->this.IsDiscountableItem(a) && items.contains(a.getId()));
            final BigDecimal[] restDisc= {_discount};
            final BigDecimal[] hdrDisctDisc= {BigDecimal.ZERO};
            final long[] itemPos= {0};
            // final BigDecimal[] calcDisc= {BigDecimal.valueOf(0)};
            // for (int i = 0; i < salesItems.count(); i++) {
            //     var salesItem=salesItems.;
            // }
            
            
            salesItems.forEach(salesItem->
            {
                
                BigDecimal tineTotal=GetLineTotal(salesItem, false);
                BigDecimal k=BigDecimal.valueOf(tineTotal.doubleValue()/totalAmount.doubleValue());
                 
                salesItem.setPaymentGrossAmount(tineTotal);
                var linediscount=k.multiply(discount);
                if(customerDiscount.compareTo(BigDecimal.ZERO)>0)
                {
                    linediscount=linediscount.add(
                        tineTotal.subtract(linediscount).multiply(customerDiscount).divide(BigDecimal.valueOf(100))
                    );
                }
                if(headerDiscountPercent.compareTo(BigDecimal.ZERO)>0)
                {
                    linediscount=linediscount.add(
                        tineTotal.subtract(linediscount).multiply(headerDiscountPercent).divide(BigDecimal.valueOf(100))
                    );
                }
                if(!IsManualDiscounted(salesItem) && !HasCoupon(salesItem))
                {
                    // restDisc[0]=restDisc[0].subtract(

                    //     linediscount.subtract(linediscount.setScale(2,RoundingMode.HALF_UP))
                    // );
                    // if(linediscount.compareTo(linediscount.setScale(2,RoundingMode.HALF_UP))!=0)
                    //     hdrDisctDisc[0]=hdrDisctDisc[0].add(linediscount.subtract(linediscount.setScale(2,RoundingMode.HALF_UP)));
                   linediscount=linediscount.setScale(2,RoundingMode.HALF_EVEN);

                    itemPos[0]++;
                    if(itemPos[0]==itemCount)
                    {
                        linediscount=restDisc[0];
                    }
                    
                    
                    restDisc[0]=restDisc[0].subtract(linediscount);

                    SetLineDiscount(salesItem,linediscount);

                    BigDecimal vTotal=salesItem.getGrossAmount();
                    logger.info("GROSS:"+vTotal.toString());
                    salesItem.setUnitPriceChanged(true);
                    salesItem.setItemDiscountChanged(true);
                    Misc.AddNote(salesItem,  "Promo:"+String.valueOf(PromoId));
                    MarkAsPromo(salesItem,Integer.toString(PromoId));
                    // salesItem.setPaymentGrossAmountWithoutReceiptDiscount(BigDecimal.valueOf(3));
                    // salesItem.setPaymentNetAmount(BigDecimal.valueOf(3));
                    // salesItem.setPaymentNetAmountWithoutReceiptDiscount(BigDecimal.valueOf(4));
                    
                }
            });

            
            // if(hdrDisctDisc[0].compareTo(BigDecimal.ZERO)!=0)
            // {
            //     receipt.setPercentageDiscount(false);
            //     receipt.setDiscountNetAmount(hdrDisctDisc[0]);
            // }

            // receipt.setDiscountAmount(BigDecimal.valueOf(0.01));
    
        }

        


        public static void setTransactionAdditionalField(ReceiptEntity receipt, String key, String value) {
            AdditionalFieldEntity additionalField2 = receipt.getAdditionalField(key);
            if (additionalField2 == null) {
                if (value == null)
                    return;
                additionalField2 = new AdditionalFieldEntity();
                receipt.addAdditionalField(additionalField2);
            }
            additionalField2.setFieldName(key);
            additionalField2.setGroupName(com.trc.ccopromo.models.Constants.PROMO_GROUP);
            if(value==null)
            additionalField2.setValue("");
                else
            additionalField2.setValue(value);
        }

        public static String getTransactionAdditionalField(ReceiptEntity receipt, String key) {
            AdditionalFieldEntity additionalField2 = receipt.getAdditionalField(key);
            if (additionalField2 == null) {
                    return null;
            }
            return additionalField2.getValue();
            
        }
        
    
}
