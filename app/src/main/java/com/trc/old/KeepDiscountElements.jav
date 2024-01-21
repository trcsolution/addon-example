// KeepCouponsDiscounts(receipt);
        // try {
        //     ClearCoupons(receipt);
        //     ApplyPromoDiscounts(receipt);
        //     ReapplyCouponsDiscounts(receipt);

        // } catch (Exception e) {

        // }

        public static class CouponDiscount {
            public CouponDiscount(){
                this.discountAmount = BigDecimal.ZERO;
                this.isPercentageDiscount = false;
                this.discountAmount=BigDecimal.ZERO;
            }
            public CouponDiscount(
                    BigDecimal discountAmount,
                    BigDecimal discountPercentage,
                    Boolean isPercentageDiscount) {
                this.discountAmount = discountAmount;
                this.isPercentageDiscount = isPercentageDiscount;
    
                if (this.isPercentageDiscount)
                    this.discountPercentage = discountPercentage;
                else
                    this.discountPercentage = BigDecimal.ZERO;
            }
            public BigDecimal discountAmount;
            public BigDecimal discountPercentage;
            public Boolean isPercentageDiscount;
        }
    
    
        public void ClearCoupons(ReceiptEntity receipt) {
       
            for (SalesItemEntity salesItem : receipt.getSalesItems()) {
                if(salesItemPosService.isSalesItemInvalid(salesItem) 
                || salesItemPosService.isSalesItemVoid(salesItem)
                || salesItemPosService.isVoucherSalesItem(salesItem)
                || salesItem.getDiscountElements().isEmpty())
                    continue;
                TransactionTools.ClearCoupon(salesItem);
    
            }
    
            // TransactionTools.getSalesItems(receipt)
            //     .filter(a -> !a.getDiscountElements().isEmpty())
            //     .forEach(salesItem->{
            //         TransactionTools.ClearCoupon(salesItem);
            //     });
                receipt.getCouponAssignments().clear();
                receipt.getDiscountElements().clear();
        }
        public void KeepCouponsDiscounts(ReceiptEntity receipt) {
            // couponsDiscounts = 
            for (int i = 0; i < receipt.getSalesItems().size(); i++) {
                SalesItemEntity salesItem=receipt.getSalesItems().get(i);
                logger.info("1 - "+salesItem.getId());
    
    
            // }
            // for (SalesItemEntity salesItem : receipt.getSalesItems()) {
    
                if(salesItemPosService.isSalesItemInvalid(salesItem) 
                || salesItemPosService.isSalesItemVoid(salesItem)
                || salesItemPosService.isVoucherSalesItem(salesItem)
                || salesItem.getDiscountElements().isEmpty())
                    continue;
                    logger.info("2 - "+salesItem.getId());
                    
    
                    var couponDiscount=new CouponDiscount(salesItem.getDiscountAmount(),
                    salesItem.getDiscountElements().get(0).getCouponAssignment().getCoupon().getCouponItem()
                            .getDiscountRules().isEmpty()
                                    ? BigDecimal.ZERO
                                    : salesItem.getDiscountElements().get(0).getCouponAssignment().getCoupon()
                                            .getCouponItem().getDiscountRules().get(0).getDiscount(),
                                            salesItem.getDiscountElements().get(0).getCouponAssignment().getCoupon().getCouponItem()
                            .getDiscountRules().isEmpty()
                                    ? false
                                    : salesItem.getDiscountElements().get(0).getCouponAssignment().getCoupon()
                                            .getCouponItem().getDiscountRules().get(0)
                                            .getDiscountType() == DiscountType.PERCENTAGE);
    
            var mapper = new ObjectMapper();
            try {
                String json=mapper.writeValueAsString(couponDiscount);
    
                TransactionTools.setAdditionalField(salesItem, com.trc.ccopromo.models.Constants.COUPON_DISCOUNT, json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    
            }
            // TransactionTools.getSalesItems(receipt)
            //     .filter(a -> !a.getDiscountElements().isEmpty())
            //     .forEach(a->
            //     {
            //         var couponDiscount=new CouponDiscount(a.getDiscountAmount(),
            //                 a.getDiscountElements().get(0).getCouponAssignment().getCoupon().getCouponItem()
            //                         .getDiscountRules().isEmpty()
            //                                 ? BigDecimal.ZERO
            //                                 : a.getDiscountElements().get(0).getCouponAssignment().getCoupon()
            //                                         .getCouponItem().getDiscountRules().get(0).getDiscount(),
            //                 a.getDiscountElements().get(0).getCouponAssignment().getCoupon().getCouponItem()
            //                         .getDiscountRules().isEmpty()
            //                                 ? false
            //                                 : a.getDiscountElements().get(0).getCouponAssignment().getCoupon()
            //                                         .getCouponItem().getDiscountRules().get(0)
            //                                         .getDiscountType() == DiscountType.PERCENTAGE);
    
            //         var mapper = new ObjectMapper();
            //         try {
            //             String json=mapper.writeValueAsString(couponDiscount);
            //             TransactionTools.setAdditionalField(a, com.trc.ccopromo.models.Constants.COUPON_DISCOUNT, json);
            //         } catch (JsonProcessingException e) {
            //             e.printStackTrace();
            //         }
            //     });
        }
    
        public void ApplyPromoDiscounts(ReceiptEntity receipt) {
    
    
        }


        public void ReapplyCouponsDiscounts(ReceiptEntity receipt) throws JsonMappingException, JsonProcessingException {
            for (SalesItemEntity salesItem : receipt.getSalesItems()) {
    
                if(salesItemPosService.isSalesItemInvalid(salesItem) 
                || salesItemPosService.isSalesItemVoid(salesItem)
                || salesItemPosService.isVoucherSalesItem(salesItem)
                )
                    continue;
    
                    
    
                var discount=salesItem.getAdditionalField(com.trc.ccopromo.models.Constants.COUPON_DISCOUNT);
                    if(discount!=null)
                     if(discount.getValue()!=null)
                     if(!discount.getValue().isEmpty())
                     {
                        var mapper = new ObjectMapper();
                        CouponDiscount disc=mapper.readValue(discount.getValue(),CouponDiscount.class);
                        
    // logger.info(salesItem.getId());
    
    
                        // salesItem.setDiscountAmount(BigDecimal.valueOf(3));
                        // // salesItem.setDiscountManuallyChanged(true);
                        // salesItem.setMarkChanged(true);
                        // salesItem.setUnitPriceChanged(true);
    
                        salesItem.setPercentageDiscount(false);
                        Misc.ApplyDiscountAmount(salesItem,disc.discountAmount);
                        // if(com.trc.ccopromo.TrcPromoAddon.isUSTaxSystem)
                        //     salesItem.setDiscountNetAmount(disc.discountAmount);
                        // else
                        //     salesItem.setDiscountAmount(disc.discountAmount);
            salesItem.setDiscountPurposeCode(com.trc.ccopromo.models.Constants.PROMO_DISCOUNT_CODE);
            salesItem.setMarkChanged(true);
            salesItem.setItemDiscountChanged(true);
            salesItem.setDiscountManuallyChanged(true);
            salesItem.setUnitPriceChanged(true);
    
    
    
                        // TransactionTools.ClearCoupon(item);
                        // CouponDiscount disc=mapper.readValue(discount.getValue(),CouponDiscount.class);
    
                     }
            }
    
            // TransactionTools.getSalesItems(receipt)
            //     .filter(a -> !a.getDiscountElements().isEmpty())
            //     .forEach(item->{
            //         var discount=item.getAdditionalField(com.trc.ccopromo.models.Constants.COUPON_DISCOUNT);
            //         if(discount!=null)
            //          if(discount.getValue()!=null)
            //          if(!discount.getValue().isEmpty())
            //          {
            //             var mapper = new ObjectMapper();
            //             // TransactionTools.ClearCoupon(item);
            //             // CouponDiscount disc=mapper.readValue(discount.getValue(),CouponDiscount.class);
    
            //          }
            //     });
        }
    