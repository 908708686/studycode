package cn.unis.ad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ResetController {
    @RequestMapping("/reset")
    public String index() {
        return "reset.html";
    }
}


