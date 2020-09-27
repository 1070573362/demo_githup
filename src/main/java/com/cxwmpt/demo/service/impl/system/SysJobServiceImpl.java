package com.cxwmpt.demo.service.impl.system;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxwmpt.demo.dao.system.SysJobMapper;
import com.cxwmpt.demo.model.system.SysJob;
import com.cxwmpt.demo.service.api.system.SysJobService;
import org.springframework.stereotype.Service;

@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

}
