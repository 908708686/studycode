package cn.unis.ad.controller;

import cn.unis.ad.service.UpdatePwdService;
import cn.unis.ad.service.impl.UpdatePwdServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class UpdatePwdController {
    private static Logger log = LoggerFactory.getLogger(UpdatePwdController.class);
    @Resource
    UpdatePwdService updatePwdService;
    @PostMapping("/updatepwd")
    @ResponseBody
    public JSONObject UpdatePwd(@RequestBody JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        log.info("-----------------" + data.toString());
        String adname = data.get("adname").toString();
        String oldpassword = data.get("oldpassword").toString();
        String newpassword1 = data.get("newpassword1").toString();
        String newpassword2 = data.get("newpassword2").toString();
        log.info("-----------------" + data.toString());
        if (newpassword1.equals(newpassword2)) {
            if (oldpassword.equals(newpassword1)) {
                jsonObject.put("message", "新密码与原密码一致");
                jsonObject.put("status", "fail");
                return jsonObject;
            } else {
                jsonObject.put("message", updatePwdService.updatepwd(adname, oldpassword, newpassword1));
                if(jsonObject.get("message").toString().contains("密码修改成功")){
                    jsonObject.put("status", "success");
                }else {
                    jsonObject.put("status", "fail");
                }
            }
        } else {
            jsonObject.put("message", "两次输入的新密码不一致");
            jsonObject.put("status", "fail");
        }
        return jsonObject;
    }
}


