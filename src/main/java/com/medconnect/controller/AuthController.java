package com.medconnect.controller;

import com.medconnect.model.User;
import com.medconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute("user") User user,
            Model model) {
        try {
            if (userService.emailExists(user.getEmail())) {
                model.addAttribute("error", "Email already registered!");
                model.addAttribute("user", user);
                return "auth/register";
            }
            userService.registerUser(user);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null) {
            if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                return "redirect:/admin/dashboard";
            else if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")))
                return "redirect:/doctor/dashboard";
            else if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")))
                return "redirect:/patient/dashboard";
        }
        return "redirect:/login";
    }
    @GetMapping("/generate-hash")
    public void generateHash(
            jakarta.servlet.http.HttpServletResponse response,
            @RequestParam String password) throws Exception {
        String hash = userService.encodePassword(password);
        response.getWriter().write(hash);
    }
}