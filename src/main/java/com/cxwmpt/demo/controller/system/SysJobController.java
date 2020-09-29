package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.annotation.SysLog;
import com.cxwmpt.demo.common.result.ResultCodeEnum;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.SysDict;
import com.cxwmpt.demo.model.system.SysDictComment;
import com.cxwmpt.demo.model.system.SysJob;
import com.cxwmpt.demo.model.system.SysUser;
import com.cxwmpt.demo.quartzJob.QuartzManager;
import com.cxwmpt.demo.service.api.system.SysJobService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.shiro.SecurityUtils.getSubject;


@Controller
public class SysJobController {

    @Autowired
    private SysJobService sysJobService;

    @Autowired
    private QuartzManager quartzManager;

    @RequestMapping("/html/system/job/page")
    public String page() {
        return "systemSetup/userCenter/job/Page";
    }

    /**
     * 新增页面
     * @return
     */
    @RequestMapping("/html/system/job/addPage")
    public String addPage() {
        return "systemSetup/userCenter/job/Add";
    }

    /**
     * 修改页面
     * @param model
     * @param id
     * @param action
     * @return
     */
    @RequestMapping("/html/system/job/updatePage")
    @SysLog("打开菜单管理的新增或修改窗口")
    public String updatePage(Model model, String id, String action) {
        model.addAttribute("action", action);
        if (StringUtils.isNotBlank(id)) {
            SysJob sysJob = sysJobService.getById(id);
            model.addAttribute("entity", sysJob);
        }
        return "systemSetup/userCenter/job/Update";
    }


    /**
     * 查询任务列表
     * @param map
     * @return
     */
    @PostMapping("/api/auth/job/pageList")
    @ResponseBody
    public ResultMessage pageList(@RequestParam Map<String, Object> map) {

        if (map.containsKey("page") && map.containsKey("limit")) {
            PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        }
        List<SysJob> list = sysJobService.AllList(map);
        PageInfo<SysJob> info = new PageInfo<>(list);

        return ResultMessage.success(info.getList(), (int)info.getTotal());
    }

    /**
     * 新增和修改
     * @param
     * @return
     */

    @RequestMapping("/api/auth/job/save")
    @ResponseBody
    @SysLog("保存菜单信息")
    public ResultMessage save(SysJob sysJob) {
        //获取登录人信息
        SysUser loginUser = (SysUser) getSubject().getPrincipal();
        //查询有没有重复的字段
        QueryWrapper<SysJob> wrapper = new QueryWrapper<>();
        wrapper.eq("job_name", sysJob.getJobName());
        wrapper.eq("del_flag", false);
        int count = sysJobService.count(wrapper);
        //添加新用户验证loginID是否相同
        if (StringUtils.isNotBlank(sysJob.getId())) {
            //原数据库数据
            SysJob old = sysJobService.getById(sysJob.getId());
            if (!sysJob.getJobName().equals(old.getJobName())) {
                if (count != 0) {
                    return ResultMessage.error(-1, "对不起，你修改的名称重复，请重新创建");
                }
            }
            //修改
            sysJob.setUpdateId(loginUser.getId());
            if (sysJobService.updateById(sysJob)) {
                quartzManager.updateJob(sysJob);
                return ResultMessage.success();
            }
            return ResultMessage.error(ResultCodeEnum.UPDATE_DATE_ERROR);
        } else {
            //新增
            if (count != 0) {
                return ResultMessage.error(-1, "对不起，你创建的编号重复，请重新创建");
            }
            sysJob.setCreateId(loginUser.getId());
            if(sysJobService.save(sysJob)){
               try {
                   quartzManager.addJob(Class.forName(sysJob.getJobClass()),sysJob,new HashMap<>());
                } catch (ClassNotFoundException e) {
                    return    ResultMessage.error(ResultCodeEnum.ADD_DATE_ERROR);
                }
                return ResultMessage.success();
            }
            return  ResultMessage.error(ResultCodeEnum.ADD_DATE_ERROR);
        }
    }

    /**
     * 删除一个任务
     * @param ids
     * @return
     */
    @PostMapping("/api/auth/job/deletes")
    @ResponseBody
    public ResultMessage deletes(@RequestParam("ids[]") List<String> ids){
        if (ids.size() <= 0) {
            return ResultMessage.error(ResultCodeEnum.OPERATION_DATA_IS_NULL);
        }
        for (String data : ids) {
            SysJob sysJob = sysJobService.getById(data);
            sysJobService.removeById(sysJob.getId());
             quartzManager.deleteJob(sysJob);
        }
        return ResultMessage.success();
    }
    /**
     * 重启一个任务
     * @param
     * @return
     */
    @PostMapping("/api/auth/job/restarts")
    @ResponseBody
    public ResultMessage restarts(@RequestParam("ids[]") List<String> ids){
        if (ids.size() <= 0) {
            return ResultMessage.error(ResultCodeEnum.OPERATION_DATA_IS_NULL);
        }
        for (String data : ids) {
            SysJob sysJob = sysJobService.getById(data);
            sysJob.setJobStatus("0");
            sysJobService.updateById(sysJob);
            quartzManager.restartJob(sysJob);
        }
        return ResultMessage.success();
    }


    /**
     * 暂停一个任务
     * @param
     * @return
     */
    @PostMapping("/api/auth/job/stops")
    @ResponseBody
    public ResultMessage stops(@RequestParam("ids[]") List<String> ids){
        if (ids.size() <= 0) {
            return ResultMessage.error(ResultCodeEnum.OPERATION_DATA_IS_NULL);
        }
        for (String data : ids) {
            SysJob sysJob = sysJobService.getById(data);
            sysJob.setJobStatus("1");
            sysJobService.updateById(sysJob);
             quartzManager.stopJob(sysJob);
        }
        return ResultMessage.success();
    }
    /**
     * 启动所有定时任务
     * @return
     */
    @PostMapping("/api/auth/job/startAll")
    @ResponseBody
    public ResultMessage startAll(){

        quartzManager.startJobs();

        return ResultMessage.success();
    }

    /**
     * 关闭所有定时任务
     * @return
     */
    @PostMapping("/api/auth/job/stopAll")
    @ResponseBody
    public ResultMessage stopAll(){

        quartzManager.stopJobs();
        return ResultMessage.success();
    }

}
