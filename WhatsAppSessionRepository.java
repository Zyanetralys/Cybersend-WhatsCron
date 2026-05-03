package com.whatsapp.scheduler.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WhatsAppSessionRepository extends JpaRepository<WhatsAppSession, Long> {
    Optional<WhatsAppSession> findBySessionId(String sessionId);
    Optional<WhatsAppSession> findFirstByStatusOrderByConnectedAtDesc(WhatsAppSession.SessionStatus status);
}
