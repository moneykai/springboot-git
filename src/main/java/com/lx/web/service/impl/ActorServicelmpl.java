package com.lx.web.service.impl;

import com.lx.web.entity.Actor;
import com.lx.web.repository.ActorRepository;
import com.lx.web.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @ClassName ActorServicelmpl
 * @Description TODO
 * @Author AK787
 * @Date 2019/2/18 9:48
 */
@Service
public class ActorServicelmpl implements ActorService{
    @Autowired
    private ActorRepository actorRepository;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public Actor getActorById(int actorId) {
        String key = "springboot-redis:actor:"+actorId;
        Actor actor = null;
        Object result = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(result)){
            System.out.println(">>>>>>>>>>>>从redis中获取数据");
            
        }

        return null;
    }
}
