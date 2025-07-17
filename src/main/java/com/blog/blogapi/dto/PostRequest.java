package com.blog.blogapi.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank(message = "{validation.title.notblank}")
    @Size(max = 255, message = "{validation.title.size}")
    private String title;

    @NotBlank(message = "{validation.content.notblank}")
    @Size(min = 10, message = "{validation.content.size}")
    private String content;

    private List<Long> tagIds;
}