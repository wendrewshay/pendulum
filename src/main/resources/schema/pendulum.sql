/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50721
Source Host           : 127.0.0.1:3306
Source Database       : pendulum

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-07-17 14:08:23
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for job_info
-- ----------------------------
DROP TABLE IF EXISTS `job_info`;
CREATE TABLE `job_info` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(50) NOT NULL COMMENT '任务名称，一般为“类名_方法名”',
  `group_name` varchar(50) NOT NULL COMMENT '分组名称，一般为项目名称',
  `sequence` smallint(2) NOT NULL DEFAULT '1' COMMENT '序号，同一个应用同一个定时任务可以在多个不同时间执行',
  `summary` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `cron_expression` varchar(30) NOT NULL COMMENT 'cron表达式',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNI_IDX` (`group_name`,`job_name`,`sequence`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务的信息';

-- ----------------------------
-- Records of job_info
-- ----------------------------
INSERT INTO `job_info` VALUES ('1', 'className.methodName1', 'appName1', '1', '组名为”应用名“，job命名为”类名_方法名“', '0/3 * * * * ?', '2019-07-16 19:19:34', '2019-07-17 12:54:51');
INSERT INTO `job_info` VALUES ('2', 'className.methodName2', 'appName1', '1', '组名为”应用名“，job命名为”类名_方法名“', '0/5 * * * * ?', '2019-07-16 19:19:34', '2019-07-17 14:04:51');
INSERT INTO `job_info` VALUES ('3', 'className.methodName2', 'appName1', '2', '组名为”应用名“，job命名为”类名_方法名“', '0/8 * * * * ?', '2019-07-16 19:19:34', '2019-07-17 14:04:58');
