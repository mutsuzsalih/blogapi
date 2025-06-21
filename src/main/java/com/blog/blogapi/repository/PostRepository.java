package com.blog.blogapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.blogapi.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor_Id(Long authorId);
}