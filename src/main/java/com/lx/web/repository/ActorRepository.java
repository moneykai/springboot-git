package com.lx.web.repository;


import com.lx.web.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName ActorRepository
 * @Description 演员信息数据库仓库
 * @Author zhuwenbin
 * @Date 2019/2/16 13:35
 */
public interface ActorRepository extends JpaRepository<Actor, Integer>{

}
