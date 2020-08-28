package com.cxwmpt.demo.service.api.system;



import com.baomidou.mybatisplus.extension.service.IService;
import com.cxwmpt.demo.model.system.SysUser;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2020/4/7.
 */
public interface SysUserService extends IService<SysUser> {

    List<SysUser> AllList(Map map);

    /**
     * 查询用户信息
     * @param map
     * @return
     */
    SysUser selectUserByMap(Map<String, Object> map);
}
