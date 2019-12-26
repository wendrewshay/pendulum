package com.eavescat.pendulum.service;

import com.eavescat.pendulum.config.MyRabbitMQConfig;
import com.eavescat.pendulum.domain.JobCache;
import com.eavescat.pendulum.domain.JobInfo;
import com.eavescat.pendulum.quartz.MyScheduler;
import com.eavescat.pendulum.repository.JobInfoRepository;
import com.eavescat.pendulum.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 逻辑层，定时获取任务信息、并调度任务
 * Created by wendrewshay on 2019/7/13 14:02
 */
@Slf4j
@Service
public class JobInfoService {
    private final JobInfoRepository jobInfoRepository;
    private final MyScheduler quartzScheduler;
    private final MyRabbitMQConfig myRabbitMQConfig;
    private JobInfoService(JobInfoRepository jobInfoRepository, MyScheduler quartzScheduler, MyRabbitMQConfig myRabbitMQConfig) {
        this.jobInfoRepository = jobInfoRepository;
        this.quartzScheduler = quartzScheduler;
        this.myRabbitMQConfig = myRabbitMQConfig;
    }

    /**
     * 初始化时延时2秒并已每1分钟执行一次，用于查询数据库中的任务并做调度处理
     * @author wendrewshay
     * @date 2019/7/13 14:08
     */
    @Scheduled(initialDelay = 10_000, fixedRate = 60_000)
    public void execute() throws SchedulerException {
        List<JobInfo> jobInfoList = this.jobInfoRepository.findByIsEnabled(1);
        if (!CollectionUtils.isEmpty(jobInfoList)) {
            // 如果缓存中的job在数据库中被删除，则移除该job缓存并删除该任务调度
            Collection<JobInfo> allCachedJobList = JobCache.getAll();
            List<JobInfo> copiedCachedJobList = new ArrayList<>(allCachedJobList);
            copiedCachedJobList.removeAll(jobInfoList);
            if (!CollectionUtils.isEmpty(copiedCachedJobList)) {
                for (JobInfo copiedCachedJobInfo : copiedCachedJobList) {
                    String cacheKey = StringUtils.concat(copiedCachedJobInfo.getGroupName(), "_", copiedCachedJobInfo.getJobName(), "_", copiedCachedJobInfo.getSequence()+"");
                    // 若库中不存在相应的jobName和groupName，则删除
                    boolean existedPrefixKey = false;
                    for (JobInfo jobInfo : jobInfoList) {
                        if (jobInfo.getGroupName().equalsIgnoreCase(copiedCachedJobInfo.getGroupName())
                                && jobInfo.getJobName().equalsIgnoreCase(copiedCachedJobInfo.getJobName())
                                && jobInfo.getSequence() == copiedCachedJobInfo.getSequence()) {
                                existedPrefixKey = true;
                        }
                    }
                    if (!existedPrefixKey) {
                        JobCache.remove(cacheKey);
                        this.quartzScheduler.deleteJob(copiedCachedJobInfo);
                        log.info(">>> 定时任务被移除：{\"jobName\":\"{}\", \"groupName\":\"{}\", \"sequence\":\"{}\", \"cronExpression\":\"{}\"}",
                                copiedCachedJobInfo.getJobName(), copiedCachedJobInfo.getGroupName(), copiedCachedJobInfo.getSequence(), copiedCachedJobInfo.getCronExpression());
                    }
                }
            }
            // 如果数据库中的job在缓存中不存在，则添加缓存并执行调度；如果存在则对比cron表达式，不相同则重新设置调度执行时间
            for (JobInfo jobInfo : jobInfoList) {
                String cacheKey = StringUtils.concat(jobInfo.getGroupName(), "_", jobInfo.getJobName(), "_", jobInfo.getSequence()+"");
                JobInfo cachedJob = JobCache.get(cacheKey);
                if (cachedJob == null) {
                    // 如果缓存中没有该job，则放入缓存，并调度执行job和消息绑定
                    JobCache.put(cacheKey, jobInfo);
                    this.quartzScheduler.scheduleJob(jobInfo);
                    this.myRabbitMQConfig.declare(jobInfo);
                    log.info(">>> 新增定时任务：{\"jobName\":\"{}\", \"groupName\":\"{}\", \"sequence\":\"{}\", \"cronExpression\":\"{}\"}", jobInfo.getJobName(), jobInfo.getGroupName(), jobInfo.getSequence(), jobInfo.getCronExpression());
                } else if (!cachedJob.getCronExpression().equalsIgnoreCase(jobInfo.getCronExpression())) {
                    // 如果缓存中存在该job但表达式发生了改变，则重新调度该job
                    JobCache.put(cacheKey, jobInfo);
                    this.quartzScheduler.rescheduleJob(jobInfo);
                    log.info(">>> 定时任务被重置：{\"jobName\":\"{}\", \"groupName\":\"{}\", \"sequence\":\"{}\", \"cronExpression\":\"{}\"}", jobInfo.getJobName(), jobInfo.getGroupName(), jobInfo.getSequence(), jobInfo.getCronExpression());
                }
            }
        } else {
            // 若库中没有任务信息，则清空缓存和调度
            if (!JobCache.isEmpty()) {
                JobCache.clear();
                this.quartzScheduler.clear();
                log.info(">>> 清除所有定时任务");
            }
        }
    }

}
