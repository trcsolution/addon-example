package com.trc.ccopromo.services;

import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trc.ccopromo.TrcPromoAddon;
import com.trc.ccopromo.WebRequest;
import com.trc.ccopromo.models.CouponRequest;
import com.trc.ccopromo.models.CouponResponse;
import com.trc.ccopromo.models.storedpromo.StoredPromos;
import com.trc.ccopromo.models.transaction.post.PostTransactionRequest;

public class WebPromoService {
    private TrcPromoAddon addon;
    public WebPromoService(TrcPromoAddon addon)
    {
        this.addon=addon;
    }
    public String PostCalculationRequest(com.trc.ccopromo.models.PromoRequest promorequest) throws IOException, InterruptedException, URISyntaxException
    {
        var request = new WebRequest(addon.getPluginConfig());
        var response=request.Post("/api/Promo/Calculate", promorequest,true);
        return response;
    }
    public StoredPromos PostTransaction(PostTransactionRequest requestObj) throws IOException, InterruptedException, URISyntaxException
    {
        var request = new WebRequest(addon.getPluginConfig());
        String json = request.Post("/api/Promo/Save", requestObj,false);
        var mapper = new ObjectMapper();
        StoredPromos promos=mapper.readValue("{\"p\":"+json+"}", com.trc.ccopromo.models.storedpromo.StoredPromos.class);
        return promos;
    }
    public com.trc.ccopromo.models.CouponResponse GetCoupon(String barcode) throws IOException, InterruptedException, URISyntaxException
    {
        var request = new WebRequest(addon.getPluginConfig());
        String json = request.Post("/api/Promo/ScanCoupon", new CouponRequest(barcode),false);
        if(request.statusCode==404)//not found
         {
            var rslt=new CouponResponse();
            rslt.barCode=barcode;
            return rslt;
         }

        var mapper = new ObjectMapper();
        com.trc.ccopromo.models.CouponResponse couponResp=mapper.readValue(json, com.trc.ccopromo.models.CouponResponse.class);
        return couponResp;
    }
        
}
