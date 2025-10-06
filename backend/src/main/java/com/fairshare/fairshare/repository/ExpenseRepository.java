package com.fairshare.fairshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup_GroupId(Long groupId);
}
