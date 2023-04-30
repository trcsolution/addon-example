Plugin.PromotionsEnginePlugin = class PromotionsEnginePlugin {
    constructor(pluginService, eventBus, receiptStore, userStore) {
        console.log('PromotionsEnginePlugin', pluginService, eventBus);
        this.pluginService = pluginService;
        this.eventBus = eventBus;
        this.receiptStore = receiptStore;
        this.userStore = userStore;
        this.init();
    }

    init() {
        //Handle customer barcodes
        this.eventBus.subscribe({
            handleEvent: (event) => {

                if (event.getType() === 'TRC_APPLY_PROMOTIONS' && event.getSource() !== this) {
                    // alert('22222');
                    this.pluginService.backendPluginEvent('TRC_APPLY_PROMOTIONS', {});
                    // this.eventBus.push('SHOW_MESSAGE_BOX', {
                    //     type: 'question',
                    //     message: 'Manual discounts might be overwritten. Continue?',
                    //     buttons: [
                    //         {
                    //             content: 'OK',
                    //             type: 'positive',
                    //             callback: () => {
                    //                 this.pluginService.backendPluginEvent('TRC_APPLY_PROMOTIONS', {});
                    //             }
                    //         },
                    //         {
                    //             content: 'Abort',
                    //             type: 'negative',
                    //             callback: () => {
                    //                 // do nothing
                    //             }
                    //         }
                    //     ]
                    // });
                } else if (event.getType() === 'TRC_CALCULATE_PROMO' && event.getSource() !== this) {
                    
                    this.pluginService.backendPluginEvent('TRC_CALCULATE_PROMO', {});
                    //alert(event.getPayload());
                }
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
