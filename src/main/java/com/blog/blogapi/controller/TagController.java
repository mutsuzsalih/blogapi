package com.blog.blogapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.blogapi.dto.TagRequest;
import com.blog.blogapi.dto.TagResponse;
import com.blog.blogapi.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag Management", description = "APIs for managing tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "Create a new tag", description = "Creates a new tag with the provided name")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tag by ID", description = "Retrieves tag details by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        TagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all tags", description = "Retrieves a list of all tags")
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> responses = tagService.getAllTags();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Update tag", description = "Updates an existing tag with new details")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> updateTag(@PathVariable Long id, @RequestBody TagRequest request) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete tag", description = "Deletes a tag by its ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}