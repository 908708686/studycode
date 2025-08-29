package cn.unis.ad.controller;

import cn.unis.ad.service.RelationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class RelationController {
    List<Map<String, Object>> dataList = new ArrayList<>();
    @Resource
    RelationService relationService;
    @GetMapping("/relation")
    public String getRelationList(HttpSession session, Model model) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/login";
        }
        dataList=relationService.GetRelationList();
        model.addAttribute("dataList", dataList);
        return "relation.html";
    }
    @PostMapping("/addrelation")
    public String addRelation(@RequestParam String co, @RequestParam String ad,@RequestParam String userid) {
        relationService.addRelation(co,ad,userid);
        return "redirect:/relation";
    }
    @PostMapping("/updaterelation")
    public String updateRelation(@RequestParam String co, @RequestParam String ad,@RequestParam String userid) {
        relationService.updateRelation(co,ad,userid);
        return "redirect:/relation";
    }
    @PostMapping("/delrelation")
    public String deleteRelation(@RequestParam String ad) {
        relationService.deleteRelation(ad);
        return "redirect:/relation";
    }

}
