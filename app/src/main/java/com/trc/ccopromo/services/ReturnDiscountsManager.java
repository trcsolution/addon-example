package com.trc.ccopromo.services;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.entity.discountrule.DiscountType;

public class ReturnDiscountsManager {
    
    private ReturnService returnService;
    public ReturnDiscountsManager(ReturnService returnService){
        this.returnService=returnService;
    }
    boolean isCustomerDiscount(ReceiptEntity receipt)
    {
        var customer=receipt.getBusinessPartner();
        return customer==null?false:customer.getDiscountPercentage().compareTo(BigDecimal.ZERO)>0;
    }
    BigDecimal getCustomerDiscountPercentage(ReceiptEntity receipt)
    {
        var customer=receipt.getBusinessPartner();
        if(customer==null)
            return BigDecimal.ZERO;
        return customer.getDiscountPercentage();
    }
    boolean isHeaderLevelCoupons(ReceiptEntity receipt)
    {
        return receipt.getCouponAssignments().stream().filter(ca->
            {
                if(ca.getCoupon()!=null)
                  if(ca.getCoupon().getCouponItem()!=null)
                    if(ca.getCoupon().getCouponItem().getDiscountRules()!=null)
                    if(!ca.getCoupon().getCouponItem().getDiscountRules().isEmpty())
                        return ca.getCoupon().getCouponItem().getDiscountRules().stream().filter(a->a.isDiscountLevelOnHeader() && a.getDiscountType().compareTo(DiscountType.PERCENTAGE)==0).findAny().isPresent();
                        //     ca.getCoupon().getCouponItem().getDiscountRules().stream().filter(a->a.isDiscountLevelOnHeader()).forEach(rule->{
                        // if(rule.getDiscountType().name().compareTo("PERCENTAGE")==0)
                return false;
            }
            ).findAny().isPresent();
    }
    BigDecimal getHeaderLevenCouponDiscountPercentage(ReceiptEntity receipt)
    {
        return receipt.getCouponAssignments().stream().filter(ca->
            {
                if(ca.getCoupon()!=null)
                  if(ca.getCoupon().getCouponItem()!=null)
                    if(ca.getCoupon().getCouponItem().getDiscountRules()!=null)
                    if(!ca.getCoupon().getCouponItem().getDiscountRules().isEmpty())
                        return ca.getCoupon().getCouponItem().getDiscountRules().stream().filter(a->a.isDiscountLevelOnHeader() && a.getDiscountType().compareTo(DiscountType.PERCENTAGE)==0).findAny().isPresent();
                return false;
            }
            ).collect(
                Collectors.reducing(BigDecimal.ZERO,ca->
                {
                    if(ca.getCoupon()!=null)
                  if(ca.getCoupon().getCouponItem()!=null)
                    if(ca.getCoupon().getCouponItem().getDiscountRules()!=null)
                    if(!ca.getCoupon().getCouponItem().getDiscountRules().isEmpty())
                        return ca.getCoupon().getCouponItem().getDiscountRules().stream().filter(a->a.isDiscountLevelOnHeader() && a.getDiscountType().compareTo(DiscountType.PERCENTAGE)==0).
                            collect(Collectors.reducing(BigDecimal.ZERO,x->x.getDiscount(),BigDecimal::add));
                    return BigDecimal.ZERO;
                }
                ,BigDecimal::add)
            );
    }
    BigDecimal headerDiscountPercentage=BigDecimal.ZERO;
    public void KeepHeaderCouponDiscountPercentage(ReceiptEntity receipt)
    {
        this.headerDiscountPercentage=getHeaderLevenCouponDiscountPercentage(receipt);
    }

    public void ApplyHeaderCouponDiscountPercentage(SalesItemEntity entry)
    {   if(headerDiscountPercentage.compareTo(BigDecimal.ZERO)!=0)
        {
            BigDecimal discount=entry.getDiscountAmount();
            var customerDiscountAmount=this.returnService.GetLineTotal(entry, true).multiply(headerDiscountPercentage).divide(BigDecimal.valueOf(100));
            discount=discount.add(customerDiscountAmount);
            returnService.SetLineDiscount(entry, discount);
        }
    }

    public void ApplyCustomerDiscount(SalesItemEntity entry)
    {
        BigDecimal discount=entry.getDiscountAmount();
        BigDecimal customerDiscountPercentage=getCustomerDiscountPercentage(entry.getReceipt());
        if(customerDiscountPercentage.compareTo(BigDecimal.ZERO)>0)
        {
            var customerDiscountAmount=this.returnService.GetLineTotal(entry, true).multiply(customerDiscountPercentage).divide(BigDecimal.valueOf(100));
            discount=discount.add(customerDiscountAmount);
            returnService.SetLineDiscount(entry, discount);
        }
        
    }

}
