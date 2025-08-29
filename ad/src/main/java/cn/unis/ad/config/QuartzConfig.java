package cn.unis.ad.config;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {
    private final static Logger log = LoggerFactory.getLogger(QuartzConfig.class);
    @Resource
    private Scheduler scheduler;
    private final AutowireCapableBeanFactory beanFactory;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @PostConstruct
    public void init() {
        String sql = "select class,param,cron from task where enable=1";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> result : results) {
            String classname = result.get("class").toString();
            String param = result.get("param").toString();
            String cron = result.get("cron").toString();
            addCronJob(classname, param, cron);
        }
    }

    public void addCronJob(String jobClassName, String param, String cron) {
        try {
//            JobKey jobKey = JobKey.jobKey(jobName);
//            if (scheduler.checkExists(jobKey)) {
//                log.info("该作业已存在");
//            }
            //构建job
            JobDetail job = (JobDetail) JobBuilder
                    .newJob((Class<? extends Job>) Class.forName(jobClassName))
                    .usingJobData("param", param)
//                    .withIdentity(jobKey)
                    .build();
            //Cron表达式定时构造器
            CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cron);
            //构建Trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(cronSchedule)
                    .build();

            //启动调度
             scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("创建定时任务成功！" + jobClassName + ",任务表达式：" + cron);
    }

    @Bean
    public SpringBeanJobFactory jobFactory() {
        return new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object jobInstance = super.createJobInstance(bundle);
                // 注入 Spring 管理的 Bean
                beanFactory.autowireBean(jobInstance);
                return jobInstance;
            }
        };
    }
}
