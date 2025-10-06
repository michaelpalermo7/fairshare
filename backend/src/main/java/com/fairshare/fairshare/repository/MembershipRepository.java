package com.fairshare.fairshare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.Membership.Role;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUser_UserIdAndGroup_GroupId(Long userId, Long groupId);

    long countByGroup_GroupIdAndRole(Long groupId, Role admin);
    List<Membership> findByGroup_GroupId(Long groupId);

    boolean existsByUser_UserIdAndGroup_GroupId(Long payerId, Long groupId);

}

