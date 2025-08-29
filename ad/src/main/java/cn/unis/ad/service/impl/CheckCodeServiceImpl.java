package cn.unis.ad.service.impl;

import cn.unis.ad.controller.CheckCodeController;
import cn.unis.ad.service.CheckCodeService;
import cn.unis.ad.util.CmdUtil;
import cn.unis.ad.util.CommonUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.List;

@Service
public class CheckCodeServiceImpl implements CheckCodeService {
    private static Logger log = LoggerFactory.getLogger(CheckCodeController.class);
    @Resource
    private Cache<String, Object> caffeineCache;
    @Resource
    private CmdUtil cmdUtil;
    @Autowired
    private CommonUtil commonUtil;
    public String checkCode(String adname, String code) {
        String message = null;
        if (commonUtil.checkAdExist(adname)) {
            if (commonUtil.checkAdEnable(adname)) {
                String c = (String) caffeineCache.getIfPresent(adname);
                log.info("验证码为：" + c+"，输入的验证码为：" + code+"，域账号为：" + adname);
                if (code.equals(c)) {
                    log.info("验证码正确");
                    String newPwd = randomPwd();
                    String cmd="Set-ADAccountPassword -Identity "+adname+" -NewPassword (ConvertTo-SecureString -AsPlainText '"+newPwd+"' -Force)";
                    List<String> reslist = cmdUtil.execCmd(cmd);
                    if (reslist.size()>0) {
                        message="密码重置失败";
                    } else {
                        message="新密码为："+newPwd;
                    }

                } else {
                    log.info("验证码错误");
                }
            } else {
                log.info("域账号已禁用");
            }
        } else {
            log.info("域账号不存在");
        }
        return message;
    }

    public String randomPwd() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int pwdlength = 16;
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder password = new StringBuilder(pwdlength);
        for (int i = 0; i < pwdlength; i++) {
            int index = RANDOM.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return "Zg2024"+password.toString();
    }
}

