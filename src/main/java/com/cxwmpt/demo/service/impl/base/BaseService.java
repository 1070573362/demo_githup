package com.cxwmpt.demo.service.impl.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: backend
 * @description
 * @author: YouName
 * @create: 2020-05-07 15:29
 **/
@Service
public class BaseService {

    @Autowired
    private EweightAppTagService eweightAppTagService;

    @Autowired
    private SysCompanyService sysDeptService;

    /**
     * 查询分类树结构
     * @return
     */
    public List<DtreeVO> showTreeTags(List<DtreeVO> result, String pid) {

        QueryWrapper<EweightAppTag> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", pid);
        wrapper.eq("delFlag", 0);
        List<EweightAppTag> first = eweightAppTagService.list(wrapper);
        if (first.size() > 0) {
            for (int i = 0; i < first.size(); i++) {
                DtreeVO t = new DtreeVO();
                EweightAppTag tag = first.get(i);
                t.setName(tag.getName());
                t.setParentId(pid);
                t.setId(tag.getId());
                t.setIcon(tag.getIcon());
                t.setOpen(true);
                List<DtreeVO> list = new ArrayList<>();
                t.setChildren(showTreeTags(list, tag.getId()));
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 查询单位树结构
     * @return
     */
    public List<DtreeVO> showTreeDepts(List<DtreeVO> result, String pid) {

        QueryWrapper<SysCompany> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", pid);
        wrapper.eq("delFlag", 0);
        List<SysCompany> first = sysDeptService.list(wrapper);
        if (first.size() > 0) {
            for (int i = 0; i < first.size(); i++) {
                DtreeVO t = new DtreeVO();
                SysCompany dept = first.get(i);
                t.setName(dept.getCompanyName());
                t.setTitle(dept.getCompanyName());
                t.setParentId(pid);
                t.setId(dept.getId());
                t.setOpen(true);
                List<DtreeVO> list = new ArrayList<>();
                t.setChildren(showTreeDepts(list, dept.getId()));
                result.add(t);
            }
        }
        return result;
    }

}
