package cn.unis.ad.task;

import cn.unis.ad.util.CmdUtil;
import cn.unis.ad.util.CommonUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DelAdTask extends QuartzJobBean {
    private final static Logger log = LoggerFactory.getLogger(DelAdTask.class);
    private static final ReentrantLock lock = new ReentrantLock();
    @Resource
     JdbcTemplate jdbcTemplate;
    @Resource
     CommonUtil  commonUtil;
    @Resource
    CmdUtil cmdUtil;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 打印任务运行信息
        try {
            lock.lock();
            delAd();
        } finally {
            lock.unlock();
        }

    }

    public void delAd() {
        //删除30天前的域账号
        System.out.println("delAdTask");
        String sql = "select ad from denyad where denytime < date('now','-30 day');";
        List<Map<String, Object>> sqlres = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : sqlres) {
            String ad = row.get("ad").toString();
            if (commonUtil.checkAdExist(ad)) {
                log.info("域账号" + ad + "存在");
                if (commonUtil.checkAdEnable(ad)) {
                    log.info("域账号" + ad + "未禁用，无需删除");
                } else {
                    List<String> result = cmdUtil.execCmd("net user " + ad + " /delete");
                    if (result.get(0).equals("The command completed successfully.")) {
                        jdbcTemplate.update("delete from denyad where ad=?", ad);
                    } else {
                        log.info("删除失败");
                    }
                }
            } else {
                //域账号不存在，删除数据库禁用域账号记录
                jdbcTemplate.update("delete from denyad where ad=?", ad);
            }
        }
    }
}
