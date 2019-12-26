package com.eavescat.pendulum.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 定时任务的信息
 * Created by wendrewshay on 2019/7/13 12:04
 */
@Data
@Entity
@Table(name = "job_info")
public class JobInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    /**
     * 任务名称
     */
    @Column(name = "job_name")
    private String jobName;
    /**
     * 分组名称
     */
    @Column(name = "group_name")
    private String groupName;
    /**
     * 序号
     */
    @Column(name = "sequence")
    private int sequence;
    /**
     * 任务简介
     */
    @Column(name = "summary")
    private String summary;
    /**
     * cron表达式
     */
    @Column(name = "cron_expression")
    private String cronExpression;
    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private int isEnabled;
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;
}
