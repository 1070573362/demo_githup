package com.cxwmpt.demo.service.api.system;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysJob;

import java.util.List;
import java.util.Map;

public interface SysJobService extends IService<SysJob> {
    List<SysJob> AllList(Map map);

}
