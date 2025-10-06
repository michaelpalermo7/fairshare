package com.fairshare.fairshare.dto;

import java.math.BigDecimal;

import com.fairshare.fairshare.entity.Settlement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSettlementRequest(
        @NotNull Long payerId,
        @NotNull Long payeeId,
        @NotNull @Positive BigDecimal amount,
        Settlement.CurrencyCode currency // optional; defaults in service
) {
}
