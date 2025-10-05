package com.fairshare.fairshare.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = { "user_email" })
})
public class User {

    /* ==== attributes ==== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @NotBlank
    @Email
    @Column(name = "user_email", unique = true, nullable = false, length = 255)
    private String userEmail;

    @Column(name = "user_created_at", nullable = false, columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant userCreatedAt;

    @Column(name = "deleted_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant deletedAt;

    /* ==== relationships ==== */

    // one to many - user to memberships
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Membership> userMemberships = new HashSet<>();

    // one to many - user to expenses
    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    private final Set<Expense> userExpenses = new HashSet<>();

    // one to many - user to expense shares
    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY)
    private final Set<ExpenseShare> userExpenseShares = new HashSet<>();

    // one to many - user to settlements
    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY)
    private final Set<Settlement> payerSettlements = new HashSet<>();

    // one to many - user to settlements
    @OneToMany(mappedBy = "payee", fetch = FetchType.LAZY)
    private final Set<Settlement> payeeSettlements = new HashSet<>();

    /* ==== getters anb setters ==== */

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Instant getUserCreatedAt() {
        return userCreatedAt;
    }

    public void setUserCreatedAt(Instant userCreatedAt) {
        this.userCreatedAt = userCreatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Set<Membership> getUserMemberships() {
        return userMemberships;
    }

    public Set<Expense> getUserExpenses() {
        return userExpenses;
    }

    public Set<ExpenseShare> getUserExpenseShares() {
        return userExpenseShares;
    }

    public Set<Settlement> getPayerSettlements() {
        return payerSettlements;
    }

    public Set<Settlement> getPayeeSettlements() {
        return payeeSettlements;
    }

}
