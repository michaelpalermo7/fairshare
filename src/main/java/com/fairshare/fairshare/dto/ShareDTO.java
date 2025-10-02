package com.fairshare.fairshare.dto;

import java.math.BigDecimal;

public record ShareDTO(
    Long participantId,
    BigDecimal shareAmount,
    BigDecimal shareRatio
) {}