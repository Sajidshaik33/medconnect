package com.medconnect.service;

import com.medconnect.model.Notification;
import com.medconnect.model.User;
import com.medconnect.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Create notification
    public Notification createNotification(User user, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    // Get all notifications for user
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    // Get unread count
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // Mark as read
    public void markAsRead(Long id) {
        Notification notification = notificationRepository
                .findById(id).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    // Mark all as read
    public void markAllAsRead(User user) {
        List<Notification> unread = getUnreadNotifications(user);
        for (Notification n : unread) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }

    // Delete notification
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}