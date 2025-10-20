package cn.unis.ad.task;

import cn.unis.ad.util.CmdUtil;
import cn.unis.ad.util.GetMailUtil;
import cn.unis.ad.util.SendMail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class CheckPwdExpireTask extends QuartzJobBean {
    private final static Logger log = LoggerFactory.getLogger(CheckPwdExpireTask.class);
    private static final ReentrantLock lock = new ReentrantLock();
    @Value("${spring.ad.expireday}")
    private long expireday;
    @Resource
     SendMail sendExpireMail;
    @Resource
     GetMailUtil getMailUtil;
    @Resource
     CmdUtil cmdUtil;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 打印任务运行信息
        try {
            lock.lock();
            CheckPwd();
        } finally {
            lock.unlock();
        }

    }
    public void CheckPwd() {
        //查看10000天未修改密码的账号
//        List<String> endstrs = execcmd("dsquery user -limit 0 -o samid -stalepwd 1000");
        List<String> endstrs = cmdUtil.execCmd("dsquery user -limit 0");
        for (String str : endstrs) {
            str = str.replace("\"", "");
            log.debug("str:" + str);
            List<String> endstrs1 = cmdUtil.execCmd("dsget user " + str + " -disabled");
            if (endstrs1.size() > 0) {
                log.debug("endstrs1：" + endstrs1);
                if (endstrs1.get(1).contains("yes")) {
                    log.debug(str + "账号已禁用");
                } else if (endstrs1.get(1).contains("no")) {
                    log.debug(str + "账号未禁用");
                    List<String> endstrs2 = cmdUtil.execCmd("dsget user " + str + " -samid");
                    String samid=endstrs2.get(1).trim();
                    long adExpirday = getAdExpirDate(samid);
                    if (adExpirday > 0) {
                        log.debug(samid + "账号未过期"+"adExpirday:"+adExpirday+"expireday:"+expireday);
                        if (adExpirday < expireday) {
                            log.debug(samid+"邮箱为："+getMailUtil.getMailAddress(samid));
                            sendExpireMail.SendExpireMai(getMailUtil.getMailAddress(samid), MailContent(samid, adExpirday));
                        } else {
                            log.debug(samid + "账号未到达密码过期时间预警，无须发送邮件");
                        }
                    } else {
                        log.debug(samid + "账号已过期");
                    }
                }
            } else {
                log.debug(str + "获取账号信息失败");
            }
        }
    }

    public long getAdExpirDate(String samid) {
        List<String> tts = cmdUtil.execCmd("net user " + samid);
        if (tts.size() > 0) {
            log.debug("获取账号信息成功");
            if (tts.get(9).contains("Never")) {
                return -1;
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                try {
                    Date date = inputFormat.parse(tts.get(9).substring(18));
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd");
                    String formattedDate = outputFormat.format(date);
                    System.out.println("转换后的日期：" + formattedDate);

                    Calendar currentCalendar = Calendar.getInstance();
                    Calendar expiryCalendar = Calendar.getInstance();
                    expiryCalendar.setTime(date);

                    long daysDifference = (expiryCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                    System.out.println("与当前时间相差天数：" + daysDifference);
                    return daysDifference;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public String MailContent(String samid, long adExpirday) {
        return "<p>\n" +
                "<span style=\"font-size:16px;font-family:\"\"><strong>" + samid + "</strong></span>" +
                "<span style=\"font-size:16px;font-family:\"\">您好,</span>" +
                "</p>" +
                "<p class=\"MsoNormal\" align=\"left\" style=\"text-align:justify;font-size:10.5pt;font-family:Calibri, sans-serif;\">" +
                "<span style=\"font-size:16px;font-family:\"\">您的域账号</span>" +
                "<span style=\"font-size:16px;color:#E53333;font-family:\"\"><strong>" + samid + "</strong></span>" +
                "<span style=\"font-size:16px;font-family:\"\">密码将在</span>" +
                "<span style=\"font-size:16px;color:#E53333;font-family:\"\"><strong>" + adExpirday + "</strong></span>" +
                "<span style=\"font-size:16px;font-family:\"\">天后过期，密码过期后域账号将不可使用,请登陆下方平台进行更改：</span>" +
                "</p>" +
                "<p>" +
                "<span style=\"font-size:16px;font-family:\"\">http://adpwd.unisfuture.cn</span>" +
                "</p>" +
                "<p>" +
                "<br/>" +
                "</p>";
    }
}
