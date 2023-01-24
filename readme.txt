manifest.attributes["pluginName"] = "sasa"
    manifest.attributes["cashdeskPOSPlugin"] = "com.trc.app3.App"
    manifest.attributes["cashDeskVersions"] = "com.trc.app3.App"
    manifest.attributes["version"] = "2.13.4"
    manifest.attributes["pluginVersion"] = "1.0"


    BB00404  0.80
    BB00064  1.35

    2 for 1.5   
select pmt.TransactionPaymentKey
,tx.BusinessDate as [Date]
,PmtPaymentType.Id as PaymentId
,RtlStore.Id as StoreId
,CusCustomer.Id as CustomerId
,RTRIM(case when len(rtrim(ltrim(CusCustomer.FirstName)))=0 then '' else CusCustomer.FirstName+' ' end+ISNULL(CusCustomer.LastName,'')) as CustomerName
,pmt.Amount
,cast(0 as bit) as IsSelected
--,PmtPaymentType.Type as PaymentType
,case when PmtPaymentType.U_CASH=1 then 0 else PmtPaymentType.Type end as PaymentType
from TrxTransactionPayment pmt inner join TrxTransaction tx on pmt.TransactionKey=tx.TransactionKey
inner join CusCustomer on tx.CustomerKey=CusCustomer.CustomerKey
inner join RtlStore on tx.StoreKey=RtlStore.StoreKey
inner join PmtPaymentType on pmt.PaymentTypeKey=PmtPaymentType.PaymentTypeKey
where not exists(select 1 from U_TRC_SAPPayments u 
inner join U_TRC_SAPPaymentsHeader hu on u.U_PaymentsHeaderKey=hu.TRC_SAPPaymentsHeaderKey
where u.U_TransactionPaymentKey=pmt.TransactionPaymentKey and ISNULL(hu.U_Canceled,0)=0)
and RtlStore.SiteId in (select SiteId from CfgSiteInformation)
and convert(varchar(8),tx.BusinessDate,112) between @DateFrom and @DateTo
and pmt.IsVoided=0 and tx.IsVoided=0 and tx.IsDeleted=0 and tx.IsSuspended=0
--and convert(varchar(8),tx.Created,112) between @DateFrom and @DateTo
