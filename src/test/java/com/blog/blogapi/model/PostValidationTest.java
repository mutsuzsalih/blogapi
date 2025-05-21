package com.blog.blogapi.model;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class PostValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testTitleCannotBeBlank() {
        Post post = new Post();
        post.setTitle("");
        post.setContent("This is a valid content.");

        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testTitleMaxLength() {
        Post post = new Post();
        post.setTitle("a".repeat(256)); // 256 karakter
        post.setContent("This is a valid content.");

        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void testContentCannotBeBlank() {
        Post post = new Post();
        post.setTitle("Valid Title");
        post.setContent("");

        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void testContentMinLength() {
        Post post = new Post();
        post.setTitle("Valid Title");
        post.setContent("short"); // 5 karakter

        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }
} 