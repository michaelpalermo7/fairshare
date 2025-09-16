package com.fairshare.fairshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.Settlement;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
