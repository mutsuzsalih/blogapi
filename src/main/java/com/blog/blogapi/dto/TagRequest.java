package com.blog.blogapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagRequest {

    @NotBlank(message = "{validation.tagname.notblank}")
    @Size(max = 50, message = "{validation.tagname.size}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}