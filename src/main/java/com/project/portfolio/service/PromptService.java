package com.project.portfolio.service;


import com.project.portfolio.model.Prompt;
import com.project.portfolio.repository.PromptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PromptService {
    private final PromptRepository promptRepository;


    public List<Prompt> getAll() {
        return promptRepository.findAll();
    }

    public String getContentByCategory(String category) {
        return promptRepository.findByCategory(category)
                .map(Prompt::getContent)
                .orElse(null);
    }
}
