package com.blog.blogapi.model;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class TagValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNameCannotBeBlank() {
        Tag tag = new Tag();
        tag.setName("");

        Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testNameMaxLength() {
        Tag tag = new Tag();
        tag.setName("a".repeat(51)); // 51 karakter

        Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
} 