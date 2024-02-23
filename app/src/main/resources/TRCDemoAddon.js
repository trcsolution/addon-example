function delay(milliseconds) {
    const startTime = Date.now();
    while (Date.now() - startTime < milliseconds) {
      // Wait and do nothing
    }
  }

Plugin.PromotionsEnginePlugin = class PromotionsEnginePlugin {
    constructor(pluginService, eventBus, receiptStore, userStore) {
        console.log('PromotionsEnginePlugin', pluginService, eventBus);
        this.pluginService = pluginService;
        this.eventBus = eventBus;
        this.receiptStore = receiptStore;
        this.userStore = userStore;
        this.TRC_PROMO_SET_URL="INIT";
        this.IGNORE=false;
        this.lastPayLoad=null;
        this.init();
    }


    init() {
        //Handle customer barcodes
        this.eventBus.subscribe({

            plugin: "TRCPromo",
            handleEvent: (event) => {
                if (event.getType() == 'SALESITEM_ADD' && event.getSource() !== this) {
                    alert(payload['materialId'])

                }
                else

                if (event.getType() === 'MY_CUSTOMEVENTT' && event.getSource() !== this) {
                     alert('MY_CUSTOMEVENTT');
                     //call anothe backend event
                     this.pluginService.backendPluginEvent('MY_BACKEND_EVENT', {barcode:event.payload.materialId});
                    //is you want call logout
                     this.eventBus.push(‘LOGOUT’);
                    
                } 
                          return true;
                          
           
                }

            }
        
        , true);
    }

    

    
};
