package com.blog.blogapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blog.blogapi.model.LocalizationMessage;

@Repository
public interface LocalizationMessageRepository extends JpaRepository<LocalizationMessage, Long> {

    Optional<LocalizationMessage> findByKeyAndLocale(String key, String locale);

    List<LocalizationMessage> findByLocale(String locale);

    @Query("SELECT lm FROM LocalizationMessage lm WHERE lm.key = :key AND lm.locale IN (:locales) ORDER BY lm.locale")
    List<LocalizationMessage> findByKeyAndLocales(@Param("key") String key, @Param("locales") List<String> locales);

    boolean existsByKeyAndLocale(String key, String locale);
}