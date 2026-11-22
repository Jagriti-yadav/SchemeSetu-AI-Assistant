
package com.mahi.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mahi.demo.entity.GovernmentScheme;

@Repository
public interface GovernmentSchemeRepository
        extends JpaRepository<GovernmentScheme, Long> {

    List<GovernmentScheme> findBySchemeNameContainingIgnoreCase(String keyword);

    List<GovernmentScheme> findByDescriptionContainingIgnoreCase(String keyword);

    List<GovernmentScheme> findByEligibilityContainingIgnoreCase(String keyword);
}
