package com.blog.blogapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "localization_messages")
public class LocalizationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Message key cannot be blank")
    @Size(max = 255)
    @Column(name = "message_key", nullable = false)
    private String key;

    @NotBlank(message = "Locale cannot be blank")
    @Size(max = 10)
    @Column(name = "locale", nullable = false)
    private String locale;

    @NotBlank(message = "Message value cannot be blank")
    @Column(name = "message_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    // Constructors
    public LocalizationMessage() {
    }

    public LocalizationMessage(String key, String locale, String value) {
        this.key = key;
        this.locale = locale;
        this.value = value;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}