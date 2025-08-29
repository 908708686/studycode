package cn.unis.ad.controller;

import cn.unis.ad.service.CheckCodeService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class CheckCodeController {
    private static Logger log = LoggerFactory.getLogger(CheckCodeController.class);
    @Resource
    CheckCodeService checkCodeService;
    @PostMapping("/checkcode")
    @ResponseBody
    public JSONObject CheckCode(@RequestBody JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "success");
        String adname = data.get("adname").toString();
        String code = data.get("code").toString();
        jsonObject.put("message", checkCodeService.checkCode(adname, code));
        return jsonObject;
    }
}


