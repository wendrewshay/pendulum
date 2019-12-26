package com.eavescat.pendulum.quartz;

import com.eavescat.pendulum.domain.JobInfo;
import org.quartz.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 自定义定时任务调度器，操作定时任务
 * Created by wendrewshay on 2019/7/13 11:15
 */
@Component
public class MyScheduler {

    private Scheduler scheduler;
    private MyScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 启动所有Job调度
     * @author wendrewshay
     * @date 2019/7/13 15:28
     * @param jobInfoList 任务信息列表
     */
    public void start(List<JobInfo> jobInfoList) throws SchedulerException {
        if (!CollectionUtils.isEmpty(jobInfoList)) {
            for (JobInfo jobInfo : jobInfoList) {
                scheduleJob(jobInfo);
            }
        }
        this.scheduler.start();
    }

    /**
     * 修改某个任务的执行时间
     * @author wendrewshay
     * @date 2019/7/13 16:29
     * @param jobInfo 任务信息
     * @return boolean
     */
    public boolean rescheduleJob(JobInfo jobInfo) throws SchedulerException {
        Date date = null;
        TriggerKey triggerKey = new TriggerKey(jobInfo.getJobName() + "_" + jobInfo.getSequence(), jobInfo.getGroupName());
        CronTrigger oldTrigger = (CronTrigger)this.scheduler.getTrigger(triggerKey);
        if (!oldTrigger.getCronExpression().equalsIgnoreCase(jobInfo.getCronExpression())) {
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression());
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobInfo.getJobName() + "_" + jobInfo.getSequence(), jobInfo.getGroupName()).withSchedule(cronScheduleBuilder).build();
            date = this.scheduler.rescheduleJob(triggerKey, cronTrigger);
        }
        return date != null;
    }

    /**
     * 删除某个Job
     * @author wendrewshay
     * @date 2019/7/13 16:02
     * @param jobInfo 任务信息
     */
    public void deleteJob(JobInfo jobInfo) throws SchedulerException {
        JobKey jobKey = new JobKey(jobInfo.getJobName() + "_" + jobInfo.getSequence(), jobInfo.getGroupName());
        JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
        if (jobDetail != null) {
            this.scheduler.deleteJob(jobKey);
        }
    }

    /**
     * 清空所有Job
     * @author wendrewshay
     * @date 2019/7/13 15:57
     */
    public void clear() throws SchedulerException {
        this.scheduler.clear();
    }

    /**
     * 调度一个Job
     * @author wendrewshay
     * @date 2019/7/13 11:19
     * @param jobInfo 任务信息
     */
    public void scheduleJob(JobInfo jobInfo) throws SchedulerException {
        // 构建JobDetail实例
        JobDetail jobDetail = JobBuilder.newJob(RabbitJob.class).withIdentity(jobInfo.getJobName() + "_" + jobInfo.getSequence(), jobInfo.getGroupName()).build();
        // 基于表达式构建触发器实例
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression());
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobInfo.getJobName() + "_" + jobInfo.getSequence(), jobInfo.getGroupName()).withSchedule(cronScheduleBuilder).build();
        this.scheduler.scheduleJob(jobDetail, cronTrigger);
    }
}
