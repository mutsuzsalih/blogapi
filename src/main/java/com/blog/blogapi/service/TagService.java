package com.blog.blogapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.blogapi.dto.TagRequest;
import com.blog.blogapi.dto.TagResponse;
import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Tag;
import com.blog.blogapi.repository.TagRepository;

@Service
@Transactional
public class TagService {
    private final TagRepository tagRepository;
    private final AuthorizationService authorizationService;

    public TagService(TagRepository tagRepository, AuthorizationService authorizationService) {
        this.tagRepository = tagRepository;
        this.authorizationService = authorizationService;
    }

    public TagResponse createTag(TagRequest request) {
        authorizationService.checkAdmin();
        Tag tag = new Tag();
        tag.setName(request.getName());
        Tag saved = tagRepository.save(tag);
        return toTagResponse(saved);
    }

    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        return toTagResponse(tag);
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toTagResponse)
                .toList();
    }

    public TagResponse updateTag(Long id, TagRequest request) {
        authorizationService.checkAdmin();
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
        tag.setName(request.getName());
        Tag updated = tagRepository.save(tag);
        return toTagResponse(updated);
    }

    public void deleteTag(Long id) {
        authorizationService.checkAdmin();
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", "id", id);
        }
        tagRepository.deleteById(id);
    }

    private TagResponse toTagResponse(Tag tag) {
        TagResponse dto = new TagResponse();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }
}