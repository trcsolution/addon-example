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
            
            handleEvent:  (event) =>  {
                
                // if (event.type === 'LOGIN_DONE') {
                //     this.pluginService.backendPluginEvent('TRC_PROMO_ADDON_INIT', {});
                // }
                // else
                // if (event.type === 'TRC_PROMO_ADDON_INIT') {
                //     this.TRC_PROMO_SET_URL=event.getPayload()
                //     // alert(event.getPayload());
                // }
                
                // return false;
                
                // if (event.getType() != 'HIDE_MESSAGE_BOX' 
                // && event.getType() != 'KEYBOARD_SHOWN' 
                // && event.getType() != 'KEYBOARD_HIDDEN' 
                // && event.getType() != 'HIDDE_KEYBOARD' 
                // && event.getType() != 'SHOW_KEYBOARD' 
                // ) {
                //     alert(JSON.stringify(event));

                //     // if (event.getType() == 'SALESITEM_ADD' && event.getSource() !== this) {

                //         // alert("1111");

                //         // event.onFailCallBack("11111");
                // // onProcessCallBack
                //     // event.onSuccessCallBack("2222");
                //     // return true;

                //     // }
                
                // }
                // return;
                if (event.getType() == 'TRC_PROMO_SCAN_BARCODE' ) 
                {
                     this.IGNORE=true;
                    // alert(JSON.stringify(event));
                    if(this.lastPayLoad!=null)
                    {
                      
                        this.eventBus.push('SALESITEM_ADD', this.lastPayLoad);
                        this.lastPayLoad=null;
                    }
                    return true;
                }
                else
                if (event.getType() == 'SALESITEM_ADD') 
                {
                     if(!this.IGNORE)
                    {

                        this.lastPayLoad=event.payload;
                        this.pluginService.backendPluginEvent('TRC_PROMO_SCAN_BARCODE', 
                        event.payload
                        // {barcode:event.payload.materialId}
                        );
                        // this.IGNORE=false;
                        return true;
                    }
                    else
                        this.IGNORE=false;
                }
                return false;

                if (event.getType() == 'SALESITEM_ADD' ) 
                {
                    if(!this.POSTED)
                    {
                        alert("11");
                        // event.source=this;
                        this.POSTED=true;
                        this.eventBus.push('SALESITEM_ADD', event.payload);
                        return true;

                    }
                    else
                    {
                        alert("222");
                        this.POSTED=false;
                        return false;
                    }

                    // alert(JSON.stringify(event));

                    // if (event.getType() == 'SALESITEM_ADD' && event.getSource() !== this) {

                        //this.pluginService.backendPluginEvent('TRC_PROMO_SCAN_BARCODE', {barcode:event.payload.materialId});

                        

                    // }
                    // else
                    // {
                        
                    // }
                    // return false;


                }
                return false;
                if (event.getType() == 'SALESITEM_ADD' && event.getSource() !== this) {
                    
                    this.pluginService.backendPluginEvent('TRC_PROMO_SCAN_BARCODE', {barcode:event.payload.materialId});

                    this.eventBus.push('SALESITEM_ADD', event.payload);

                    return false;
                
                    try {

                        const payload = event.getPayload();
                        

                        // let barcode="";

                        // alert(JSON.stringify(event.payload.materialid));
                        // alert(JSON.stringify(event.payload.materialId));
                        const url=`https://64cebe6f0c01d81da3ef0b59.mockapi.io/api/v1/products`;
                        // const url=`${this.TRC_PROMO_SET_URL}/api/Promo/GetCoupon?barcode=${payload.materialId}`;
                        alert(url);
                        // return false;

                        (async () => {
                            // `${this.TRC_PROMO_SET_URL}/api/Promo/ScanCoupon`
                            // alert("11111");
                            // const response=await fetch(`https://64cebe6f0c01d81da3ef0b59.mockapi.io/api/v1/products`
                            const response=await fetch(url
                            // ,
                            // {
                            //     method: "POST", 
                            //     headers: {
                            //         "Content-Type": "application/json",
                            //       },
                            //     body: JSON.stringify({
                            //         BarCode:event.payload.materialId

                            //     })
                                
                            // }
                            );
                            // alert("2222");
                            // alert(JSON.stringify(response));
                            const json=await response.json();
                            const rslt=JSON.stringify(json);
                            alert(rslt);

                            // this.pluginService.backendPluginEvent('TRC_PROMO_COUPON_REDEEM', 
                            // // JSON.stringify(json)
                            // // json
                            // {barcode:payload['materialId']}
                            // );

                            // alert(JSON.stringify(json));
                          })();
                          alert('vvv');
                          delay(4000);
                          alert('ooo');
                          return true;
                          
                        //  alert(barcode);

                        // this.pluginService.backendPluginEvent('TRC_PROMO_COUPON_REDEEM', {barcode:payload['materialId']});
                        // return true;
                      } catch (err) {
                        alert(err)
                        // error handling   
                        // return false;
                      
                      }
                    // if (payload['materialId'].startsWith('*') && payload['materialId'].length === 10) {
                    //     //payload['price'] = '0.0';
                    //     // payload['materialId'] = 'A00001';
                    //     this.showInputPopup(payload);
                    //     return true;
                    // }

                }
                // else


                // if (event.getType() === 'TRC_APPLY_PROMOTIONS' && event.getSource() !== this) {
                //     // alert('22222');
                //     this.pluginService.backendPluginEvent('TRC_APPLY_PROMOTIONS', {});
                //     // this.eventBus.push('SHOW_MESSAGE_BOX', {
                //     //     type: 'question',
                //     //     message: 'Manual discounts might be overwritten. Continue?',
                //     //     buttons: [
                //     //         {
                //     //             content: 'OK',
                //     //             type: 'positive',
                //     //             callback: () => {
                //     //                 this.pluginService.backendPluginEvent('TRC_APPLY_PROMOTIONS', {});
                //     //             }
                //     //         },
                //     //         {
                //     //             content: 'Abort',
                //     //             type: 'negative',
                //     //             callback: () => {
                //     //                 // do nothing
                //     //             }
                //     //         }
                //     //     ]
                //     // });
                // } else if (event.getType() === 'TRC_CALCULATE_PROMO' && event.getSource() !== this) {
                    
                //     this.pluginService.backendPluginEvent('TRC_CALCULATE_PROMO', {});
                //     //alert(event.getPayload());
                // }
                else
                return false;
                // else if (event.getType() === 'TRC_SHOW_DISCOUNTS' && event.getSource() !== this) {
                    
                //     this.fetchAndShowTrcPromoDiscounts();
                // }
            }
        
        }, true);
    }

    fetchAndShowTrcPromoDiscounts() {
        this.fetchTrcPromoDiscounts().then((result) => {
            console.log('result', result);
            this.showTrcPromoDiscounts(result);
        })
    }

    fetchTrcPromoDiscounts() {
        return new Promise(resolve => {
            const connectionStore = this.pluginService.getContextInstance('connectionStore');
            const receiptStore = this.pluginService.getContextInstance('ReceiptStore');
            const receiptModel = receiptStore.getReceiptModel();
            const conn = connectionStore.getConnection('plugin');
            if (conn && receiptModel) {
                conn.pushCommand(new cco.Command('plugin', 'plugin.backChannel',
                    {
                        eventId: 'trcPromoInfo',
                        receiptId: receiptModel.id
                    },
                    {
                        eventId: 'trcPromoInfo',
                        receiptId: receiptModel.id
                    }).done((response) => {
                        resolve(response.payload.trcPromo);
                    }
                ));
            }
        })
    }

    showTrcPromoDiscounts(trcPromoResult) {
        let labelContent = '';

        for (const promo of trcPromoResult.promos) {
            labelContent += 'Promo: ' + promo.name + '\n';
            //labelContent += '- Type: '+promo.promoType + '\n';
            labelContent += '- Items: ' + promo.items + '\n';
            if (promo.count) {
                labelContent += '- Count: ' + promo.count + '\n';
            }
            labelContent += '- Discount: ' + promo.discount + '\n';
        }
        labelContent += '\n';
        if (trcPromoResult.discount) {
            labelContent += 'Total Discount: ' + trcPromoResult.discount + '\n';
        } else {
            labelContent += 'No Discount from Promoengine applied.' + '\n';
        }

        const lines = labelContent.split(/\r\n|\r|\n/);
        const labelVspan = Math.min(lines.length * 8, 100);
        const elements = [];
        elements.push({
            index: [0, 0],
            hspan: 100,
            vspan: labelVspan,
            content: {
                // this is our second LabelComponent
                component: cco.LabelComponent,
                props: {
                    text: labelContent,
                    class: 'tableCellLayout',
                    heightFactor: 1
                }
            }
        })
        this.eventBus.push('SHOW_GENERIC_POPUP', {
            componentConfig: {
                component: cco.GridLayoutComponent,
                props: {
                    overlappingElements: false,
                    elements: elements,
                    cols: 100,
                    rows: 100
                }
            },
            title: 'Applied Discounts',
            showKeyboardSwitchButton: false,
            keyboardType: null,
            relHeight: 0.9,
            relWidth: 0.9,
            hideDoneButton: true,
            hideCancelButton: true,
            centerButtons: [{
                content: 'OK',
                type: 'positive',
                id: 'overlayButtonDone',
                callback: (closeDialogFun) => {
                    closeDialogFun(true);
                    return true;
                }
            }]
        });
    }
};
