package com.cxwmpt.demo.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cxwmpt.demo.common.result.ResultMessage;
import com.cxwmpt.demo.model.system.SysJob;
import com.cxwmpt.demo.quartzJob.QuartzManager;
import com.cxwmpt.demo.service.api.system.SysJobService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


@Controller
public class SysJobController {

    @Autowired
    private SysJobService jobService;

    @Autowired
    private QuartzManager quartzManager;

    @GetMapping("html/job/Page")
    public String page() {
        return "systemSetup/userCenter/job/Page";
    }

    @GetMapping("html/job/AddPage")
    public String add() {
        return "system/job/Add";
    }

    @GetMapping("html/job/UpdatePage")
    public String update(Model model, String id, String action) {
        if (id != null) {
            SysJob job = jobService.getById(id);
            model.addAttribute("job", job);
        }

        model.addAttribute("action", action);
        return "system/job/Update";
    }

    /**
     * 查询任务列表
     * @param map
     * @return
     */
    @PostMapping("/api/auth/job/pageList")
    @ResponseBody
    public ResultMessage pageList(@RequestParam Map<String, Object> map) {

        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        

        List<SysJob> list = jobService.AllList(map);

        PageInfo<SysJob> info = new PageInfo<>(list);

        return ResultMessage.success(info.getList(), (int)info.getTotal());
    }

    /**
     * 新增一个任务
     * @param job
     * @return
     */
    @PostMapping("api/auth/system/job/add")
    @ResponseBody
    public ResultMessage addJob(SysJob job){

        try {
            job.setJobStatus("0");
            jobService.save(job);

            quartzManager.addJob(job,Class.forName(job.getJobClass()));

        } catch (ClassNotFoundException e) {



        }

        return ResultMessage.success();
    }

    /**
     * 暂停一个任务
     * @param job
     * @return
     */
    @PostMapping("api/auth/system/job/pasueOneJob")
    @ResponseBody
    public ResultMessage edJob(SysJob job){

        job.setJobStatus("1");

        jobService.updateById(job);

        quartzManager.pasueOneJob(job);

        return ResultMessage.success();
    }

    /**
     * 删除一个任务
     * @param ids
     * @return
     */
    @PostMapping("api/auth/system/job/delete")
    @ResponseBody
    public ResultMessage removeJob(@RequestParam("ids[]") List<String> ids){

        ids.stream().forEach((id)->{
            SysJob job = jobService.getById(id);
            jobService.removeById(job.getId());
            quartzManager.removeJob(job);
        });

        return ResultMessage.success();
    }

    /**
     * 修改一个任务的cron表达式
     * @param job
     * @return
     */
    @PostMapping("api/auth/system/job/update")
    @ResponseBody
    public ResultMessage modifyJobTime(SysJob job){

        jobService.updateById(job);

        quartzManager.modifyJobTime(job, job.getCronExpression());

        return ResultMessage.success();
    }

    /**
     * 重启一个任务
     * @param job
     * @return
     */
    @PostMapping("api/auth/system/job/resOneJob")
    @ResponseBody
    public ResultMessage resOneJob(SysJob job){

        job.setJobStatus("0");

        jobService.updateById(job);

        quartzManager.resOneJob(job);

        return ResultMessage.success();
    }

    /**
     * 启动所有定时任务
     * @return
     */
    @PostMapping("api/auth/system/job/startAll")
    @ResponseBody
    public ResultMessage startAll(){

        quartzManager.startJobs();

        return ResultMessage.success();
    }

    /**
     * 关闭所有定时任务
     * @return
     */
    @PostMapping("api/auth/system/job/stopAll")
    @ResponseBody
    public ResultMessage stopAll(){

        quartzManager.shutdownJobs();

        return ResultMessage.success();
    }

}
