package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.common.result.CodeEnum;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.service.api.system.SysDictService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

   // @Autowired
  //  private SysDictIndexService sysDictIndexService;

    /**
     * 字典列表页面
     * @return
     */
    @RequestMapping("/html/dict/page")
    public String page() {
        return "systemSetup/userCenter/dict/Page";
    }

    /**
     * 新增索引页面
     * @return
     */
    @RequestMapping("/html/dict/addIndex")
    public String addIndex() {
        return "systemSetup/userCenter/dict/AddIndex";
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
     * 删除数据字典
     * @param ids
     * @return
     */
    @RequestMapping("/api/auth/dict/deletes")
    @ResponseBody
    public ResultMessage deletes(@RequestParam("ids[]") List<String> ids) {

        //删除字典信息
        for (String id : ids) {
            SysDict dict = sysDictService.getById(id);
            sysDictService.removeById(id);
        }
        return ResultMessage.success();
    }



}
