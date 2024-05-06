//package com.qunar.flight.octopus.qmall.finance.service.impl;
//
//import com.google.common.base.Preconditions;
//import com.qunar.flight.baggage.api.bean.BaggageOrderFinanceInfo;
//import com.qunar.flight.baggage.api.service.IBaggageFinanceService;
//import com.qunar.flight.jy.charles.business.collect.request.AuxiliaryQueryRequest;
//import com.qunar.flight.jy.charles.business.common.enums.common.BusinessLine;
//import com.qunar.flight.jy.charles.business.common.enums.common.BusinessScope;
//import com.qunar.flight.jy.charles.business.common.enums.common.CheckType;
//import com.qunar.flight.jy.charles.business.common.enums.common.DataType;
//import com.qunar.flight.jy.charles.business.common.enums.supermarketing.RecordModeEnum;
//import com.qunar.flight.jy.charles.business.common.model.auxiliary.AdcAccountInfo;
//import com.qunar.flight.jy.charles.business.common.model.auxiliary.AdcFeeType;
//import com.qunar.flight.jy.charles.business.common.model.auxiliary.AdcSupplierFee;
//import com.qunar.flight.octopus.doublewrite.dao.RefundInfoRecordDao;
//import com.qunar.flight.octopus.doublewrite.qdb.CustomRuleMethod;
//import com.qunar.flight.octopus.doublewrite.tool.SqlRouterContext;
//import com.qunar.flight.octopus.qmall.bean.OrderAttrEnum;
//import com.qunar.flight.octopus.qmall.bean.vippackage.FreeOrPlusVIPPrivilegeDTO;
//import com.qunar.flight.octopus.qmall.dao.OrderKvDao;
//import com.qunar.flight.octopus.qmall.finance.FinanceUtils;
//import com.qunar.flight.octopus.qmall.finance.bean.FinanceAgentInfoDTO;
//import com.qunar.flight.octopus.qmall.finance.bean.OrderInfoSnapshot;
//import com.qunar.flight.octopus.qmall.finance.bean.ProductInfoSnapshot;
//import com.qunar.flight.octopus.qmall.finance.common.FinanceConstant;
//import com.qunar.flight.octopus.qmall.finance.handler.TransferAndCheckTypeConverter;
//import com.qunar.flight.octopus.qmall.finance.model.*;
//import com.qunar.flight.octopus.qmall.finance.wrapper.CouponChangeRecordDaoWrapper;
//import com.qunar.flight.octopus.qmall.finance.wrapper.FinanceQmallWrapper;
//import com.qunar.flight.octopus.qmall.finance.wrapper.FinanceSnapshotDaoWrapper;
//import com.qunar.flight.octopus.qmall.finance.wrapper.TransferRecordDaoWrapper;
//import com.qunar.flight.octopus.qmall.model.RefundInfoRecord;
//import com.qunar.flight.octopus.qmall.model.TtsPayFlow;
//import com.qunar.flight.octopus.qmall.qconfig.CouponBackReceiveConfig;
//import com.qunar.flight.octopus.qmall.qconfig.QConfigHandler;
//import com.qunar.flight.octopus.qmall.tts.service.trade.TtsPayService;
//import com.qunar.flight.octopus.qmall.util.HttpClientUtil;
//import com.qunar.flight.octopus.refactor.adapters.persistence.mysql.payment.TtsPayFlowAdapter;
//import com.qunar.flight.octopus.refactor.application.flight.bean.dto.refundcallback.CouponRefundDetail;
//import com.qunar.flight.octopus.refactor.application.flight.bean.dto.refundcallback.CouponRefundInfo;
//import com.qunar.flight.qmall.Enum.BusinessLineEnum;
//import com.qunar.flight.qmall.Enum.CallbackTypeEnum;
//import com.qunar.flight.qmall.bean.CouponStatusEnum;
//import com.qunar.flight.qmall.bean.OrderSource;
//import com.qunar.flight.qmall.common.constant.AgentSettleMentTypeEnum;
//import com.qunar.flight.qmall.product.utils.ProductTypeVOUtils;
//import com.qunar.flight.qmonitor.QMonitor;
//import com.qunar.flight.ttsi.ifd.common.api.jdk7.pojo.result.RpcResult;
//import com.qunar.ucenter.client.utils.JsonUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.MapUtils;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.assertj.core.util.Lists;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.qunar.flight.octopus.qmall.finance.FinanceUtils.parseUniqueId;
//
///**
// * @Auther: song.huai
// * @Date: 2021/2/9 18:24
// * @Description:
// */
//@Slf4j
//@Service
//public class FinanceFlightDataService {
//
//    private static final int REFUND_CALL_BACK = 2;
//    @Resource
//    private FinanceSnapshotDaoWrapper financeSnapshotDaoWrapper;
//    @Resource
//    private TransferRecordDaoWrapper transferRecordDaoWrapper;
//    @Resource
//    private CouponChangeRecordDaoWrapper couponChangeRecordDaoWrapper;
//    @Resource
//    private CouponBackReceiveConfig couponBackReceiveConfig;
//    @Resource
//    private FinanceQmallWrapper financeQmallWrapper;
//    @Resource
//    private IBaggageFinanceService iBaggageFinanceService;
//    @Resource
//    private OrderKvDao orderKvDao;
//    @Resource
//    private RefundInfoRecordDao refundInfoRecordDao;
//
//    @Resource
//    private TtsPayFlowAdapter ttsPayFlowAdapter;
//    @Resource
//    private TtsPayService ttsPayService;
//
//    private AdcAccountInfo buildAdcAccountInfo(AuxiliaryQueryRequest auxiliaryQueryRequest, FinanceSnapshot financeSnapshot,
//                                               CouponChangeRecord couponChangeRecord, TransferRecord transferRecord,
//                                               boolean abolishVoucher, Boolean isCarType) {
//
//        String inventoryId = parseUniqueId(couponChangeRecord.getCouponUniqueId())[1];
//
//        AdcAccountInfo adcAccountInfo = new AdcAccountInfo();
//        OrderInfoSnapshot orderInfoSnapshot = financeSnapshot.getOrderInfoSnapshot();
//        ProductInfoSnapshot productInfoSnapshot = financeSnapshot.getProductInfoSnapshot();
//
//        adcAccountInfo.setNewData(!orderInfoSnapshot.isOldData());
//        adcAccountInfo.setOrderNo(financeSnapshot.getQmallOrderNo());
//        adcAccountInfo.setSupplyItemNo(couponChangeRecord.getCouponUniqueId());
//        adcAccountInfo.setBizOrderNo(financeSnapshot.getFlightOrderNo());
//        adcAccountInfo.setSaleChannel(orderInfoSnapshot.getSaleChannel());
//        adcAccountInfo.setSaleSource(orderInfoSnapshot.getSaleSource());
//        adcAccountInfo.setSaleForm(orderInfoSnapshot.getSaleForm());
//        adcAccountInfo.setSaleType(productInfoSnapshot.getSaleType());
//        adcAccountInfo.setSettleMentType(productInfoSnapshot.getSettleMentType());
//        adcAccountInfo.setPacking(orderInfoSnapshot.isPacking());
//        adcAccountInfo.setPackCode(orderInfoSnapshot.getPackCode());
//        adcAccountInfo.setPackName(orderInfoSnapshot.getPackName());
//        if (orderInfoSnapshot.isPacking() && StringUtils.isBlank(orderInfoSnapshot.getPackName())) {
//            QMonitor.recordOne("orderInfoSnapshot_packName_blank");
//            //修复兼容历史数据包名称为空问题
//            Map<String, String> orderKvMap = financeQmallWrapper.getOrderKvMap(financeSnapshot.getQmallOrderNo());
//            adcAccountInfo.setPackCode(orderKvMap.get(OrderAttrEnum.PACKING_CODE.getCode()));
//            adcAccountInfo.setPackName(orderKvMap.get(OrderAttrEnum.PACKING_NAME.getCode()));
//        }
//        adcAccountInfo.setCreateTime(orderInfoSnapshot.getOrderCreateTime());
//        adcAccountInfo.setFinishTime(couponChangeRecord.getCreateTime());
//        adcAccountInfo.setProductCode(financeSnapshot.getPid());
//        adcAccountInfo.setProductName(productInfoSnapshot.getProductName());
//        adcAccountInfo.setProductTypeCode(productInfoSnapshot.getProductTypeCode());
//        adcAccountInfo.setProductTypeName(productInfoSnapshot.getProductTypeName());
//        adcAccountInfo.setProductClassify(productInfoSnapshot.getProductClassify());
//        adcAccountInfo.setSupplierCode(productInfoSnapshot.getAbbrName());
//        adcAccountInfo.setSupplierName(productInfoSnapshot.getAgentName());
//        adcAccountInfo.setSupplierShortName(productInfoSnapshot.getShortName());
//        adcAccountInfo.setSupplierPartner(productInfoSnapshot.getAgentPartner());
//        //实时查下
//        FinanceAgentInfoDTO financeAgentInfoDTO = financeQmallWrapper.getAgentInfo(productInfoSnapshot.getAbbrName());
//        if (Objects.nonNull(financeAgentInfoDTO)) {
//            adcAccountInfo.setSupplierCode(financeAgentInfoDTO.getAbbrName());
//            adcAccountInfo.setSupplierName(financeAgentInfoDTO.getAgentName());
//            adcAccountInfo.setSupplierShortName(financeAgentInfoDTO.getShortName());
//            adcAccountInfo.setSupplierPartner(financeAgentInfoDTO.getAgentPartner());
//        }
//        adcAccountInfo.setDepAirport(orderInfoSnapshot.getDepCode());
//        adcAccountInfo.setArrAirport(orderInfoSnapshot.getArrCode());
//        adcAccountInfo.setCommission(productInfoSnapshot.getIncomePrice());
//        adcAccountInfo.setTransactionNo(StringUtils.EMPTY);
//        Integer checkType = auxiliaryQueryRequest.getCheckType();
//        BigDecimal businessPrice = BigDecimal.ZERO;
//        if (transferRecord != null && BigDecimal.ZERO.compareTo(transferRecord.getTransferPrice()) != 0) {
//            businessPrice = transferRecord.getTransferPrice();
//            //0元传转账流水号并没有在支付中心发生过，所以非0元的才传转账流水
//            adcAccountInfo.setTransactionNo(transferRecord.getTransferNo());
//            if (TransferTypeEnum.WITHHOLD_REFUND.equals(transferRecord.getTransferType())) {
//                //如果是代扣退款，大龙那边要的流水号是代扣支付的流水号
//                adcAccountInfo.setTransactionNo(transferRecord.getPayId());
//            }
//        }
//        BigDecimal providerPrice = financeSnapshot.getOrderSettlePrice();
//        if (isSettle(checkType)) {
//            providerPrice = businessPrice;
//        }
//        if (Objects.equals(CheckType.A_DK_REFUND.code, checkType)
//                && financeSnapshot.getProductInfoSnapshot().getSettleMentType() == AgentSettleMentTypeEnum.PLAT_FORM.getCode()) {
//            //平台类型的代扣退款的底价，用实际底价，兼容部分退场景
//            String settlementRefundNo = TransferNoDTO.buildTransferNo(couponChangeRecord.getCouponUniqueId(), TransferTypeEnum.SETTLEMENT_REFUND.getValue());
//            TransferRecord settlementRefundRecord = transferRecordDaoWrapper.selectByOrderNoAndTransferNo(financeSnapshot.getQmallOrderNo(), settlementRefundNo);
//            if (Objects.nonNull(settlementRefundRecord)) {
//                providerPrice = settlementRefundRecord.getTransferPrice();
//            } else {
//                QMonitor.recordOne("finance_dk_refund_not_have_settlementRefundRecord");
//                log.info("qmall_finance, dk_refund_not_have_settlementRefundRecord, {}", settlementRefundNo);
//            }
//        }
//        if(isCarType){
//            adcAccountInfo.setBusinessPrice(financeSnapshot.getOrderInfoSnapshot().getTotalPrice());
//            adcAccountInfo.setProviderPrice(financeSnapshot.getOrderInfoSnapshot().getTotalPrice());
//        }else {
//            adcAccountInfo.setBusinessPrice(businessPrice);
//            adcAccountInfo.setProviderPrice(providerPrice);
//        }
//
//        if (productInfoSnapshot.getIncomePrice().compareTo(BigDecimal.ZERO) != 0 &&
//                StringUtils.equals(productInfoSnapshot.getCashBackType(), "after_back")) {
//            AdcSupplierFee adcSupplierFee = new AdcSupplierFee();
//            adcSupplierFee.setFeeType(AdcFeeType.REBATE.getCode());
//            adcSupplierFee.setAmount(productInfoSnapshot.getIncomePrice());
//            adcAccountInfo.setSupplierFeeList(Lists.newArrayList(adcSupplierFee));
//        }
//        //处理赠券后返数据
//        processBackReceive(financeSnapshot, inventoryId, adcAccountInfo, productInfoSnapshot, checkType, providerPrice);
//
//        adcAccountInfo.setOrderSource(financeSnapshot.getOrderSourceFrom());
//        adcAccountInfo.setCouponCode(couponChangeRecord.getCouponCode());
//
//        adcAccountInfo.setCouponStatus(couponChangeRecord.getCouponStatus());
//        if (abolishVoucher && CouponStatusEnum.expired.getCode() == couponChangeRecord.getCouponStatus()) {
//            /*
//            特殊处理，表示废券的，因为辅营系统没废券状态，也不好加，就用过期状态加废券表里数据标识废券。
//            给大龙是又不能和过期状态一样，就单独加个值。后续如果加状态，可以把这个废券状态加到辅营系统里
//             */
//            adcAccountInfo.setCouponStatus(30);
//        }
//        adcAccountInfo.setValidateStartTime(couponChangeRecord.getValidateStartTime());
//        adcAccountInfo.setValidateEndTime(couponChangeRecord.getValidateEndTime());
//        adcAccountInfo.setBusinessLine(BusinessLine.FY.code());
//        //历史financeSnapshot.getBusinessLine()没存值，默认是0。这个地方做个判断，如果是0返回机票，否则返回financeSnapshot.getBusinessLine()
//        String businessSource = Objects.equals(financeSnapshot.getBusinessLine(), BusinessLineEnum.UNKNOWN.getCode()) ?
//                BusinessLineEnum.FLIGHT.name() : BusinessLineEnum.fromCode(financeSnapshot.getBusinessLine()).name();
//        adcAccountInfo.setBusinessSource(businessSource);
//        adcAccountInfo.setBusinessScope(BusinessScope.DOM.code());
//        adcAccountInfo.setCheckType(checkType);
//        adcAccountInfo.setDataType(DataType.OD.code());
//        adcAccountInfo.setBusiMode(0);
//        //行李额相关逻辑填充数据
//        processBaggage(auxiliaryQueryRequest, adcAccountInfo, financeSnapshot);
//        //超商相关逻辑
//        if (StringUtils.isNotBlank(orderInfoSnapshot.getSaleSource())
//                && QConfigHandler.getList("finance_super_mall_order_source", OrderSource.sale_first_supermall.name()).contains(orderInfoSnapshot.getSaleSource())) {
//            Map<String, String> orderKvMap = financeQmallWrapper.getOrderKvMap(financeSnapshot.getQmallOrderNo());
//            adcAccountInfo.setSuperMallNo(orderKvMap.get(OrderAttrEnum.SUPER_SALE_FIRST_ORDER.getCode()));
//        }
//
//        adcAccountInfo.setRecordMode(RecordModeEnum.NORMAL_MODE.code());
//        if (ProductTypeVOUtils.isTypeAndEnumNameMatch(productInfoSnapshot.getProductTypeCode(), "FLIGHT_PRESALE_VOUCHER")) {
//            //预售券逻辑，返回预售标识给大龙
//            adcAccountInfo.setRecordMode(RecordModeEnum.ADVANCE_MODE.code());
//        }
//        processReceiveType(adcAccountInfo,isCarType);
//        return adcAccountInfo;
//    }
//
//
//    /**
//     * 设置收款模式。
//     */
//    private void processReceiveType(AdcAccountInfo adcAccountInfo,Boolean carType){
//        if(carType){
//            adcAccountInfo.setReceiveType(1);
//            return ;
//        }
//        adcAccountInfo.setReceiveType(0);
//    }
//
//    private void processBaggage(AuxiliaryQueryRequest auxiliaryQueryRequest, AdcAccountInfo adcAccountInfo,
//                                FinanceSnapshot financeSnapshot) {
//        //不是行李额相关大类不处理
//        if (!FinanceUtils.isBaggageProductType(financeSnapshot.getProductInfoSnapshot().getProductTypeCode())) {
//            return;
//        }
//        adcAccountInfo.setBusiMode(1);
//        //不是结算逻辑，下面不处理
//        if (!isSettle(auxiliaryQueryRequest.getCheckType())) {
//            if (Objects.equals(CheckType.A_DK_PAYIN.code, auxiliaryQueryRequest.getCheckType())) {
//                adcAccountInfo.setBSideStatus(30);
//            }
//            if (Objects.equals(CheckType.A_DK_REFUND.code, auxiliaryQueryRequest.getCheckType())) {
//                adcAccountInfo.setBSideStatus(34);
//            }
//            return;
//        }
//        BaggageOrderFinanceInfo financeInfo = iBaggageFinanceService.queryBaggageFinanceData(auxiliaryQueryRequest.getTransactionNo());
//        if (Objects.isNull(financeInfo)) {
//            throw new IllegalArgumentException("查询到行李额财务信息失败");
//        }
//        adcAccountInfo.setProcurementMode(financeInfo.getProcurementMode());
//        adcAccountInfo.setProcurementAmount(financeInfo.getProcurementAmount());
//        adcAccountInfo.setPayMode(financeInfo.getPayMode());
//        adcAccountInfo.setBSideStatus(financeInfo.getBSiteStatus());
//        adcAccountInfo.setTransactionNo(financeInfo.getTransferNo());
//    }
//
//    /**
//     * 处理赠券后返数据逻辑
//     */
//    private void processBackReceive(FinanceSnapshot financeSnapshot, String inventoryId, AdcAccountInfo adcAccountInfo,
//                                    ProductInfoSnapshot productInfoSnapshot,
//                                    Integer checkType, BigDecimal providerPrice) {
//        adcAccountInfo.setBackReceive(productInfoSnapshot.getBackReceive());
//        adcAccountInfo.setBackReceiveAmount(productInfoSnapshot.getCouponBackActualPrice());
//        adcAccountInfo.setRecording(Objects.equals(productInfoSnapshot.getExternalAccountType(), ExternalAccountTypeEnum.RECORD.getValue()));
//
//        Set<String> memberInventorySet = productInfoSnapshot.getMemberBackReceiveInventoryIdSet();
//        if (CollectionUtils.isNotEmpty(memberInventorySet) && memberInventorySet.contains(inventoryId)) {
//            //主流程会员权益部分绑单case
//            adcAccountInfo.setBackReceive(FinanceConstant.MEMBER_IDENTIFICATION);
//            adcAccountInfo.setSaleType(SaleTypeEnum.GIFT.getValue());
//        }
//        if (FinanceConstant.MEMBER_IDENTIFICATION.equals(adcAccountInfo.getBackReceive())) {
//            //会员权益商品的后返应收金额，取最终结算转账的金额
//            adcAccountInfo.setBackReceiveAmount(providerPrice);
//        }
//        if (FinanceConstant.FISSION_IDENTIFICATION.equals(adcAccountInfo.getBackReceive())) {
//            //绑单赠券， 取最终结算转账的金额
//            adcAccountInfo.setBackReceiveAmount(providerPrice);
//        }
//        boolean recording = Objects.equals(couponBackReceiveConfig.getEat(adcAccountInfo.getBackReceive()), ExternalAccountTypeEnum.RECORD.getValue());
//        adcAccountInfo.setRecording(recording);
//        if (StringUtils.isNotBlank(adcAccountInfo.getBackReceive())) {
//            Integer recordNode = couponBackReceiveConfig.getRecordNode(adcAccountInfo.getBackReceive());
//            if (Objects.isNull(recordNode)) {
//                log.error("couponBackReceiveConfig_getRecordNode_null, qmallOrderNo: {}", financeSnapshot.getQmallOrderNo());
//                QMonitor.recordOne("couponBackReceiveConfig_getRecordNode_null");
//                return;
//            }
//            if (Objects.equals(recordNode, TransferTypeEnum.WITHHOLD_PAY.getValue())
//                    && !isDk(checkType)) {
//                //如果赠券应收对象不为空，并且记收节点是代扣，并且当前拉取的不是代扣支付或代扣退款数据，则记收金额为0，并且是否记收标识为false
//                adcAccountInfo.setBackReceiveAmount(BigDecimal.ZERO);
//                adcAccountInfo.setRecording(false);
//            }
//            if (Objects.equals(recordNode, TransferTypeEnum.SETTLEMENT_PAY.getValue())
//                    && !isSettle(checkType)) {
//                //如果赠券应收对象不为空，并且记收节点是结算，并且当前拉取的不是结算转账或结算退款数据，则记收金额为0，并且是否记收标识为false
//                adcAccountInfo.setBackReceiveAmount(BigDecimal.ZERO);
//                adcAccountInfo.setRecording(false);
//            }
//        }
//        // 处理付费会员和免费会员的区分 卡号为空 - 免费会员
//        if (adcAccountInfo.getBackReceive().equals(FinanceConstant.MEMBER_IDENTIFICATION)  ) {
//            //公共的酷狗需求需要把 免费的强制写成付费的
//            if (OrderSource.sale_first_supermall.name().equals(financeSnapshot.getOrderSourceFrom())) {
//                return;
//            }
//            String privilegeCode = orderKvDao.queryByOrderIdAndKey( financeSnapshot.getQmallOrderNo(), OrderAttrEnum.MEMBER_PRIVILEGE_CODE.getCode());
//            Optional<Boolean> qStar = queryIsPaidOrFreeVIP(privilegeCode);  // true为是付费会员
//            if(!qStar.orElse(true)) {
//                adcAccountInfo.setBackReceive(FinanceConstant.FREE_MEMBER_IDENTIFICATION);
//            }
//        }
//
//    }
//
//    /**
//     * 是否是代扣支付或代扣退款
//     */
//    private boolean isDk(Integer checkType) {
//        return Objects.equals(CheckType.A_DK_PAYIN.code, checkType) || Objects.equals(CheckType.A_DK_REFUND.code, checkType);
//    }
//
//    /**
//     * 是否是结算转账或结算退款
//     */
//    private boolean isSettle(Integer checkType) {
//        return Objects.equals(CheckType.A_SETT_PAYIN.code, checkType) || Objects.equals(CheckType.A_SETT_REFUND.code, checkType);
//    }
//
//    public RpcResult<List<AdcAccountInfo>> getFinanceData(AuxiliaryQueryRequest auxiliaryQueryRequest) {
//        return SqlRouterContext.runOnBusinessLineEnumContext(() -> {
//            String qmallOrderNo = auxiliaryQueryRequest.getOrderNo();
//            Integer transferType = TransferAndCheckTypeConverter.getTransferType(auxiliaryQueryRequest.getCheckType());
//            Preconditions.checkArgument(transferType != null, "未获得对应的转账类型");
//            String itemNo = auxiliaryQueryRequest.getSupplyItemNo();
//            String transferNo = auxiliaryQueryRequest.getTransactionNo();
//            if (StringUtils.isBlank(transferNo)) {
//                QMonitor.recordOne("finance_getFinanceData_transferNo_null");
//                transferNo = TransferNoDTO.buildTransferNo(itemNo, transferType);
//            }
//            String supplyItemNo = auxiliaryQueryRequest.getSupplyItemNo();
//            String inventoryIdStr = parseUniqueId(supplyItemNo)[1];
//            BigInteger inventoryId = new BigInteger(inventoryIdStr);
//            TransferRecord transferRecord = null;
//            FinanceSnapshot orderSnapshot = financeSnapshotDaoWrapper.selectByQmallOrderNo(qmallOrderNo);
//            Boolean isCarType = false;
//            Preconditions.checkArgument(orderSnapshot != null, "未查询到快照信息");
//            if (transferType != TransferTypeEnum.OTHER.getValue()) {
//                if(!isCarType){
//                    //OTHER类型没有转账流水
//                    transferRecord = transferRecordDaoWrapper.selectByOrderNoAndTransferNo(qmallOrderNo, transferNo);
//                    TtsPayFlow ttsPayFlow = ttsPayFlowAdapter.selectByOrderId(qmallOrderNo);
//                    // 去除辅营大账户
//                    if (Objects.isNull(transferRecord) && ttsPayFlowAdapter.isXSecondAccount(qmallOrderNo)) {
//                        log.info("finance_qmall_getFinanceData_build_transferRecord_when_is_null qmallOrderNo: {}, inventoryId: {}", qmallOrderNo, inventoryId);
//                        QMonitor.recordOne("finance_qmall_getFinanceData_build_transferRecord_when_is_null");
//                        transferRecord = this.buildTransferRecord(qmallOrderNo, transferType, inventoryId, ttsPayFlow);
//                    }
//                    if (Objects.isNull(transferRecord)) {
//                        log.info("finance_qmall_getFinanceData_transferRecord_null, {}, {}", qmallOrderNo, transferNo);
//                        QMonitor.recordOne("finance_qmall_getFinanceData_transferRecord_null");
//                        //如果是代扣支付，并且支付金额大于0，但是此时没查到transferRecord是有问题的，返回失败重试
//                        BigDecimal detailPrice;
//                        boolean validate = transferType == TransferTypeEnum.WITHHOLD_PAY.getValue()
//                                && Objects.nonNull(detailPrice = financeQmallWrapper.getOrderDetailPrice(qmallOrderNo, inventoryId))
//                                && detailPrice.compareTo(BigDecimal.ZERO) > 0;
//                        if (validate) {
//                            log.info("finance_qmall_getFinanceData_transferRecord_null_error, {}, {}", qmallOrderNo, transferNo);
//                            QMonitor.recordOne("finance_qmall_getFinanceData_transferRecord_null_error");
//                            return RpcResult.error("金额不为0，但未发生转账数据异常");
//                        }
//                    }
//                }else {
//                    log.info("finance_qmall_getFinanceData_transferRecord_car_message orderId:{}",orderSnapshot.getQmallOrderNo());
//                }
//            }
//            if (transferRecord != null && !TransferStatusEnum.SUCCESS.equals(transferRecord.getTransferStatus())) {
//                //需要重查，这里单独记一个监控
//                QMonitor.recordOne("transfer_record_fail_need_retry");
//                return RpcResult.error("转账失败需重试");
//            }
//
//            List<CouponChangeRecord> couponChangeRecords = couponChangeRecordDaoWrapper.selectByQmallOrderNoCouponId(qmallOrderNo, supplyItemNo);
//            Optional<CouponChangeRecord> optional = couponChangeRecords.stream()
//                    .filter(couponChangeRecord -> couponChangeRecord.getCouponStatus() == auxiliaryQueryRequest.getCouponStatus())
//                    .max(Comparator.comparing(CouponChangeRecord::getCreateTime));
//
//            CouponChangeRecord couponChangeRecord = null;
//            if (optional.isPresent()) {
//                couponChangeRecord = optional.get();
//            }
//            Preconditions.checkArgument(Objects.nonNull(couponChangeRecord), "未获取到券状态信息");
//            boolean abolishVoucher = financeQmallWrapper.isAbolishVoucher(qmallOrderNo, inventoryId);
//            AdcAccountInfo adcAccountInfo = buildAdcAccountInfo(auxiliaryQueryRequest, orderSnapshot,
//                    couponChangeRecord, transferRecord, abolishVoucher,isCarType);
//            return RpcResult.success(Lists.newArrayList(adcAccountInfo));
//        }, getBusinessLine(auxiliaryQueryRequest));
//    }
//
//
//    /**
//     * 获取业务线参数
//     */
//    private BusinessLineEnum getBusinessLine(AuxiliaryQueryRequest auxiliaryQueryRequest) {
//        BusinessLineEnum businessLineEnum = CustomRuleMethod.getBizLine(auxiliaryQueryRequest.getOrderNo());
//        if (Objects.nonNull(businessLineEnum)) {
//            return businessLineEnum;
//        }
//        return BusinessLineEnum.FLIGHT;
//    }
//
//    /**
//     * 用户VPI是否为付费查询(pp_u_qprivilege)
//     * @param privilegeCode
//     * @return
//     */
//    public Optional<Boolean> queryIsPaidOrFreeVIP(String privilegeCode) {
//        Boolean result;
//
//        String response = null;
//        try {
//            String getQueryParam = QConfigHandler.getProperty("privilege.paid_or_free_vip.url") + "?privilegeCode=" + privilegeCode;
//            response = HttpClientUtil.getRequest(getQueryParam, "query_is_paid_vip");
//            log.info("FinanceFlightDataService_queryIsPaidOrFreeVIP, result : {}", response);
//
//            FreeOrPlusVIPPrivilegeDTO freeOrPlusVIPPrivilegeDTO = JsonUtils.parseObject(response, FreeOrPlusVIPPrivilegeDTO.class);
//            result = freeOrPlusVIPPrivilegeDTO.getData().getQStar();
//        } catch (Exception exception) {
//            QMonitor.recordOne("query_is_paid_vip_failure");
//            log.error("Failed to query VIP privilege type for privilegeCode [{}], got response [{}]", privilegeCode, response, exception);
//            throw new RuntimeException(exception);
//        }
//
//        return Optional.of(result);
//    }
//
//    private TransferRecord buildTransferRecord(String qmallOrderNo, Integer transferType, BigInteger inventoryId, TtsPayFlow ttsPayFlow) {
//        TransferRecord transferRecord;
//        transferRecord = new TransferRecord();
//        if (Objects.nonNull(ttsPayFlow)) {
//            transferRecord.setQmallOrderNo(qmallOrderNo);
//            transferRecord.setCouponUniqueId(FinanceUtils.buildCouponUniqueId(qmallOrderNo, inventoryId.toString()));
//            transferRecord.setTransferNo(ttsPayFlow.getPayId());
//            if (transferType == TransferTypeEnum.WITHHOLD_PAY.getValue()) {
//                transferRecord.setTransferPrice(financeQmallWrapper.getOrderDetailPrice(qmallOrderNo, inventoryId));
//            }
//            if (transferType == TransferTypeEnum.WITHHOLD_REFUND.getValue()) {
//                transferRecord.setPayId(ttsPayFlow.getPayId());
//                transferRecord.setTransferPrice(this.getRefundAmount(ttsPayFlow.getBusineesOrderNo(), inventoryId));
//            }
//            transferRecord.setSourceAccount(ttsPayFlow.getPpmAccount());
//            transferRecord.setTargetAccount(ttsPayFlow.getPpmAccount());
//
//            transferRecord.setTransferType(TransferTypeEnum.getEnum(transferType));
//            transferRecord.setTransferStatus(TransferStatusEnum.SUCCESS);
//            Date now = new Date();
//            transferRecord.setTransferTime(now);
//            transferRecord.setUpdateTime(now);
//            transferRecord.setCreateTime(now);
//        }
//        if (QConfigHandler.getBoolean("FinanceFlightDataService_buildTransferRecord_finish", false)) {
//            log.info("FinanceFlightDataService_buildTransferRecord_finish qmallOrderNo: {}, transferRecord: {}", qmallOrderNo, JsonUtils.toJson(transferRecord));
//        }
//        return transferRecord;
//    }
//
//    public BigDecimal getRefundAmount(String businessOrderNo, BigInteger inventoryId) {
//        List<RefundInfoRecord> refundInfoRecordList = refundInfoRecordDao.selectByBusinessOrderNo(businessOrderNo);
//        List<CouponRefundDetail> couponRefundDetailList = refundInfoRecordList.stream()
//                .filter(refundInfoRecord -> refundInfoRecord.getRefundStage() == REFUND_CALL_BACK)
//                .map(refundInfoRecord -> com.qunar.flight.support.common.util.serialize.JsonUtils.deSerialize(refundInfoRecord.getCouponDetail(), CouponRefundDetail.class))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//        BigDecimal price = couponRefundDetailList.stream()
//                .map(CouponRefundDetail::getCouponInfoMap)
//                .filter(MapUtils::isNotEmpty)
//                .flatMap(map -> map.values().stream())
//                .filter(couponRefundInfo -> StringUtils.equals(couponRefundInfo.getCouponId(), inventoryId.toString()))
//                .findFirst()
//                .map(CouponRefundInfo::getRefundAmount)
//                .orElse(null);
//        if (Objects.isNull(price)) {
//            QMonitor.recordOne("FinanceFlightDataService_getRefundAmount_finish_refund_price_null");
//        }
//        return price;
//    }
//
//}
