package com.cxwmpt.demo.dao.system;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxwmpt.demo.model.system.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* Created by Mybatis Generator 2020/04/07
*/
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectUserByMap(Map<String, Object> map);

    List<SysUser> AllList(@Param("map") Map map);
}