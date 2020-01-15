package com.emotte.hss.core.order.controller;

import com.emotte.common.utils.response.BaseResponse;
import com.emotte.common.utils.util.ResultUtils;
import com.emotte.hss.common.vo.order.*;
import com.emotte.hss.core.common.RedisConstant;
import com.emotte.hss.core.common.ResultEnum;
import com.emotte.hss.core.framework.exception.BusinessException;
import com.emotte.hss.core.framework.log.Log;
import com.emotte.hss.core.order.service.OrderReconciliationService;
import com.emotte.hss.core.order.service.OrderTransactionService;
import com.emotte.hss.core.order.service.TransactionDetailService;
import com.emotte.hss.core.pay.service.TransactionPayRecordService;
import com.emotte.hss.core.utils.RedisLock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.emotte.common.utils.util.ResultUtils.success;

/**
 * 订单交易表
 *
 * @author kk
 * @date 2019-11-08 18:31:26
 */
@Slf4j
@RestController
@RequestMapping("order-transaction")
@Api(value = "order-transaction", tags = "订单交易")
public class OrderTransactionController {
    @Autowired
    private OrderTransactionService orderTransactionService;
    @Autowired
    private TransactionDetailService transactionDetailService;
    @Autowired
    private TransactionPayRecordService transactionPayRecordService;
    @Autowired
    private OrderReconciliationService orderReconciliationService;
    @Autowired
    private RedisLock redisLock;

    @Log(value = "创建订单交易表记录")
    @RequestMapping("/createAccount")
    @ApiOperation(value="创建订单交易表记录",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse<OrderTransactionRespVO> createAccount(@RequestBody @Validated OrderTransactionReqVO orderTransactionReqVO) {
        try {
            //加redis锁
            if (redisLock.lock(RedisConstant.ORDER_TRANSACTION_LOCK + orderTransactionReqVO.getOrderId())) {
                return success(orderTransactionService.insertOrderTransaction(orderTransactionReqVO));
            }
            return ResultUtils.success();
        } catch (BusinessException be){
            return ResultUtils.fail(be.getCode(),be.getMsg());
        } catch (Exception e) {
            log.error("ContractCheckRecordController.checkPass() inner error，错误信息：{}", orderTransactionReqVO.getOrderId(), e);
            return ResultUtils.fail(ResultEnum.FAIL);
        } finally {
            redisLock.unlock(RedisConstant.ORDER_TRANSACTION_LOCK + orderTransactionReqVO.getOrderId());
        }
    }

    @Log(value = "查询订单交易表记录")
    @RequestMapping("/queryAcconts")
    @ApiOperation(value="查询订单交易表记录",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse<List<OrderTransactionRespVO>> queryAcconts(@RequestBody @Validated OrderTransactionReqVO orderTransactionReqVO) {
        return success(orderTransactionService.queryOrderTransaction(orderTransactionReqVO));
    }

    @Log(value = "根据id查询订单交易表记录")
    @RequestMapping("/queryAccontById/{orderTransactionId}")
    @ApiOperation(value="根据id查询订单交易表记录",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易表ID", name = "orderTransactionId", required = true, dataType = "Long")
    public BaseResponse<OrderTransactionRespVO> queryAccontById(@PathVariable("orderTransactionId") Long orderTransactionId) {
        return success(orderTransactionService.queryOrderTransactionById(orderTransactionId));
    }

    @Log(value = "查询待支付订单交易记录（查待支付账单）")
    @RequestMapping("/queryAccontPay")
    @ApiOperation(value="查询待支付订单交易记录（查待支付账单）",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse<OrderTransactionRespVO> queryAccontPay(@RequestBody @Validated OrderTransactionReqVO orderTransactionReqVO) {
        return success(orderTransactionService.loadOrderTransaction(orderTransactionReqVO));
    }

    @Log(value = "查询订单交易明细记录（查询账单明细）")
    @RequestMapping("/queryAccontDetails")
    @ApiOperation(value="查询订单交易明细记录（查询账单明细）",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse<List<TransactionDetailRespVO>> queryAccontDetails(@RequestBody @Validated OrderTransactionReqVO orderTransactionReqVO) {
        return success(transactionDetailService.queryTransactionDetail(orderTransactionReqVO));
    }

    @Log(value = "查询本次账单的支付记录")
    @RequestMapping("/queryTransactionPayRecord")
    @ApiOperation(value="查询本次账单的支付记录",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse<List<TransactionPayRecordRespVO>> queryTransactionPayRecord(@RequestBody @Validated OrderTransactionReqVO orderTransactionReqVO) {
        return success(transactionPayRecordService.queryTransactionPayRecordListPay(orderTransactionReqVO));
    }

    @Log(value = "修改账单")
    @RequestMapping("/updateOrderTransaction")
    @ApiOperation(value="修改账单",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单交易VO", name = "orderTransactionReqVO", required = true, dataType = "OrderTransactionReqVO")
    public BaseResponse updateOrderTransaction(@RequestBody OrderTransactionReqVO orderTransactionReqVO) {
        orderTransactionService.updateOrderTransaction(orderTransactionReqVO);
        return success();
    }

    @Log(value = "订单流水记录")
    @RequestMapping("/loadOrderReconciliation/{orderId}")
    @ApiOperation(value="订单流水记录",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单ID", name = "orderId", required = true, dataType = "Long")
    public BaseResponse<OrderReconciliationVO> loadOrderReconciliation(@PathVariable("orderId") Long orderId) {
        return success(orderReconciliationService.load(orderId));
    }

    @Log(value = "订单已支付金额(交易支付记录表)")
    @RequestMapping("/loadPaid/{orderId}")
    @ApiOperation(value="订单已支付金额(交易支付记录表)",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单ID", name = "orderId", required = true, dataType = "Long")
    public BaseResponse<Long> loadPaid(@PathVariable("orderId") Long orderId) {
        return success(orderTransactionService.loadPaid(orderId));
    }

    @Log(value = "订单已消费金额(交易支付记录表)")
    @RequestMapping("/loadConsumed/{orderId}")
    @ApiOperation(value="订单已消费金额(交易支付记录表)",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单ID", name = "orderId", required = true, dataType = "Long")
    public BaseResponse<Long> loadConsumed(@PathVariable("orderId") Long orderId) {
        return success(orderTransactionService.loadConsumed(orderId));
    }

    @Log(value = "退款金额对账")
    @RequestMapping("/refundAmountCheck/{orderId}")
    @ApiOperation(value="退款金额对账",httpMethod = "POST", response = BaseResponse.class)
    @ApiImplicitParam(value = "订单ID", name = "orderId", required = true, dataType = "Long")
    public BaseResponse<RefundAmountCheckRespVO> refundAmountCheck(@PathVariable("orderId") Long orderId) {
        return success(orderTransactionService.refundAmountCheck(orderId));
    }
}
