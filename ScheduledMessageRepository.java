package com.whatsapp.scheduler.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledMessageRepository extends JpaRepository<ScheduledMessage, Long> {

    List<ScheduledMessage> findByStatusOrderByScheduledAtAsc(ScheduledMessage.MessageStatus status);

    @Query("SELECT m FROM ScheduledMessage m WHERE m.status = 'PENDING' AND m.scheduledAt <= :now ORDER BY m.scheduledAt ASC")
    List<ScheduledMessage> findDueMessages(LocalDateTime now);

    List<ScheduledMessage> findAllByOrderByScheduledAtDesc();

    long countByStatus(ScheduledMessage.MessageStatus status);
}
