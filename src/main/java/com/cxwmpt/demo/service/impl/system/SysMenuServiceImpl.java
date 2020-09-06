package com.cxwmpt.demo.service.impl.system;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxwmpt.demo.common.util.TreeBuilder;
import com.cxwmpt.demo.common.vo.Node;
import com.cxwmpt.demo.dao.system.SysMenuMapper;
import com.cxwmpt.demo.model.system.SysMenu;
import com.cxwmpt.demo.service.api.system.SysMenuService;

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
* @create: 2020-04-08 17:02
**/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    protected SysMenuMapper sysMenuMapper;
    @Override
    public List<SysMenu> listTreeTable() {
        return sysMenuMapper.listTreeTable();
    }

    @Override
    public List<Map> getDTreeList() {
        return sysMenuMapper.getDTreeList();
    }

    @Override
    public List<String> getByIDSelectSubNode(String id) {
        return sysMenuMapper.getByIDSelectSubNode(id);
    }

    @Override
    public List<Node> listLoginInfoMenu(String id) {
        List<Node> listTreeTable =sysMenuMapper.listLoginInfoMenu(id);
        //转换成子父节点
        TreeBuilder treeBuilder = new TreeBuilder(listTreeTable);
       // tbMenuBarTree.buildListTree();
        return treeBuilder.buildTree();
    }

    @Override
    public List<String> ListPermissionFromUserId(String id) {
        List<String> sysMenuList=sysMenuMapper.ListPermissionFromUserId(id);
        if(sysMenuList.size()==1&&sysMenuList.get(0)==null){
            return null;
        }
        return sysMenuList;
    }
}
