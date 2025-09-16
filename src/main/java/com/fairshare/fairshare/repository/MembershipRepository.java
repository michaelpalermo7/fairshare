package com.fairshare.fairshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
}

