package cn.unis.check.service;

import cn.unis.check.task.CheckBak;
import cn.unis.check.task.DelBak;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class CheckService {
    private final static Logger logger = LoggerFactory.getLogger(CheckService.class);

    @Value("${bak.crontime:0 0 9 * * ?}")
    private String executeTime;

    @Value("${bak.proxyServer:}")
    private String proxyServer;

    @Value("${bak.webhookUrl:}")
    private String webhookUrl;

    @Value("${bak.cheklist:}")
    private String cheklist;

    @Value("${bak.dellist:}")
    private String dellist;
    @Resource
    CheckBak checkBak;
    @Resource
    DelBak delBak;
    @PostConstruct
    public void init() {
        logger.info("定时任务系统启动");
        logger.info("定时任务执行时间: " + executeTime);
        logger.info("proxyServer: " + proxyServer);
        logger.info("webhookUrl: " + webhookUrl);
        logger.info("cheklist: " +  cheklist);
        logger.info("dellist: " + dellist);
    }

    /**
     * 定时执行任务
     */
    @Scheduled( cron = "${bak.crontime:0 0 9 * * ?}") // 默认每天凌晨2点执行
    public void executeTask() {
        logger.info("执行定时任务----: " + LocalDateTime.now());
        JSONArray checkListArray = JSONArray.parseArray(cheklist);
        JSONArray delListArray = JSONArray.parseArray(dellist);
        checkBak.check(checkListArray, proxyServer, webhookUrl);
        delBak.del(delListArray);
    }
}
