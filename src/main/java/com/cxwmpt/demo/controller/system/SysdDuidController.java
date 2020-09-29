package com.cxwmpt.demo.controller.system;

import com.cxwmpt.demo.annotation.SysLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author admin
 * @date 2020/4/9
 */
@Controller
public class SysdDuidController {

    /**
     * druid 数据监控页面
     * @return
     */
    @RequestMapping(value = "/html/system/druid/page")
    @SysLog("打开用户管理窗口")
    public String druid() {
        return "systemSetup/userCenter/druid/index";
    }

}
