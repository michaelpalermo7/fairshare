package com.fairshare.fairshare.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fairshare.fairshare.entity.Expense;

public record ExpenseDTO (
    Long id,
    Long groupId,
    Long payerId,
    BigDecimal amount,
    Expense.CurrencyCode currency,
    String description,
    Instant occurredAt,
    Instant createdAt,
    //contains a breakdown of shares per participant of the expense
    List<ShareDTO> shares
    ) {}
    

