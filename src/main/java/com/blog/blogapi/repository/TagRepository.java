package com.blog.blogapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.blogapi.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
}