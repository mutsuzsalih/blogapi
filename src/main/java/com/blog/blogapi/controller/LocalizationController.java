package com.blog.blogapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog.blogapi.model.LocalizationMessage;
import com.blog.blogapi.service.LocalizationService;

@RestController
@RequestMapping("/api/localization")
public class LocalizationController {

    private final LocalizationService localizationService;

    @Autowired
    public LocalizationController(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    @GetMapping("/messages/{locale}")
    public ResponseEntity<Map<String, String>> getMessages(@PathVariable String locale) {
        List<LocalizationMessage> messages = localizationService.getAllMessages(locale);

        Map<String, String> messageMap = messages.stream()
                .collect(Collectors.toMap(
                        LocalizationMessage::getKey,
                        LocalizationMessage::getValue));

        return ResponseEntity.ok(messageMap);
    }

    @GetMapping("/message")
    public ResponseEntity<String> getMessage(
            @RequestParam String key,
            @RequestParam(defaultValue = "en") String locale) {

        String message = localizationService.getMessage(key, locale);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocalizationMessage> createMessage(@RequestBody LocalizationMessage message) {
        LocalizationMessage savedMessage = localizationService.saveMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    @PutMapping("/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocalizationMessage> updateMessage(
            @PathVariable Long id,
            @RequestBody LocalizationMessage message) {

        message.setId(id);
        LocalizationMessage updatedMessage = localizationService.saveMessage(message);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        localizationService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }
}