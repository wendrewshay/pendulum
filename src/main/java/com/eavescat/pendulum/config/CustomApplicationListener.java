package com.eavescat.pendulum.config;

import com.eavescat.pendulum.domain.JobCache;
import com.eavescat.pendulum.domain.JobInfo;
import com.eavescat.pendulum.quartz.MyScheduler;
import com.eavescat.pendulum.repository.JobInfoRepository;
import com.eavescat.pendulum.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 自定义应用事件监听
 * Created by wendrewshay on 2019/7/13 14:39
 */
@Slf4j
@Configuration
public class CustomApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final MyScheduler quartzScheduler;
    private final JobInfoRepository jobInfoRepository;
    private final MyRabbitMQConfig myRabbitMQConfig;
    public CustomApplicationListener(JobInfoRepository jobInfoRepository, MyScheduler quartzScheduler, MyRabbitMQConfig myRabbitMQConfig) {
        this.jobInfoRepository = jobInfoRepository;
        this.quartzScheduler = quartzScheduler;
        this.myRabbitMQConfig = myRabbitMQConfig;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        JobCache.clear();
        List<JobInfo> jobInfoList = this.jobInfoRepository.findByIsEnabled(1);
        if (!CollectionUtils.isEmpty(jobInfoList)) {
            for (JobInfo jobInfo : jobInfoList) {
                String cacheKey = StringUtils.concat(jobInfo.getGroupName(), "_", jobInfo.getJobName(), "_", jobInfo.getSequence()+"");
                JobCache.put(cacheKey, jobInfo);
                myRabbitMQConfig.declare(jobInfo);
            }
        }
        try {
            quartzScheduler.start(jobInfoList);
        } catch (SchedulerException e) {
            log.error(">>> 定时任务调度异常，原因：{}", e.getMessage(), e);
        }
        log.info(">>> 应用启动成功，共加载定时任务缓存{}个", jobInfoList.size());
    }

}
