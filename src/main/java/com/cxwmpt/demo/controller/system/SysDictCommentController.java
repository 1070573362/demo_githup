package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysDictComment;
import com.cxwmpt.demo.model.system.SysLog;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.service.api.system.SysDictCommentService;
import com.cxwmpt.demo.service.api.system.SysLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @program: backend
 * @description
 * @author: YouName
 * @create: 2020-05-19 14:45
 **/
@Controller
public class SysDictCommentController {
    @Autowired
    private SysDictCommentService sysDictCommentService;
    /**
     * 查询所有的数据
     * @param map
     * @return
     */
    @RequestMapping("/api/auth/dictComment/pageList")
    @ResponseBody
    public ResultMessage pageList(@RequestParam Map<String, Object> map) {

        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        }
        List<SysDictComment> list = sysDictCommentService.AllList(map);
        PageInfo<SysDictComment> info = new PageInfo<>(list);
        return ResultMessage.success(info.getList(), (int)info.getTotal());



    }


}
