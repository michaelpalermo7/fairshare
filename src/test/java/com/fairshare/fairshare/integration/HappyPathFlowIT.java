package com.fairshare.fairshare.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fairshare.fairshare.dto.BalanceDTO;
import com.fairshare.fairshare.dto.ExpenseDTO;
import com.fairshare.fairshare.dto.GroupDTO;
import com.fairshare.fairshare.dto.MembershipDTO;
import com.fairshare.fairshare.entity.Expense;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.ExpenseRepository;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.UserRepository;
import com.fairshare.fairshare.service.ExpenseService;
import com.fairshare.fairshare.service.GroupService;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext
class HappyPathFlowIT {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    MembershipRepository membershipRepository;
    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    GroupService groupService;
    @Autowired
    ExpenseService expenseService;

    @Test
    void fullFlow_createUsers_addToGroup_adminEnforced_createExpense_balances_delete() throws Exception {
        // create two users (separate build vs saved instances to avoid reassignment)
        var aliceEntity = new User();
        aliceEntity.setUserName("Alice");
        aliceEntity.setUserEmail("alice@example.com");
        final var alice = userRepository.save(aliceEntity);

        var bobEntity = new User();
        bobEntity.setUserName("Bob");
        bobEntity.setUserEmail("bob@example.com");
        final var bob = userRepository.save(bobEntity);

        // create group via service (creator becomes ADMIN)
        final GroupDTO groupDto = groupService.createGroup("Trip", alice.getUserId());
        final Long groupId = groupDto.id();

        // verify creator is ADMIN
        final List<MembershipDTO> membersAfterCreate = groupService.listAllMembers(groupId);
        assertThat(membersAfterCreate).hasSize(1);
        assertThat(membersAfterCreate.get(0).userId()).isEqualTo(alice.getUserId());
        assertThat(membersAfterCreate.get(0).role()).isEqualTo(Membership.Role.ADMIN);

        // add Bob as MEMBER
        final MembershipDTO bobMember = groupService.addMember(groupId, bob.getUserId(), Membership.Role.MEMBER);
        assertThat(bobMember.userId()).isEqualTo(bob.getUserId());
        assertThat(bobMember.role()).isEqualTo(Membership.Role.MEMBER);

        // sanity: group now has 2 members
        final List<MembershipDTO> allMembers = groupService.listAllMembers(groupId);
        assertThat(allMembers).extracting(MembershipDTO::userId)
                .containsExactlyInAnyOrder(alice.getUserId(), bob.getUserId());

        // create an expense (Alice pays $40, equal split)
        final ExpenseDTO expense = expenseService.createExpense(
                groupId,
                alice.getUserId(),
                new BigDecimal("40.00"),
                Expense.CurrencyCode.CAD,
                "Dinner",
                Instant.parse("2025-01-01T00:00:00Z"));
        assertThat(expense.id()).isNotNull();
        assertThat(expense.shares()).hasSize(2);
        assertThat(expense.description()).isEqualTo("Dinner");

        // verify balances (+20 Alice, -20 Bob)
        final List<BalanceDTO> balances = expenseService.getUserBalances(groupId);
        final Map<Long, BigDecimal> balMap = balances.stream()
                .collect(Collectors.toMap(BalanceDTO::userId, BalanceDTO::balance));
        assertThat(balMap.get(alice.getUserId())).isEqualByComparingTo("20.00");
        assertThat(balMap.get(bob.getUserId())).isEqualByComparingTo("-20.00");

        // non-admin cannot delete the group
        assertThatThrownBy(() -> groupService.deleteGroup(groupId, bob.getUserId()))
                .isInstanceOf(java.nio.file.AccessDeniedException.class);

        // admin can delete the group
        groupService.deleteGroup(groupId, alice.getUserId());

        // verify cascade cleanup for that group (no expenses/memberships remain for it)
        assertThat(groupRepository.findById(groupId)).isEmpty();
        assertThat(membershipRepository.findByGroup_GroupId(groupId)).isEmpty();
        assertThat(expenseRepository.findByGroup_GroupId(groupId)).isEmpty();
    }

}
