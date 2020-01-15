package com.emotte.hss.core.utils;

import com.emotte.hss.core.common.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.core.Commit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @program: hss-parent
 * @description: redis分布式锁
 * @author: TTao
 * @date: 2019-11-08 15:03
 **/
@Commit
@Slf4j
@Component
public class RedisLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean lock(String key){
        while (true){
            try {
                if(doLock(key, RedisConstant.EXPIRE_TIME)){
                    break;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error("加锁异常",e);
                return true;
            }
        }
        log.info("获得redis锁，key:{}",key);
        return true;
    }

    /**
     * redis加锁
     * @param key 锁的键值
     * @param value 超时时间 锁多久超时
     * @return
     */
    public boolean doLock(String key,String value){
        //如果不存在就设置锁的对象及超时时间
        if(redisTemplate.opsForValue().setIfAbsent(key,value)){
            return true;
        }
        String currentLockValue  = redisTemplate.opsForValue().get(key);
        //如果锁过期
        if(!StringUtils.isEmpty(currentLockValue) && Long.parseLong(currentLockValue)<System.currentTimeMillis()){
            //取得之前过期锁的时间并且设置当前锁的超时时间 redis单线程保证该步只有一个线程执行
            String beforeValue = redisTemplate.opsForValue().getAndSet(key,value);
            if(!StringUtils.isEmpty(beforeValue) && beforeValue.equals(currentLockValue)){
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     */
    public void unlock(String key) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(RedisConstant.EXPIRE_TIME)){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
            log.info("redis解锁：{}",key);
        } catch (Exception e) {
            log.error("解锁异常，{}",e);
        }
    }

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}