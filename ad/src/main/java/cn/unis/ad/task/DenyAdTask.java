package cn.unis.ad.task;

import cn.hutool.http.HttpRequest;
import cn.unis.ad.util.CmdUtil;
import cn.unis.ad.util.CommonUtil;
import cn.unis.ad.util.GetToken;
import com.alibaba.fastjson.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DenyAdTask extends QuartzJobBean {
    private final static Logger log = LoggerFactory.getLogger(DenyAdTask.class);
    private static final ReentrantLock lock = new ReentrantLock();
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    GetToken getToken;
    @Resource
    CmdUtil  cmdUtil;
    @Resource
    CommonUtil commonUtil;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 打印任务运行信息
        try {
            lock.lock();
            String co = context.getMergedJobDataMap().getString("param");
            denyAd(co);
        } finally {
            lock.unlock();
        }

    }
    //离职域账号禁用
    public void denyAd(String co) {
        String  sql = "select distinct ou from dept where ou is not null and co=?";
        List<Map<String, Object>> sqlres = jdbcTemplate.queryForList(sql, co);
        for (Map<String, Object> row : sqlres) {
            String ou = row.get("ou").toString();
            List<String> ad_list = cmdUtil.execCmd("dsquery user " + ou + " -scope onelevel -o samid -limit 100000");
            for (String adname : ad_list) {
                adname=adname.replace("\"", "");
                if (commonUtil.checkAdEnable(adname)) {
                    String userid = switchAdnameToUserid(adname);
                    userid = userid.replace("\"", "");
                    if (checkUseridExist(co, userid)) {
                        log.info("userid:" + userid + "存在，不处理");
                    } else {
                        log.info("企业微信userid:" + userid + "不存在");
                        if (NewAccountExclude(adname)){
                            log.info("userid:" + userid + "不存在，但是是新账号，不处理");
                        }else {
                            log.info("userid:" + userid + "不存在，准备禁用adname");
                            sql="select ad from denyad where ad='"+adname+"'";
                            sqlres = jdbcTemplate.queryForList(sql);
                            if (sqlres.size()>0){
                                log.info("adname:"+adname+"在denyad表中已存在，不需要再次禁用");
                            }else {
                                List<String> cmdres = cmdUtil.execCmd("net user " + adname + " /active:no");
                                if (cmdres.get(0).equals("The command completed successfully.")) {
                                    log.info("禁用成功");
                                    LocalDate currentDate = LocalDate.now();
                                    jdbcTemplate.update("insert into denyad (ad,denytime)values(?,?);)", adname, currentDate);
                                } else {
                                    log.info("禁用失败");
                                }
                            }
                        }
                    }
                } else {
                    log.info("域账号" + adname + "已禁用");
                }
            }
        }
    }

    //将域控adname转换为userid
    public String switchAdnameToUserid(String adname) {
        String sql = "select userid from relation where ad='"+adname+"' LIMIT 1";
        List<Map<String, Object>> sqlres = jdbcTemplate.queryForList(sql);
        if (sqlres.size() > 0) {
            return sqlres.get(0).get("userid").toString();
        }
        return adname;
    }

    //判断企业微信userid是否存在
    public boolean checkUseridExist(String co, String userid) {
        Map<String, Object> params = new HashMap<>();
        String access_token = getToken.getAccessTokenCache(String.valueOf(co));
        params.put("access_token", access_token);
        params.put("userid", userid);
        String reqres = HttpRequest.get("https://qyapi.weixin.qq.com/cgi-bin/user/get").form(params).execute().body();
        log.debug("userid:" + userid + "报错信息为" + reqres);
        JSONObject jsonObject = JSONObject.parseObject(reqres);
        String errcode = jsonObject.getString("errcode");
        log.debug("userid:" + userid + "报错码为" + errcode);
        if (errcode.equals("60111")) {
            return false;
        } else if (errcode.equals("0")) {
            log.info("userid:" + userid + "存在");
            return true;
        } else {
            log.info("userid:" + userid + "其他报错，报错码为" + errcode);
            return true;
        }
    }
    //为新账号设置7天免禁用规则
    public boolean NewAccountExclude(String adname) {
        String cmd ="Get-ADUser -Identity "+adname+" -Properties whenCreated | Select-Object whenCreated";
        List<String> cmdres = cmdUtil.execCmd(cmd);
        String dateStr=cmdres.get(3);
        log.debug("域账号创建日期为：" + dateStr);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
        LocalDateTime givenDate = LocalDateTime.parse(dateStr, formatter);
        LocalDateTime now = LocalDateTime.now();
        long daysDifference = ChronoUnit.DAYS.between(now, givenDate);
        if (Math.abs(daysDifference) < 7) {
            log.debug("创建日期相差不到7天");
            return true;
        } else {
            log.debug("创建日期相差超过7天");
            return false;
        }
    }
}
