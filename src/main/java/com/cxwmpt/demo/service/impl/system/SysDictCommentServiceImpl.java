package com.cxwmpt.demo.service.impl.system;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxwmpt.demo.dao.system.SysDictCommentMapper;
import com.cxwmpt.demo.dao.system.SysDictMapper;
import com.cxwmpt.demo.dao.system.SysLogMapper;
import com.cxwmpt.demo.dao.system.SysMenuMapper;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysDictComment;
import com.cxwmpt.demo.model.system.SysLog;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.service.api.system.SysDictCommentService;
import com.cxwmpt.demo.service.api.system.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @program: backend
*
* @description
*
* @author: YouName
*
* @create: 2020-04-03 10:15
**/
@Service
public class SysDictCommentServiceImpl extends ServiceImpl<SysDictCommentMapper, SysDictComment> implements SysDictCommentService {

    @Autowired//一定要加注释
    protected SysDictCommentMapper sysDictCommentMapper;
    @Override
    public List<SysDictComment> AllList(Map map) {
        return sysDictCommentMapper.AllList(map);
    }
}
