package com.blog.blogapi.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorUsername;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
}