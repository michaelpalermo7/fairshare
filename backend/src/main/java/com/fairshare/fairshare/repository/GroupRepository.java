package com.fairshare.fairshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
