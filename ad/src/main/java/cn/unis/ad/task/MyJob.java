package cn.unis.ad.task;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MyJob extends QuartzJobBean {

    private static final ReentrantLock lock = new ReentrantLock();
    private final static Logger log = LoggerFactory.getLogger(AddAdTask.class);
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try{
            lock.lock();
            String co = context.getMergedJobDataMap().getString("param");
            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("co:"+co+"--Executing MyJob at: " + formattedDateTime);
        }finally {
            lock.unlock();
        }

    }
}
