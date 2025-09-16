package com.fairshare.fairshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.ExpenseShare;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
}

