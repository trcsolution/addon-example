// package com.trc.ccopromo.models.session;

// import java.math.BigDecimal;
// import java.util.ArrayList;

// import com.sap.scco.ap.pos.entity.SalesItemEntity;
// import com.trc.ccopromo.TransactionTools;

// public class TransactionState {
//     public TransactionState()
//     {
//         items=new ArrayList<Entry>();
//     }
//     public Boolean HasDiscountChanged(SalesItemEntity salesItem)
//     {
//         var rslt=items.stream().filter(a->a.Key.equals(salesItem.getKey())).findAny();
//           if(!rslt.isPresent() && TransactionTools.getPromoId(salesItem)>0)
//             return true;

//         return rslt.isPresent()?rslt.get().discount.compareTo(salesItem.getDiscountAmount())!=0:Boolean.FALSE;
//     }
//     public DiscountSource getDiscountsource(String salesItemKey)
//     {
//         var rslt=items.stream().filter(a->a.Key.equals(salesItemKey)).findAny(); 
//         return rslt.isPresent()?rslt.get().discountsource:DiscountSource.NONE;
//     }
//     public void setDiscountsource(String salesItemKey,DiscountSource newSource,BigDecimal newDiscount)
//     {
//         var rslt=items.stream().filter(a->a.Key.equals(salesItemKey)).findAny(); 
//         if(rslt.isPresent())
//         {
//             rslt.get().discountsource=newSource;
//             rslt.get().discount=newDiscount;
//         }
//         else
//         items.add(new Entry(salesItemKey, newSource, newDiscount));

//     }
//     public void Reset()
//     {
//         items=new ArrayList<Entry>();
//     }
//     public void ResetItem(String salesItemKey)
//     {
//         if(this.items.stream().anyMatch(a->a.Key==salesItemKey))
//         {
//             this.items.remove(
//                 this.items.stream().filter(a->a.Key==salesItemKey).findFirst().get()    
//             );
//         }
//     }
//     public ArrayList<Entry> items;
// }
