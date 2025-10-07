package com.project.portfolio.repository;

import com.project.portfolio.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    Optional<Prompt> findByCategory(String title);
}
