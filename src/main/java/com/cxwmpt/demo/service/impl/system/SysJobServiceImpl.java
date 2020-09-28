package com.cxwmpt.demo.service.impl.system;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxwmpt.demo.dao.system.SysDictMapper;
import com.cxwmpt.demo.dao.system.SysJobMapper;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysJob;
import com.cxwmpt.demo.service.api.system.SysJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {
    @Autowired//一定要加注释
    protected SysJobMapper sysJobMapper;
    @Override
    public List<SysJob> AllList(Map map) {
        return sysJobMapper.AllList(map);
    }
}
