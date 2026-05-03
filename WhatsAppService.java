package com.whatsapp.scheduler.service;

import com.whatsapp.scheduler.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * WhatsApp Service
 *
 * This service manages WhatsApp Web sessions and message dispatching.
 *
 * INTEGRATION NOTE:
 * For real WhatsApp automation, this service integrates with:
 *   1. whatsapp-web.js (Node.js) running as a sidecar process
 *   2. Baileys library via REST bridge
 *   3. Official WhatsApp Business API (requires Meta approval)
 *
 * The current implementation uses the WhatsApp "wa.me" deep link for
 * direct browser-based sending and simulates session management.
 * A production deployment would use a Node.js microservice bridge.
 *
 * Architecture:
 *   Spring Boot (Java) <--> REST Bridge <--> Node.js (whatsapp-web.js)
 *                                          ^
 *                                          |
 *                                   WhatsApp Web QR Auth
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Autowired
    private WhatsAppSessionRepository sessionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String DEFAULT_SESSION = "default";

    /**
     * Initiates a new WhatsApp Web session.
     * In production: triggers QR code generation via whatsapp-web.js bridge.
     */
    public WhatsAppSession initiateSession() {
        WhatsAppSession session = sessionRepository.findBySessionId(DEFAULT_SESSION)
                .orElse(new WhatsAppSession());

        session.setSessionId(DEFAULT_SESSION);
        session.setStatus(WhatsAppSession.SessionStatus.QR_PENDING);
        session.setQrCode(generateSimulatedQRData());
        session = sessionRepository.save(session);

        // Notify connected clients via WebSocket
        broadcastSessionStatus(session);
        log.info("WhatsApp session initiated: {}", session.getSessionId());
        return session;
    }

    /**
     * Simulates QR scan confirmation.
     * In production: whatsapp-web.js fires this callback when user scans QR.
     */
    public WhatsAppSession confirmConnection(String phoneNumber) {
        WhatsAppSession session = sessionRepository.findBySessionId(DEFAULT_SESSION)
                .orElse(new WhatsAppSession());

        session.setSessionId(DEFAULT_SESSION);
        session.setStatus(WhatsAppSession.SessionStatus.CONNECTED);
        session.setPhoneNumber(phoneNumber);
        session.setConnectedAt(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());
        session = sessionRepository.save(session);

        broadcastSessionStatus(session);
        log.info("WhatsApp connected for number: {}", phoneNumber);
        return session;
    }

    /**
     * Disconnects the current WhatsApp session.
     */
    public void disconnect() {
        sessionRepository.findBySessionId(DEFAULT_SESSION).ifPresent(session -> {
            session.setStatus(WhatsAppSession.SessionStatus.DISCONNECTED);
            session.setPhoneNumber(null);
            sessionRepository.save(session);
            broadcastSessionStatus(session);
            log.info("WhatsApp session disconnected");
        });
    }

    /**
     * Returns the current session status.
     */
    public WhatsAppSession getSession() {
        return sessionRepository.findBySessionId(DEFAULT_SESSION)
                .orElseGet(() -> {
                    WhatsAppSession s = new WhatsAppSession();
                    s.setSessionId(DEFAULT_SESSION);
                    s.setStatus(WhatsAppSession.SessionStatus.DISCONNECTED);
                    return s;
                });
    }

    /**
     * Sends a WhatsApp message.
     *
     * Strategy:
     * 1. If connected via session → use whatsapp-web.js bridge (REST call)
     * 2. Fallback → generate wa.me deep link for manual dispatch
     *
     * Returns true if sent successfully, false if fallback needed.
     */
    public boolean sendMessage(String phoneNumber, String message) {
        WhatsAppSession session = getSession();

        if (session.getStatus() == WhatsAppSession.SessionStatus.CONNECTED) {
            // Production: call Node.js bridge
            // return callNodeBridge(phoneNumber, message);
            log.info("Sending message to {} via active session: {}", phoneNumber,
                    message.substring(0, Math.min(50, message.length())) + "...");
            session.setLastActive(LocalDateTime.now());
            sessionRepository.save(session);
            return true; // Simulate success
        }

        // Fallback: wa.me deep link (opens WhatsApp Web / app)
        log.info("No active session. Fallback wa.me link for: {}", phoneNumber);
        return false;
    }

    /**
     * Generates the wa.me deep link URL for manual sending fallback.
     */
    public String generateWaLink(String phoneNumber, String message) {
        String cleanPhone = phoneNumber.replaceAll("[^0-9+]", "");
        if (cleanPhone.startsWith("+")) cleanPhone = cleanPhone.substring(1);
        try {
            String encoded = java.net.URLEncoder.encode(message, "UTF-8");
            return "https://wa.me/" + cleanPhone + "?text=" + encoded;
        } catch (Exception e) {
            return "https://wa.me/" + cleanPhone;
        }
    }

    private void broadcastSessionStatus(WhatsAppSession session) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", session.getStatus().name());
        payload.put("phoneNumber", session.getPhoneNumber());
        payload.put("qrCode", session.getQrCode());
        payload.put("connectedAt", session.getConnectedAt() != null ?
                session.getConnectedAt().toString() : null);
        messagingTemplate.convertAndSend("/topic/session-status", payload);
    }

    private String generateSimulatedQRData() {
        // In production: actual QR data from whatsapp-web.js
        return "2@" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
                + "," + UUID.randomUUID().toString().replace("-", "");
    }
}
