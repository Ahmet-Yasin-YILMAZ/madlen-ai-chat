package com.example.bootcamptoprod.repository;

import com.example.bootcamptoprod.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
}
