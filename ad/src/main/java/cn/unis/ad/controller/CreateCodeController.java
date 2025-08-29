package cn.unis.ad.controller;

import cn.unis.ad.service.CreateCodeService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class CreateCodeController {
    private final static Logger log = LoggerFactory.getLogger(CreateCodeController.class);
    @Resource
    CreateCodeService createCodeService ;
    @PostMapping("/createcode")
    @ResponseBody
    public JSONObject CreateCode(@RequestBody JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "success");
        String adname = data.get("adname").toString();
        String message =createCodeService.createCode(adname);
        jsonObject.put("message", message);
        return jsonObject;
    }
}


