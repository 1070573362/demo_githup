package com.cxwmpt.demo.quartzJob;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * Created by admin on 2020/8/17.
 */
public class LogMaintainJob extends QuartzJobBean {

    @Override
    @Caching(evict = {
            @CacheEvict(value = "carouseCache", allEntries = true),
            @CacheEvict(value = "activityCache", allEntries = true),
            @CacheEvict(value = "newsCache", allEntries = true)
    })
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("维护redis定时任务启动！！！启动时间：{}");
    }
}
