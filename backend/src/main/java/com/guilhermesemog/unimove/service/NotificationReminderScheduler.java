package com.guilhermesemog.unimove.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "unimove.phase-five.notifications", name = "reminders-enabled", havingValue = "true")
public class NotificationReminderScheduler {
    private final NotificationReminderService reminderService;

    public NotificationReminderScheduler(NotificationReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "${unimove.phase-five.notifications.reminder-cron:0 0 8 * * *}",
            zone = "${unimove.business-time-zone:America/Sao_Paulo}")
    public void createDueNotifications() {
        reminderService.createDueNotifications();
    }
}
