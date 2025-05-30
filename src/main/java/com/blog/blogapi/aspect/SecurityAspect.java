package com.blog.blogapi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.UserRepository;

@Aspect
@Component
public class SecurityAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAspect.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SecurityAspect(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Pointcut("@annotation(com.blog.blogapi.aspect.RequiresPostOwnerOrAdmin) && args(postId, ..)")
    public void requiresPostOwnerOrAdminPointcut(Long postId) {
    }

    @Around("requiresPostOwnerOrAdminPointcut(postId)")
    public Object checkPostOwnerOrAdmin(ProceedingJoinPoint joinPoint, Long postId) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Checking @RequiresPostOwnerOrAdmin for method [{}] with Post ID: {}", methodName, postId);

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("Current user is null. Denying access for method [{}] on Post ID: {}.", methodName, postId);
            throw new AccessDeniedException("User not authenticated to perform this action.");
        }
        logger.debug("Current User ID: {}, Username: {}", currentUser.getId(), currentUser.getUsername());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.warn("Post not found with id: {} (for method [{}]).", postId, methodName);

                    return new RuntimeException("Post not found with id: " + postId);
                });
        logger.debug("Post found with ID: {}. Title: {}", post.getId(), post.getTitle());

        User postAuthor = post.getAuthor();
        boolean isCurrentUserAdmin = isAdmin(currentUser);
        logger.debug("Is current user admin? {}", isCurrentUserAdmin);

        if (postAuthor == null) {
            logger.warn("Post author is NULL for Post ID: {}.", postId);
            if (!isCurrentUserAdmin) {
                logger.error(
                        "Access DENIED for user {} (not admin and post author is null) to method [{}] for post {}.",
                        currentUser.getUsername(), methodName, postId);
                throw new AccessDeniedException(
                        "You do not have permission to perform this action on the post as author is not set and you are not an admin.");
            }
        } else {
            logger.debug("Post Author ID: {}, Username: {}", postAuthor.getId(), postAuthor.getUsername());
            if (!isCurrentUserAdmin && !postAuthor.getId().equals(currentUser.getId())) {
                logger.error(
                        "Access DENIED for user {} (not admin and not author) to method [{}] for post {}. CurrentUserID: {}, PostAuthorID: {}",
                        currentUser.getUsername(), methodName, postId, currentUser.getId(), postAuthor.getId());
                throw new AccessDeniedException("You do not have permission to perform this action on the post.");
            }
        }

        logger.info("Access GRANTED for user {} to method [{}] for post {}.", currentUser.getUsername(), methodName,
                postId);
        return joinPoint.proceed();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Current user not authenticated or anonymous.");
            return null;
        }
        String username = authentication.getName();
        logger.debug("Authenticated username: {}", username);
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    logger.warn("Authenticated user '{}' not found in database.", username);
                    return null;
                });
    }

    private boolean isAdmin(User user) {
        if (user == null) {
            logger.debug("isAdmin check: User object is null.");
            return false;
        }
        if (user.getRole() == null) {
            logger.debug("isAdmin check: User's role is null for user: {}", user.getUsername());
            return false;
        }
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        logger.debug("isAdmin check: User: {}, Role: {}, IsAdmin: {}", user.getUsername(), user.getRole().name(),
                isAdmin);
        return isAdmin;
    }
}