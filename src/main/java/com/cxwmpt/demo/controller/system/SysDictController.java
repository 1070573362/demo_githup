package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.annotation.SysLog;
import com.cxwmpt.demo.common.result.CodeEnum;
import com.cxwmpt.demo.common.result.ResultCodeEnum;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.*;
import com.cxwmpt.demo.service.api.system.SysDictCommentService;
import com.cxwmpt.demo.service.api.system.SysDictService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.shiro.SecurityUtils.getSubject;

/**
 * @program: backend
 * @description
 * @author: YouName
 * @create: 2020-05-20 16:53
 **/
@Controller
public class SysDictController {

    @Autowired
    private SysDictService sysDictService;

    @Autowired
    private SysDictCommentService sysDictCommentService;

    /**
     * 字典列表页面
     * @return
     */
    @RequestMapping("/html/dict/page")
    public String page() {
        return "systemSetup/userCenter/dict/Page";
    }

    /**
     * 新增页面
     * @return
     */
    @RequestMapping("/html/dict/addPage")
    public String addPage() {
        return "systemSetup/userCenter/dict/Add";
    }

    /**
     * 修改页面
     * @param model
     * @param id
     * @param action
     * @return
     */
    @RequestMapping("/html/dict/updatePage")
    @SysLog("打开菜单管理的新增或修改窗口")
    public String updatePage(Model model, String id, String action) {
        model.addAttribute("action", action);
        if (StringUtils.isNotBlank(id)) {
            SysDict sysDict = sysDictService.getById(id);
            model.addAttribute("entity", sysDict);
        }
        return "systemSetup/userCenter/dict/Update";
    }


    /**
     * 查询所有的数据
     * @param map
     * @return
     */
    @RequestMapping("/api/auth/dict/pageList")
    @ResponseBody
    public ResultMessage pageList(@RequestParam Map<String, Object> map) {

        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        }

        List<SysDict> list = sysDictService.AllList(map);

        PageInfo<SysDict> info = new PageInfo<>(list);

        return ResultMessage.success(info.getList(), (int)info.getTotal());
    }

    /**
     * 新增和修改
     * @param sysDict
     * @return
     */

    @RequestMapping("/api/auth/dict/save")
    @ResponseBody
    @SysLog("保存菜单信息")
    public ResultMessage save(SysDict sysDict) {
        //获取登录人信息
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        //添加新用户验证loginID是否相同
        if (StringUtils.isNotBlank(sysDict.getId())) {
            //修改
            sysDict.setUpdateId(loginUser.getId());
            if (sysDictService.updateById(sysDict)) {
                return ResultMessage.success();
            }
            return ResultMessage.error(ResultCodeEnum.UPDATE_DATE_ERROR);
        } else {
            //新增
            sysDict.setCreateId(loginUser.getId());
            if(sysDictService.save(sysDict)){
                return ResultMessage.success();
            }
            return  ResultMessage.error(ResultCodeEnum.ADD_DATE_ERROR);
        }
    }

    /**
     * 删除数据字典
     * @param ids
     * @return
     */
    @RequestMapping("/api/auth/dict/deletes")
    @ResponseBody
    public ResultMessage deletes(@RequestParam("ids[]") List<String> ids) {
        if (ids.size() <= 0) {
            return ResultMessage.error(ResultCodeEnum.OPERATION_DATA_IS_NULL);
        }
        for (String data : ids) {
            SysDict dict = sysDictService.getById(data);
            sysDictService.removeById(data);
            QueryWrapper<SysDictComment> wrapper = new QueryWrapper<>();
            wrapper.eq("dict_code", dict.getCode());
            wrapper.eq("del_flag", false);
            sysDictCommentService.remove(wrapper);
        }
        return ResultMessage.success();
    }



}
