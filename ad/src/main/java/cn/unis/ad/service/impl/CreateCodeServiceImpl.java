package cn.unis.ad.service.impl;

import cn.unis.ad.service.CreateCodeService;
import cn.unis.ad.util.CommonUtil;
import cn.unis.ad.util.GetMailUtil;
import cn.unis.ad.util.SendMail;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;

@Service
public class CreateCodeServiceImpl implements CreateCodeService {

    private final static Logger log = LoggerFactory.getLogger(CreateCodeServiceImpl.class);

    @Resource
    GetMailUtil getMailUtil;
    @Resource
    private Cache<String, Object> caffeineCache;
    @Resource
    private SendMail sendMail;
    @Resource
    private CommonUtil cmmonUtil;

    @Override
    public String createCode(String adname) {
        String message = null;
        if (cmmonUtil.checkAdExist(adname)) {
            String code = randomCode(adname);
            String mailaddress=getMailUtil.getMailAddress(adname);
            if (mailaddress == null) {
                message="未找到该ad账号的邮箱";
            } else {
                String mailMsg = adname + " 您好!您的域账号验证码是：" +
                        code + " 您正在使用重置密码功能，验证码提供他人可能导致域账号被盗，请勿转发或泄漏。";
                sendMail.SendExpireMai(mailaddress, mailMsg);
                message="验证码已发送至" + mailaddress;
            }
        }
        return message;
    }

    public String randomCode(String adname) {
        // 生成随机验证码
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000); // 生成100000到999999之间的随机数
        String code = String.valueOf(randomNumber);
        caffeineCache.put(adname, code);
        return code;
    }
}