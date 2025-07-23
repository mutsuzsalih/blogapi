package com.blog.blogapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.blogapi.model.LocalizationMessage;
import com.blog.blogapi.repository.LocalizationMessageRepository;

@Service
@Transactional
public class LocalizationService {

    private final LocalizationMessageRepository messageRepository;

    @Autowired
    public LocalizationService(LocalizationMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Cacheable(value = "localization", key = "#key + '_' + #locale")
    public String getMessage(String key, String locale) {
        Optional<LocalizationMessage> message = messageRepository.findByKeyAndLocale(key, locale);

        if (message.isPresent()) {
            return message.get().getValue();
        }

        // Fallback to default locale (English)
        if (!"en".equals(locale)) {
            Optional<LocalizationMessage> fallback = messageRepository.findByKeyAndLocale(key, "en");
            if (fallback.isPresent()) {
                return fallback.get().getValue();
            }
        }

        // Return key if no translation found
        return key;
    }

    @Cacheable(value = "localization-all", key = "#locale")
    public List<LocalizationMessage> getAllMessages(String locale) {
        return messageRepository.findByLocale(locale);
    }

    @CacheEvict(value = { "localization", "localization-all" }, allEntries = true)
    public LocalizationMessage saveMessage(LocalizationMessage message) {
        return messageRepository.save(message);
    }

    @CacheEvict(value = { "localization", "localization-all" }, allEntries = true)
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public boolean messageExists(String key, String locale) {
        return messageRepository.existsByKeyAndLocale(key, locale);
    }
}