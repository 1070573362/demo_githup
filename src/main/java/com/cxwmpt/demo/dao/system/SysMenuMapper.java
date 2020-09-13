package com.cxwmpt.demo.dao.system;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxwmpt.demo.common.vo.Node;
import com.cxwmpt.demo.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* @program: backend
*
* @description
*
* @author: YouName
*
* @create: 2020-04-08 17:02
**/
@Repository
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    List<SysMenu> listTreeTable();

    List<Map>  getDTreeList();

    List<String> getByIDSelectSubNode(@Param("id") String id);

    List<Node> listLoginInfoMenu(@Param("id") String id);

    List<String> ListPermissionFromUserId(@Param("id") String id);

    List<SysMenu> getListById(@Param("map")Map map);
}