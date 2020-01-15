package com.emotte.hss.core.common;

/**
 * @program: hss-core
 * @description: redis
 * @author: Mr.Wang
 * @date: 2019-11-07 13:55
 **/
public class RedisConstant {
    public final static String EXPIRE_TIME = "20000";

    /**
     * 锁订单系统，key：ORDER_LOCK+"{orderId}"
     */
    public final static String ORDER_LOCK = "orderLock-";

    /**
     * 锁支付，key：PAY_LOCK+"{payId}"
     */
    public final static String PAY_LOCK = "payLock-";

    /**
     * 锁账单创建，key：ORDER_TRANSACTION_LOCK+"{orderd}"
     */
    public final static String ORDER_TRANSACTION_LOCK = "order_transaction_lock-";

    /**
     * 锁自动填报工资创建，key：SALARY_LOCK"
     */
    public final static String SALARY_LOCK = "salary_lock";

}