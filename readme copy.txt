<#--  
<#if trcIssuedCoupons??>
<@solid_line/>  -->

<#--  Issued Coupons:
<#list trcIssuedCoupons as coupon>
${coupon.code}  ${coupon.discount} Eur
<@barcode  value="${coupon.code}"/>
</#list>
<@solid_line/>
</#if>
<#if trcRedeemCoupons??>
<@solid_line/>
Issued Coupons:
<#list trcRedeemCoupons as redcoupon>
${redcoupon.code}  ${redcoupon.discount} Eur
</#list>
<@solid_line/>

  -->
  </#if>


  <@printCoupons/>
<#macro printCoupons>
</#macro>





<#list trcIssuedCoupons as coupon>
${coupon.code}  ${coupon.discount} Eur
<@barcode  value="${coupon.code}"/>
</#list>
<@solid_line/>
</#if>
<#if trcRedeemCoupons??>
<@solid_line/>
Redeeming Coupons:
<#list trcRedeemCoupons as redcoupon>
${redcoupon.code}  ${redcoupon.discount} Eur
</#list>
<@solid_line/>

TREOXTLAGE