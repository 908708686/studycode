package cn.unis.ad.controller;

import cn.unis.ad.service.DeptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class TestController {
    private static Logger log = LoggerFactory.getLogger(TestController.class);
    @Resource
    DeptService DeptService;
    @GetMapping("/test")
    public String GetDeptList(HttpSession session, Model model) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/login";
        }
        dataList=DeptService.GetDeptList();
        model.addAttribute("dataList", dataList);
        return "test.html";
    }
}


