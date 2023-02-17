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
