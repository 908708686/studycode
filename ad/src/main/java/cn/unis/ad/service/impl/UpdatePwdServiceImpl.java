package cn.unis.ad.service.impl;

import cn.unis.ad.controller.IndexController;
import cn.unis.ad.service.UpdatePwdService;
import cn.unis.ad.util.CmdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpdatePwdServiceImpl implements UpdatePwdService {
    private static Logger log = LoggerFactory.getLogger(IndexController.class);
    @Resource
    CmdUtil cmdUtil;
    @Override
    public String updatepwd(String adname, String oldpwd, String newpwd) {
        List<String> reslist = null;
        String cmd=null;
        String message = "";
        if (RegexPwd(newpwd)) {
            String tt=checkOldPwd(adname, oldpwd);
            if (tt.contains("The command completed successfully.")) {
                cmd="Set-ADAccountPassword -Identity "+adname+" -NewPassword (ConvertTo-SecureString -AsPlainText '"+newpwd+"' -Force)";
                reslist = cmdUtil.execCmd(cmd);
                if (reslist.size()>0) {
                    for(int i = 0; i < reslist.size(); i++){
                        log.info(reslist.get(i));
                    }
                    message = reslist.get(0);
                    log.info("密码修改失败"+message);
                } else {
                    message = "密码修改成功";
                }
            } else {
                message = "旧密码错误";
                log.info(message);
            }
        } else {
            // 添加键值对
            message = "密码不符合规则";
        }

        return message;
    }

    // 正则验证密码
    public boolean RegexPwd(String password) {
        String emailRegex = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@!%*?&_.])[A-Za-z\\d@!%*?&_.]{8,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        // 创建匹配器
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    //验证旧密码是否正确
    public String checkOldPwd(String adname, String oldpwd) {
        String tt = "";
        cmdUtil.execCmd("net use \\\\127.0.0.1 /delete");
        List<String> reslist = cmdUtil.execCmd("net use \\\\127.0.0.1 /user:" + adname + " " + oldpwd);
        try {
            tt = reslist.get(0);
        } catch (Exception e) {
            tt = "密码错误";
        }
        return tt;
    }
}
