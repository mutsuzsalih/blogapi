package com.blog.blogapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.blogapi.model.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
}