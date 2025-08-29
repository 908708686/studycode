package cn.unis.ad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    @GetMapping("/login")
    public String showLoginPage() {
        return "login.html";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            session.setMaxInactiveInterval(60*10);
            return "redirect:/admin";
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login.html";
        }
    }

    @GetMapping("/admin")
    public String showDashboard(HttpSession session) {
        if (session.getAttribute("adminLoggedIn") == null) {
            return "redirect:/login";
        }
        return "admin.html";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}