# Getting Started

### 定时任务动态配置
涉及到的主要技术栈:Spring Boot + Quartz + RabbitMQ + Mysql

主要原理是通过获取数据库中的定时配置信息，准时发送消息到消息队列服务，再由接入的消费端执行对应的方法。

* 首先，在Mysql新建pendulum数据库，导入pendulum.sql脚本
* 其次，搭建好RabbitMQ服务端(单例也足够)
* 再次，配置好application-test.yml配置文件
* 最后，启动运行。

