package cn.unis.ad.controller;

import cn.unis.ad.service.DeptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class DeptController {
    List<Map<String, Object>> dataList = new ArrayList<>();
    @Resource
    DeptService DeptService;
    @GetMapping("/dept")
    public String getDeptList(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/login";
        }
        dataList=DeptService.GetDeptList();
        model.addAttribute("dataList", dataList);
        return "dept.html";
    }
    @PostMapping("/updatedept")
    public String updateDept(@RequestParam String co, @RequestParam String dept,@RequestParam String ou) {
        DeptService.updateDept(co,dept,ou);
        return "redirect:/dept";
    }

}
