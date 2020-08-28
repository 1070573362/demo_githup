package com.cxwmpt.demo.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by admin on 2020/4/9.
 */
@Controller
public class SystemController {

    /**
     * druid 数据监控页面
     * @return
     */
    @GetMapping(value = "html/sys/druid")
    public String druid() {
        return "system/druid/index";
    }

}
