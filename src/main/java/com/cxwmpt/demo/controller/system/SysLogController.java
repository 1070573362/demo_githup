package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nuotadi.common.message.ReturnMessage;
import com.nuotadi.common.message.ReturnMessageUtil;
import com.nuotadi.model.system.SysLog;
import com.nuotadi.service.api.system.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @program: backend
 * @description
 * @author: YouName
 * @create: 2020-05-19 14:45
 **/
@Controller
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 日志列表页面
     * @return
     */
    @GetMapping("/html/syslog/page")
    public String page() {
        return "html/systemSetup/userCenter/log/Page";
    }

    /**
     * 查询部门列表
     * @param params
     * @return
     */
    @PostMapping("/api/auth/sys/log/list")
    @ResponseBody
    public ReturnMessage<Object> selectData(@RequestParam Map<String, Object> params) {

        if (params.containsKey("page") && params.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(params.get("page").toString()), Integer.parseInt(params.get("limit").toString()));
        }

        QueryWrapper<SysLog> wrapper = new QueryWrapper<>();

        if (params.get("classMethod") != null && !("").equals(params.get("classMethod"))) {
            wrapper.like("class_method", params.get("classMethod"));
        }

        wrapper.orderByDesc("createDate");

        List<SysLog> list = sysLogService.list(wrapper);

        PageInfo<SysLog> info = new PageInfo<>(list);

        return ReturnMessageUtil.sucess(info.getList(), (int)info.getTotal());
    }

    /**
     * 删除日志信息
     * @param ids
     * @return
     */
    @PostMapping("/api/auth/sys/log/delete")
    @ResponseBody
    public ReturnMessage<Object> delete(@RequestParam("ids[]") List<String> ids) {
        //删除部门信息
        for (String id : ids) {
            sysLogService.removeById(id);
        }
        return ReturnMessageUtil.sucess();
    }


}
