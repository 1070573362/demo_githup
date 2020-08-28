package com.cxwmpt.demo.service.api.system;



import com.baomidou.mybatisplus.extension.service.IService;
import com.cxwmpt.demo.model.system.SysMenu;

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
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> listTreeTable();

    List<Map> getDTreeList();

    List<String> getByIDSelectSubNode(String id);

    List<Node> listLoginInfoMenu(String id);

    List<String> ListPermissionFromUserId(String id);
}
