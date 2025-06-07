package com.blog.blogapi.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.UserRepository;

@Service
public class AuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public AuthorizationService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public void checkPostOwnerOrAdmin(Long postId) {
        logger.info("Checking ownership or admin status for Post ID: {}", postId);

        User currentUser = getCurrentUserOrThrow();
        logger.debug("Current User ID: {}, Username: {}", currentUser.getId(), currentUser.getUsername());

        Post post = getPostOrThrow(postId);
        logger.debug("Post found with ID: {}. Title: {}", post.getId(), post.getTitle());

        if (!isAuthorized(currentUser, post)) {
            logger.error("Access DENIED for user {} for post {}.", currentUser.getUsername(), postId);
            throw new AccessDeniedException("You do not have permission to perform this action on the post.");
        }
    }

    public void checkAdmin() {
        logger.info("Checking admin status.");
        User currentUser = getCurrentUserOrThrow();
        if (!isAdmin(currentUser)) {
            logger.error("Access DENIED for user {} (not admin).", currentUser.getUsername());
            throw new AccessDeniedException("You must be an admin to perform this action.");
        }
        logger.info("Admin access GRANTED for user {}.", currentUser.getUsername());
    }

    User getCurrentUserOrThrow() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .flatMap(userRepository::findByUsername)
                .orElseThrow(() -> new AccessDeniedException("User not authenticated to perform this action."));
    }

    Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    boolean isAuthorized(User currentUser, Post post) {
        if (currentUser.isAdmin()) {
            logger.info("Authorization check: User {} is admin on post {}.",
                    currentUser.getUsername(), post.getId());
            return true;
        }

        if (!post.hasAuthor()) {
            logger.warn("Post author is NULL for Post ID: {}.", post.getId());
            return false;
        }

        if (post.isAuthoredBy(currentUser)) {
            logger.info("Authorization check: User {} is author of post {}.",
                    currentUser.getUsername(), post.getId());
            return true;
        }

        logger.error(
                "Authorization check: User {} is not admin and not author of post {}. CurrentUserID: {}",
                currentUser.getUsername(), post.getId(), currentUser.getId());
        return false;
    }

    boolean isAdmin(User user) {
        if (user == null) {
            logger.debug("isAdmin check: User object is null.");
            return false;
        }
        if (user.getRole() == null) {
            logger.debug("isAdmin check: User's role is null for user: {}", user.getUsername());
            return false;
        }
        boolean isAdmin = user.isAdmin();
        logger.debug("IsAdmin check: User: {}, Role: {}, IsAdmin: {}", user.getUsername(), user.getRoleName(),
                isAdmin);
        return isAdmin;
    }
}