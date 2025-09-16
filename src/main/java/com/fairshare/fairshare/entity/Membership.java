package com.fairshare.fairshare.entity;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="memberships",uniqueConstraints = @UniqueConstraint(name = "uq_membership_user_group", columnNames = {"user_id", "group_id"}))
public class Membership {

    /* ==== attributes ==== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="membership_id")
    private Long membershipId;

    @NotNull
    @Column(name="joined_at", nullable = false, columnDefinition="timestamptz", insertable = false, updatable = false)
    private Instant joinedAt;

    public enum Role {
        ADMIN,
        MEMBER
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.MEMBER;

    /* ==== relationships ==== */
    //many to one - memberships to user - fk userId
    @NotNull
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    //many to one - memberships to group - fk groupId
    @NotNull
    @ManyToOne
    @JoinColumn(name="group_id", nullable = false)
    private Group group;

    /* ==== getters and setters ==== */
    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

}
