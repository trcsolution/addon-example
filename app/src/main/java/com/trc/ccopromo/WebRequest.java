package com.trc.ccopromo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander.Builder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.trc.ccopromo.models.PromoRequest;
import com.trc.ccopromo.models.PromoRequestItem;
import com.trc.ccopromo.models.PromoResponse;
import com.trc.ccopromo.models.transaction.post.Data;
import com.trc.ccopromo.models.transaction.post.Item;
import com.trc.ccopromo.models.transaction.post.PostTransactionRequest;

import org.slf4j.Logger;
import net.sf.json.JSONObject;
import org.slf4j.LoggerFactory;

public class WebRequest {

    private Logger logger = LoggerFactory.getLogger(TrcPromoAddon.class);

    private PluginConfig _config;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public WebRequest(PluginConfig config) {
        _config = config;
    }

    // ArrayList<Object> refPromos=null
    // public PromoResponse Request(ReceiptEntity receipt,ArrayList<Integer> refPromos) throws IOException, InterruptedException {
    //     PromoRequest request = new PromoRequest();
    //     request.setItems(new ArrayList<PromoRequestItem>(
    //             receipt.getSalesItems().stream().filter(a -> !a.getStatus().equals("3") &&  a.getMaterial() != null).map(item -> {
    //                 var itm = new PromoRequestItem();
    //                 itm.setItemCode(item.getId());
    //                 itm.setQty(item.getQuantity().intValue());
    //                 itm.setPrice(item.getUnitGrossAmount().doubleValue());
    //                 itm.setGroup(item.getMaterial().getArticleGroup().getId());
    //                 return itm;
    //             }).collect(Collectors.toList())));
    //     request.setTransactionNumber(receipt.getId());
    //     request.setRefPromos(refPromos);

    //     // var ss=JSONObject.fromObject(request).toString();
    //     logger.info("-------------- REQUEST BEGIN-----------");
    //     logger.info(JSONObject.fromObject(request).toString());
    //     logger.info("-------------- REQUEST END-----------");

    //     var builder=HttpRequest.newBuilder()
    //     .header("Content-Type", "application/json")
    //     .POST(HttpRequest.BodyPublishers.ofString(JSONObject.fromObject(request).toString()))
    //     .uri(URI.create(_config.getBaseUrl()+"/api/Promo/Calculate"))
    //     .setHeader("User-Agent", "Promotions engine plugin");

    //     if(_config.getSecure())
    //         builder=builder.header("Authorization","Bearer ".concat(_config.getAPIKey()));

        

    //     HttpRequest httpRequest = builder.build();
    //     // HttpRequest.newBuilder()
    //     //         .header("Content-Type", "application/json")
    //     //         .POST(HttpRequest.BodyPublishers.ofString(JSONObject.fromObject(request).toString()))
    //     //         .uri(URI.create(_config.getBaseUrl()))
    //     //         .setHeader("User-Agent", "Promotions engine plugin")
    //     //         //.header("Authorization","Bearer ".concat(_config.getAPIKey()))
    //     //         .build();

    //     HttpResponse<String> response = null;
    //     response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    //     ObjectMapper m = new ObjectMapper();
    //     PromoResponse resp = m.readValue(response.body(), PromoResponse.class);
        

    //     logger.info("-------------- RESPONCE BEGIN-----------");
    //     logger.info(response.body());
    //     logger.info("-------------- RESPONCE END-----------");

    //     return resp;
    // }
    public String Post(String url,Object data) throws IOException, InterruptedException 
    {
        String rslt=null;
        try {
            var mapper = new ObjectMapper();
            String json=mapper.writeValueAsString(data);
            var builder=HttpRequest.newBuilder()
             .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .uri(URI.create(_config.getBaseUrl()+url))
            .setHeader("User-Agent", "Promotions engine plugin");
            if(_config.getSecure())
                builder=builder.header("Authorization","Bearer ".concat(_config.getAPIKey()));
            HttpRequest httpRequest = builder.build();
            HttpResponse<String> response= httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            rslt=response.body();
            logger.info(response.body());
            logger.info(json);
            return rslt;
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rslt;
        
    }
    

}
