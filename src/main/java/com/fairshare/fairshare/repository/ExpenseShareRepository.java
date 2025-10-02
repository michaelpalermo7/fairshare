package com.fairshare.fairshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.ExpenseShare;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    List<ExpenseShare> findByExpense_ExpenseId(Long expenseId);
    List<ExpenseShare> findByExpense_Group_GroupId(Long groupId);
}

