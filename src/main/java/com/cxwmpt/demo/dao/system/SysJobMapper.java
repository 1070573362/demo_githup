package com.cxwmpt.demo.dao.system;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
    List<SysJob> AllList(@Param("map") Map map);
}