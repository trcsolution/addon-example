package com.trc.ccopromo;
import java.util.HashMap;
import java.util.Map;
import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.annotation.ListenToExit;
import com.sap.scco.ap.pos.dto.ReceiptPrintDTO;
import com.sap.scco.ap.pos.service.component.listener.ReceiptChangeListener;
public class TRCDemoAddon extends BasePlugin implements ReceiptChangeListener {
    

    @Override
    public String getId() {
        return "TRCDemoAddon";
    }

    @Override
    public String getName() {
        return "TRC Demo Addon";
    }
    

    @Override
    public String getVersion() {
        return "1.1.1";
    } 
    @Override
    public boolean persistPropertiesToDB() {
        return true;
    }
    

    @Override
    public Map<String, String> getPluginPropertyConfig() {
        Map<String, String> propConfig = new HashMap<>();
        return propConfig;
    }
    @Override
    public void startup() {
        
        super.startup();
    }
    
    
    
    public void onReceiptPost(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {

        // try this point for your code 

        
    }
    

    @ListenToExit(exitName = "BasePrintJobBuilder.mergeTemplateWithData")
    public void mergeTemplateWithData(Object caller, Object[] args) {
        // This is a receipt class
        var receipt=(ReceiptPrintDTO)args[2];

    }

    

    


    
}
