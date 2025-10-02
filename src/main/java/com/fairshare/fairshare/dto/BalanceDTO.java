package com.fairshare.fairshare.dto;

import java.math.BigDecimal;

public record BalanceDTO(
    Long userId,
    BigDecimal balance
) {}
