package com.cxwmpt.demo.service.impl.system;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxwmpt.demo.dao.system.SysUserMapper;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.service.api.system.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired//一定要加注释
    protected SysUserMapper sysUserMapper;

    @Override
    public List<SysUser> AllList(Map map) {
        return sysUserMapper.AllList(map);
    }

    @Override
    public SysUser selectUserByEntity(SysUser loginUser) {
        return sysUserMapper.selectUserByEntity(loginUser);
    }
}
