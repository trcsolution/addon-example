package com.trc.ccopromo.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.returnreceipt.ReturnReceiptObject;
import com.trc.ccopromo.TrcPromoAddon;

public class ReturnService extends BasePromoService {
    public ReturnService(TrcPromoAddon addon,CDBSession dbSession){
        super(addon, dbSession);
    }

    static Map<String, Double> m_promos;
    static Map<String, Map<String, List<SalesItemEntity>>> m_itemPromoMap;

        

    
        public void InitReturn(ReceiptEntity actualOriginalReceipt)
        {
                m_promos=this.getSalesItems(actualOriginalReceipt).filter(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)!=null)
                .collect(Collectors.groupingBy(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue(),Collectors.summingDouble(a->
                a.getUnitGrossAmount().multiply(a.getQuantity()).subtract(a.getDiscountAmount()).setScale(2,RoundingMode.HALF_UP).doubleValue()
            )));

                    m_itemPromoMap=this.getSalesItems(actualOriginalReceipt).filter(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID)!=null)
            .collect(Collectors.groupingBy(a->a.getId(),
                Collectors.groupingBy(a->a.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID).getValue())
                ));

        }

        List<com.trc.ccopromo.models.storedpromo.StoredPromo> getPromotionsFromAdditionalItms(ReceiptEntity receipt)
        {   
            if(receipt.getAdditionalFields()==null)
              return new ArrayList<com.trc.ccopromo.models.storedpromo.StoredPromo>();
              else
            //   new List<com.trc.ccopromo.models.storedpromo.StoredPromo>();
            return receipt.getAdditionalFields().stream().filter(a->a.getFieldName().startsWith("Promo:")).map(a->
                    {
                        try {
                            ObjectMapper mapper=new ObjectMapper();
                            return mapper.readValue(a.getValue(),  com.trc.ccopromo.models.storedpromo.StoredPromo.class);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    ).
                    collect(Collectors.toList()).stream().filter(a->a!=null).collect(Collectors.toList());
        }

        com.trc.ccopromo.models.PromoResponse getTransactionPromoDiscounts(ReceiptEntity receipt,List<com.trc.ccopromo.models.storedpromo.StoredPromo> usedpromos) throws IOException, InterruptedException, URISyntaxException
        {
            var request=Misc.MakePromoRequest(receipt, null);
            request.promotions=usedpromos;
            var response=webPromoService.PostCalculationRequest(request);

            ObjectMapper m = new ObjectMapper();
            return m.readValue(response, com.trc.ccopromo.models.PromoResponse.class);
        }


        public void ItemForReturn(ReturnReceiptObject returnReciept,boolean isStartReturn) throws IOException, InterruptedException, URISyntaxException
        {
            ReceiptEntity targetReceipt = returnReciept.getIndividualItemsReceipt();
            ReceiptEntity sourceReceipt = returnReciept.getSourceReceipt();
            ReceiptEntity actualOriginalReceipt = Misc.LoadReceipt(returnReciept.getSourceReceipt().getId());
            //Get Promotions used for Original transaction
            if(isStartReturn)
                InitReturn(actualOriginalReceipt);
            var promotions=getPromotionsFromAdditionalItms(actualOriginalReceipt);
            //calculate promo discounts
            var reminingDescounts=getTransactionPromoDiscounts(sourceReceipt,promotions);
            //Apply remining discount to the left side
            ResetSalesItems(sourceReceipt);
                ApplyPromoDiscountsToTransaction(reminingDescounts, sourceReceipt);
            //  var promoDiscount=new BigDecimal(reminingDescounts.discount);
        }

        public void moveReturnedReceiptToCurrentReceipt(ReceiptEntity sourcereceipt,ReceiptEntity targetReceipt,boolean isWholeReceipt,BigDecimal totalReminingAmount)
        {
            int i=0;
            if(isWholeReceipt)
            {
                        // var originalReceipt=transactionlogic.LoadReceipt(TransactionId);
                
                        for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
                            var targetEntry=targetReceipt.getSalesItems().get(i++);
            
                        // var promoId=targetEntry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
                            //var promoId=entry.getAdditionalField(com.trc.ccopromo.models.Constants.PROMO_ID);
                            // if(promoId==null)
                            //     continue;
                            
                            targetEntry.setReferenceSalesItem(null);
                            targetEntry.setDiscountAmount(BigDecimal.ZERO);
                            targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(entry.getDiscountAmount()) .divide(entry.getQuantity()));
            
                        }
                        if(sourcereceipt.getDiscountAmount().compareTo(BigDecimal.ZERO)>0)
                        {
                            //totalReminingAmount
                            var _total=BigDecimal.valueOf(sourcereceipt.getSalesItems().stream().mapToDouble(a->
                            a.getUnitGrossAmount()
                            .multiply(a.getQuantity())
                            .subtract(a.getDiscountAmount())
                            .doubleValue()
                            ).sum());

                            // targetReceipt.setDiscountAmount(sourcereceipt.getTotalGrossAmount().subtract(totalReminingAmount).negate());
                            targetReceipt.setDiscountAmount(_total.subtract(totalReminingAmount).negate());

                            // targetReceipt.setDiscountAmount(sourcereceipt.getDiscountAmount().negate());

                            targetReceipt.setPercentageDiscount(false);
                            
                        }
                        

            }
            else
            {
                    for (SalesItemEntity entry : sourcereceipt.getSalesItems().stream().filter(a->!a.getStatus().equals("3")).collect(Collectors.toList())) {
                        var targetEntry=targetReceipt.getSalesItems().get(i++);
                        var discountStr=entry.getAdditionalField("TRC_Discount");
                        if(discountStr!=null)
                            if(discountStr.getValue()!=null)
                            {
                                var discount=new BigDecimal(discountStr.getValue());
                                targetEntry.setReferenceSalesItem(null);
                                targetEntry.setDiscountAmount(BigDecimal.ZERO);
                                targetEntry.setUnitGrossAmount(entry.getGrossAmount().subtract(discount).divide(entry.getQuantity()));
                                // targetEntry.setUnitPriceChanged(true);
                                continue;
                            }
                            targetEntry.setGrossAmount(entry.getGrossAmount());
                            targetEntry.setUnitPriceChanged(true);
                    }
            }
        }


}
