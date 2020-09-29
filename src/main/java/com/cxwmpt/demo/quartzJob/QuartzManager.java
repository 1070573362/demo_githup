package com.cxwmpt.demo.quartzJob;


import com.cxwmpt.demo.model.system.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuartzManager {

    //注入调度器
    @Autowired
    private Scheduler scheduler;



    /**
     * @Description: 添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
     *
     * @param myJob
     *
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public  void addJob(SysJob myJob, Class cls) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:addJob  start");
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            if(scheduler==null){
                log.info("scheduler is null");
            }
            CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (null == cronTrigger) {
                JobDetail jobDetail = JobBuilder.newJob(cls).withIdentity(jobName,jobGroup).build();

                cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup)
                        .withSchedule(CronScheduleBuilder.cronSchedule(myJob.getCronExpression())).build();
//						cronTrigger.getJobDataMap().put("jobEntity", myJob);

                scheduler.scheduleJob(jobDetail, cronTrigger);
                //启动一个定时器
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
            } else {
                cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(CronScheduleBuilder.cronSchedule(myJob.getCronExpression()))
                        .build();
                scheduler.rescheduleJob(triggerKey, cronTrigger);
            }
            //等待启动完成
//			Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("定时任务启动失败：" + e.getMessage());
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:addJob  end");
    }

    /**
     * @Description: 暂停一个任务
     *
     * @param myJob
     *
     */
    public  void pasueOneJob(SysJob myJob) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:pasueOneJob  start");
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
//			if(null==trigger){
//				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob trigger is NULL ");
//				return;
//			}
            JobKey jobKey = trigger.getJobKey();
            scheduler.pauseJob(jobKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:pasueOneJob  end");
    }

    /**
     * @Description: 重启一个任务
     *
     * @param myJob
     *
     */
    public  void resOneJob(SysJob myJob) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:resOneJob  start");
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
//			if(null==trigger){
//				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob trigger is NULL ");
//				return;
//			}
            scheduler.rescheduleJob(triggerKey, trigger);
//			Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:resOneJob  end");
    }

    /**
     * @Description: 修改一个任务的触发时间
     *
     */
    public  void modifyJobTime(SysJob myJob, String time) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:modifyJobTime  start");
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            myJob.setCronExpression(time);
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(myJob.getCronExpression());
            cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
                    .build();
            scheduler.rescheduleJob(triggerKey, cronTrigger);

//			Thread.sleep(TimeUnit.MINUTES.toMillis(60));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:modifyJobTime  end");
    }

    /**
     * @Description: 移除一个任务(使用默认的任务组名，触发器名，触发器组名)
     *
     *
     */
    public  void removeJob(SysJob myJob) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob  start");
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if(null==trigger){
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob trigger is NULL ");
                return;
            }
            JobKey jobKey = trigger.getJobKey();
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(jobKey);// 删除任务

            //Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob  end");
    }

    /**
     *
     * 方法表述  获得执行器状态
     * @return
     * String
     */
    public  String getStatus(SysJob myJob){
        String state = "NONE";
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            //trigger state
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            state = triggerState.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return state;
    }

    /**
     * 是否存在任务
     * 方法表述
     * @param myJob
     * @return
     * boolean
     */
    public  boolean hasTrigger(SysJob myJob){
        boolean isHas = true;
        try {
            // 唯一主键
            String jobName = myJob.getJobName();
            String jobGroup = myJob.getJobGroup();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if(null==trigger){
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>method:removeJob trigger is NULL ");
                isHas = false;
            }
        } catch (SchedulerException e) {
            isHas = false;
            e.printStackTrace();
        }
        return isHas;
    }

    /**
     * @Description:启动所有定时任务
     *
     */
    public  void startJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description:关闭所有定时任务
     *
     */
    public  void shutdownJobs() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}