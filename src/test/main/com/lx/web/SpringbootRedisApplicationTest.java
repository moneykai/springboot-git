package com.lx.web;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
/*
* 应用测试启动器
* */

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootRedisApplicationTest {

       @Autowired
       private RedisTemplate<String,Object> redisTemplate;

       @Test
       public void stringOperation(){
           //设置值
           redisTemplate.opsForValue().set("name","Kobe");
           //获取值
           Object name = redisTemplate.opsForValue().get("name");
           //断言
           Assert.assertEquals("abc",name);
        }

}

