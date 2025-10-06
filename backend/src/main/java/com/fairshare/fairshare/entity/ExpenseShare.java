package com.fairshare.fairshare.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="expense_share", uniqueConstraints = @UniqueConstraint(name = "uq_expense_share", columnNames = {"expense_id", "participant_id"}))
public class ExpenseShare {

    /* ==== attributes ==== */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="expense_share_id")
    private Long expenseShareId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Share amount must be >= 0")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "share_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal shareAmount;

    @Digits(integer = 2, fraction = 4) 
    @Column(name = "share_ratio", precision = 6, scale = 4)
    private BigDecimal shareRatio;

    /* ==== relationships ==== */
    //many to one - expense shares to user
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false/*, foreignKey = @ForeignKey(name = "fk_expense_share_participant")*/)
    private User participant;

    //many to one - expense shares to expense
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false/*, foreignKey = @ForeignKey(name = "fk_expense_share_expense")*/)
    private Expense expense;


    /* ==== getters and setters ==== */
    public Long getExpenseShareId() { return expenseShareId; }
    public void setExpenseShareId(Long expenseShareId) { this.expenseShareId = expenseShareId; }

    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }

    public BigDecimal getShareRatio() { return shareRatio; }
    public void setShareRatio(BigDecimal shareRatio) { this.shareRatio = shareRatio; }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }

    public User getParticipant() { return participant; }
    public void setParticipant(User participant) { this.participant = participant; }
}
