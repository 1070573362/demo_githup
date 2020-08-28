package com.cxwmpt.demo.service.api.system;



import com.baomidou.mybatisplus.extension.service.IService;
import com.cxwmpt.demo.model.system.SysRole;

import java.util.List;
import java.util.Map;

/**
* @program: backend
*
* @description
*
* @author: YouName
*
* @create: 2020-04-08 17:01
**/
public interface SysRoleService extends IService<SysRole> {


    List<SysRole> AllList(Map map);
}
