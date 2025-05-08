package com.blog.blogapi.repository;

import com.blog.blogapi.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}