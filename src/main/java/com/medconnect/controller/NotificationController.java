package com.medconnect.controller;

import com.medconnect.model.User;
import com.medconnect.service.NotificationService;
import com.medconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewNotifications(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("notifications",
                notificationService.getNotificationsByUser(user));
            model.addAttribute("unreadCount",
                notificationService.getUnreadCount(user));
            notificationService.markAllAsRead(user);
        }
        model.addAttribute("user", user);
        return "notifications";
    }

    @GetMapping("/mark-read/{id}")
    public String markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @GetMapping("/delete/{id}")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return "redirect:/notifications";
    }
}