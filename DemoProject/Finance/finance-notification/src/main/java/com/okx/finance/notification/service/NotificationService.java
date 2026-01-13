package com.okx.finance.notification.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendEmail(String to, String subject, String content) {
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content: " + content);
    }

    public void sendSMS(String phone, String message) {
        System.out.println("Sending SMS to: " + phone);
        System.out.println("Message: " + message);
    }

    public void pushNotification(Long userId, String message) {
        System.out.println("Pushing notification to user: " + userId);
        System.out.println("Message: " + message);
    }
}
