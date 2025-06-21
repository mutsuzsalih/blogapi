package com.blog.blogapi.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blog.blogapi.dto.TagRequest;
import com.blog.blogapi.dto.TagResponse;
import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Tag;
import com.blog.blogapi.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private TagRequest tagRequest;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(1L);
        tag.setName("java");

        tagRequest = new TagRequest();
        tagRequest.setName("spring");
    }

    @Test
    @DisplayName("getAllTags should return all tags")
    void getAllTags_shouldReturnAllTags() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<TagResponse> responses = tagService.getAllTags();

        assertEquals(1, responses.size());
        assertEquals("java", responses.get(0).getName());
        verify(tagRepository).findAll();
    }

    @Test
    @DisplayName("getTagById should return tag when found")
    void getTagById_whenFound_shouldReturnTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        TagResponse response = tagService.getTagById(1L);

        assertNotNull(response);
        assertEquals("java", response.getName());
        verify(tagRepository).findById(1L);
    }

    @Test
    @DisplayName("getTagById should throw exception when not found")
    void getTagById_whenNotFound_shouldThrowException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> tagService.getTagById(1L));
        assertEquals("Tag not found with id : '1'", exception.getMessage());
        verify(tagRepository).findById(1L);
    }

    @Test
    @DisplayName("createTag should create a tag when admin is authorized")
    void createTag_whenAdminAuthorized_shouldCreateTag() {
        doNothing().when(authorizationService).checkAdmin();
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        TagRequest request = new TagRequest();
        request.setName("New Tag");

        TagResponse response = tagService.createTag(request);

        assertNotNull(response);
        assertEquals("java", response.getName());
        verify(authorizationService).checkAdmin();
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("updateTag should update a tag when admin is authorized")
    void updateTag_whenAdminAuthorized_shouldUpdateTag() {
        doNothing().when(authorizationService).checkAdmin();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        TagRequest request = new TagRequest();
        request.setName("spring");

        TagResponse response = tagService.updateTag(1L, request);

        assertNotNull(response);
        assertEquals("spring", response.getName());
        verify(authorizationService).checkAdmin();
        verify(tagRepository).findById(1L);
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("updateTag should throw ResourceNotFoundException when tag not found")
    void updateTag_whenTagNotFound_shouldThrowException() {
        doNothing().when(authorizationService).checkAdmin();
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        TagRequest request = new TagRequest();
        request.setName("Non-existent Tag");

        var exception = assertThrows(ResourceNotFoundException.class, () -> tagService.updateTag(1L, request));
        assertEquals("Tag not found with id : '1'", exception.getMessage());
        verify(authorizationService).checkAdmin();
        verify(tagRepository).findById(1L);
    }

    @Test
    @DisplayName("deleteTag should delete a tag when admin is authorized")
    void deleteTag_whenAdminAuthorized_shouldDeleteTag() {
        doNothing().when(authorizationService).checkAdmin();
        when(tagRepository.existsById(1L)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(1L);

        tagService.deleteTag(1L);

        verify(authorizationService).checkAdmin();
        verify(tagRepository).existsById(1L);
        verify(tagRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTag should throw ResourceNotFoundException when tag not found")
    void deleteTag_whenTagNotFound_shouldThrowException() {
        doNothing().when(authorizationService).checkAdmin();
        when(tagRepository.existsById(1L)).thenReturn(false);

        var exception = assertThrows(ResourceNotFoundException.class, () -> tagService.deleteTag(1L));
        assertEquals("Tag not found with id : '1'", exception.getMessage());
        verify(authorizationService).checkAdmin();
        verify(tagRepository).existsById(1L);
    }
}