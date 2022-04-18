package com.wsz.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Boolean> script;

    /**
     * 未设置锁的失效时间，新的线程进去一直拿不到锁，导致无法进行占位
     */
    @Test
    void testLock01() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 占位，如果key不存在也可以设置成功
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1");
        // 如果占位成功，进行正常操作
        if (isLock){
            valueOperations.set("name", "xxxxx");
            String name = (String)valueOperations.get("name");
            System.out.println("name = " + name);
            // 出现异常
            Integer.parseInt("xxxxx");
            // 占位成功，删除锁
            redisTemplate.delete("k1");
        }else{
            System.out.println("有线程在使用,请稍后再试");
        }

    }

    /**
     * 设置锁的失效时间，等锁的失效时间到了还是会重新占位
     */
    @Test
    public void test02(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //给锁添加过期时间，防止应用在运行过程中抛出异常导致锁无法释放
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        if (isLock){
            valueOperations.set("name", "xxxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            // 出现异常
            Integer.parseInt("xxxxx");
            redisTemplate.delete("k1");
        }else{
            System.out.println("有线程在使用,请稍后再试");
        }
    }

    /**
     * lua文件测试
     */
    @Test
    public void test03(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String value = UUID.randomUUID().toString();
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        if (isLock){
            valueOperations.set("name", "xxxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            System.out.println(valueOperations.get("k1"));
            Boolean result = (Boolean)redisTemplate.execute(script, Collections.singletonList("k1"), value);
            System.out.println(result);
        }else{
            System.out.println("有线程在使用,请稍后再试");
        }
    }
}
