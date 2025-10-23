package com.project.safetyFence.repository;

import com.project.safetyFence.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> {
}
