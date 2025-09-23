package com.fairshare.fairshare.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="groups")
public class Group {

    /* ==== attributes ==== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id")
    private Long groupId;

    @Column(name="group_name", nullable = false, length = 255)
    private String groupName;

    @Column(name="group_created_at", nullable = false, columnDefinition="timestamptz", insertable = false, updatable = false)
    private Instant groupCreatedAt;

    /* ==== relationships ==== */
    
    //one to many - group to memberships
    @OneToMany(mappedBy="group", fetch=FetchType.LAZY)
    private final Set<Membership> groupMemberships = new HashSet<>();

    //one to many - group to expenses
    @OneToMany(mappedBy="group", fetch=FetchType.LAZY)
    private final Set<Expense> groupExpenses = new HashSet<>();

    //one to many - group to settlements
    @OneToMany(mappedBy="group", fetch=FetchType.LAZY)
    private final Set<Settlement> groupSettlements = new HashSet<>();

    /* ==== getters and setters ==== */

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Instant getGroupCreatedAt() { return groupCreatedAt; }
    public void setGroupCreatedAt(Instant groupCreatedAt){
        this.groupCreatedAt = groupCreatedAt;
    }

    public Set<Membership> getGroupMemberships() {
        return groupMemberships;
    }

    public Set<Expense> getGroupExpenses() {
        return groupExpenses;
    }

    public Set<Settlement> getGroupSettlements() {
        return groupSettlements;
    }
    
}
