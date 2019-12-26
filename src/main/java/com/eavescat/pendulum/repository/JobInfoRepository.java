package com.eavescat.pendulum.repository;

import com.eavescat.pendulum.domain.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 自定义任务持久层操作库
 * Created by wendrewshay on 2019/7/13 14:01
 */
public interface JobInfoRepository extends JpaRepository<JobInfo, Integer> {

    List<JobInfo> findByIsEnabled(Integer isEnabled);
}
