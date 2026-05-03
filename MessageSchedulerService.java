package com.whatsapp.scheduler.service;

import com.whatsapp.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class MessageSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(MessageSchedulerService.class);

    @Autowired
    private ScheduledMessageRepository messageRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Scheduler runs every 30 seconds to check for due messages.
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processDueMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledMessage> dueMessages = messageRepository.findDueMessages(now);

        if (!dueMessages.isEmpty()) {
            log.info("Processing {} due messages", dueMessages.size());
        }

        for (ScheduledMessage msg : dueMessages) {
            processMessage(msg);
        }
    }

    @Transactional
    public void processMessage(ScheduledMessage msg) {
        msg.setStatus(ScheduledMessage.MessageStatus.SENDING);
        messageRepository.save(msg);
        broadcastMessageUpdate(msg);

        try {
            boolean sent = whatsAppService.sendMessage(msg.getPhoneNumber(), msg.getMessageContent());

            if (sent) {
                msg.setStatus(ScheduledMessage.MessageStatus.SENT);
                msg.setSentAt(LocalDateTime.now());
                log.info("Message {} sent successfully to {}", msg.getId(), msg.getPhoneNumber());
            } else {
                // Generate wa.me link as fallback
                String waLink = whatsAppService.generateWaLink(msg.getPhoneNumber(), msg.getMessageContent());
                msg.setStatus(ScheduledMessage.MessageStatus.PENDING);
                msg.setErrorMessage("FALLBACK_LINK:" + waLink);
                log.info("Message {} requires manual send via: {}", msg.getId(), waLink);
            }

            // Handle recurrence
            if (sent && msg.getRecurrence() != ScheduledMessage.RecurrenceType.NONE) {
                scheduleNextRecurrence(msg);
            }

        } catch (Exception e) {
            msg.setStatus(ScheduledMessage.MessageStatus.FAILED);
            msg.setErrorMessage(e.getMessage());
            log.error("Failed to send message {}: {}", msg.getId(), e.getMessage());
        }

        messageRepository.save(msg);
        broadcastMessageUpdate(msg);
    }

    public ScheduledMessage createMessage(ScheduledMessage message) {
        message.setStatus(ScheduledMessage.MessageStatus.PENDING);
        ScheduledMessage saved = messageRepository.save(message);
        broadcastMessageUpdate(saved);
        log.info("Message scheduled: id={}, to={}, at={}",
                saved.getId(), saved.getPhoneNumber(), saved.getScheduledAt());
        return saved;
    }

    public ScheduledMessage sendNow(Long id) {
        ScheduledMessage msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));
        msg.setScheduledAt(LocalDateTime.now());
        messageRepository.save(msg);
        processMessage(msg);
        return msg;
    }

    public ScheduledMessage cancelMessage(Long id) {
        ScheduledMessage msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));
        msg.setStatus(ScheduledMessage.MessageStatus.CANCELLED);
        ScheduledMessage saved = messageRepository.save(msg);
        broadcastMessageUpdate(saved);
        return saved;
    }

    public List<ScheduledMessage> getAllMessages() {
        return messageRepository.findAllByOrderByScheduledAtDesc();
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", messageRepository.count());
        stats.put("pending", messageRepository.countByStatus(ScheduledMessage.MessageStatus.PENDING));
        stats.put("sent", messageRepository.countByStatus(ScheduledMessage.MessageStatus.SENT));
        stats.put("failed", messageRepository.countByStatus(ScheduledMessage.MessageStatus.FAILED));
        stats.put("cancelled", messageRepository.countByStatus(ScheduledMessage.MessageStatus.CANCELLED));
        return stats;
    }

    private void scheduleNextRecurrence(ScheduledMessage original) {
        ScheduledMessage next = new ScheduledMessage();
        next.setPhoneNumber(original.getPhoneNumber());
        next.setMessageContent(original.getMessageContent());
        next.setContactName(original.getContactName());
        next.setRecurrence(original.getRecurrence());
        next.setTimezone(original.getTimezone());

        LocalDateTime nextTime = switch (original.getRecurrence()) {
            case DAILY -> original.getScheduledAt().plusDays(1);
            case WEEKLY -> original.getScheduledAt().plusWeeks(1);
            case MONTHLY -> original.getScheduledAt().plusMonths(1);
            default -> null;
        };

        if (nextTime != null) {
            next.setScheduledAt(nextTime);
            messageRepository.save(next);
            log.info("Recurring message scheduled: next at {}", nextTime);
        }
    }

    private void broadcastMessageUpdate(ScheduledMessage msg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", msg.getId());
        payload.put("status", msg.getStatus().name());
        payload.put("phoneNumber", msg.getPhoneNumber());
        payload.put("contactName", msg.getContactName());
        payload.put("scheduledAt", msg.getScheduledAt() != null ? msg.getScheduledAt().toString() : null);
        payload.put("sentAt", msg.getSentAt() != null ? msg.getSentAt().toString() : null);
        payload.put("errorMessage", msg.getErrorMessage());
        messagingTemplate.convertAndSend("/topic/message-updates", payload);
    }
}
