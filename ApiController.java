package com.whatsapp.scheduler.controller;

import com.whatsapp.scheduler.model.ScheduledMessage;
import com.whatsapp.scheduler.model.WhatsAppSession;
import com.whatsapp.scheduler.service.MessageSchedulerService;
import com.whatsapp.scheduler.service.WhatsAppService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private MessageSchedulerService schedulerService;

    @Autowired
    private WhatsAppService whatsAppService;

    // ── Messages ──────────────────────────────────────────────

    @GetMapping("/messages")
    public ResponseEntity<List<ScheduledMessage>> getMessages() {
        return ResponseEntity.ok(schedulerService.getAllMessages());
    }

    @PostMapping("/messages")
    public ResponseEntity<ScheduledMessage> createMessage(@Valid @RequestBody ScheduledMessage message) {
        return ResponseEntity.ok(schedulerService.createMessage(message));
    }

    @PostMapping("/messages/{id}/send-now")
    public ResponseEntity<ScheduledMessage> sendNow(@PathVariable Long id) {
        return ResponseEntity.ok(schedulerService.sendNow(id));
    }

    @PostMapping("/messages/{id}/cancel")
    public ResponseEntity<ScheduledMessage> cancelMessage(@PathVariable Long id) {
        return ResponseEntity.ok(schedulerService.cancelMessage(id));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        schedulerService.getAllMessages().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .ifPresent(m -> {});
        // Direct delete
        try {
            schedulerService.cancelMessage(id);
        } catch (Exception ignored) {}
        return ResponseEntity.noContent().build();
    }

    // ── WhatsApp Session ──────────────────────────────────────

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession() {
        WhatsAppSession session = whatsAppService.getSession();
        return ResponseEntity.ok(sessionToMap(session));
    }

    @PostMapping("/session/connect")
    public ResponseEntity<Map<String, Object>> connect() {
        WhatsAppSession session = whatsAppService.initiateSession();
        return ResponseEntity.ok(sessionToMap(session));
    }

    @PostMapping("/session/confirm")
    public ResponseEntity<Map<String, Object>> confirmConnection(@RequestBody Map<String, String> body) {
        String phone = body.getOrDefault("phoneNumber", "");
        WhatsAppSession session = whatsAppService.confirmConnection(phone);
        return ResponseEntity.ok(sessionToMap(session));
    }

    @PostMapping("/session/disconnect")
    public ResponseEntity<Map<String, String>> disconnect() {
        whatsAppService.disconnect();
        return ResponseEntity.ok(Map.of("status", "disconnected"));
    }

    // ── Stats & Utilities ─────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(schedulerService.getStats());
    }

    @GetMapping("/server-time")
    public ResponseEntity<Map<String, Object>> getServerTime() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", LocalDateTime.now().toString());
        result.put("timezone", ZoneId.systemDefault().getId());
        result.put("availableZones", ZoneId.getAvailableZoneIds().stream()
                .sorted().limit(100).toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/wa-link")
    public ResponseEntity<Map<String, String>> getWaLink(
            @RequestParam String phone,
            @RequestParam(defaultValue = "") String message) {
        String link = whatsAppService.generateWaLink(phone, message);
        return ResponseEntity.ok(Map.of("link", link));
    }

    // ── Health ────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "version", "1.0.0",
                "time", LocalDateTime.now().toString()
        ));
    }

    private Map<String, Object> sessionToMap(WhatsAppSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", session.getStatus().name());
        map.put("phoneNumber", session.getPhoneNumber());
        map.put("qrCode", session.getQrCode());
        map.put("connectedAt", session.getConnectedAt() != null ?
                session.getConnectedAt().toString() : null);
        map.put("lastActive", session.getLastActive() != null ?
                session.getLastActive().toString() : null);
        return map;
    }
}
