package com.fairshare.fairshare.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name="settlements")
public class Settlement {

    /* ==== attributes ==== */
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="settlement_id")
    private Long settlementId;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2) 
    private BigDecimal amount;

    public enum CurrencyCode { CAD, USD, EUR }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency = CurrencyCode.CAD;

    @NotNull
    @Column(name="settled_at",nullable = false, columnDefinition="timestamptz", insertable = false, updatable = false)
    private Instant settledAt;

    /* ==== relationships ==== */
    //many to one users
    @ManyToOne
    @JoinColumn(name="payer_id", nullable=false)
    private User payer;

    //many to one users
    @ManyToOne
    @JoinColumn(name="payee_id", nullable=false)
    private User payee;

    //many to one groups
    @ManyToOne
    @JoinColumn(name="group_id", nullable=false)
    private Group group;

    /* ==== getters and setters ==== */

    public Long getSettlementId() {
        return settlementId;
    }
    public void setSettlementId (Long settlementId) {
        this.settlementId=settlementId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public User getPayee() {
        return payee;
    }

    public void setPayee(User payee) {
        this.payee = payee;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }


}
