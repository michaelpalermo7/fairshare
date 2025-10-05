package com.fairshare.fairshare.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fairshare.fairshare.entity.Expense;
import com.fairshare.fairshare.entity.Group;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.UserRepository;
import com.fairshare.fairshare.service.ExpenseService;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class NonMemberCannotCreateExpenseIT {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    MembershipRepository membershipRepository;
    @Autowired
    ExpenseService expenseService;

    @Test
    void nonMember_cannotCreateExpense_inGroup() throws Exception {
        // seed two users
        var member = new User();
        member.setUserName("Member");
        member.setUserEmail("member@example.com");
        member = userRepository.save(member);

        var outsider = new User();
        outsider.setUserName("Outsider");
        outsider.setUserEmail("outsider@example.com");
        outsider = userRepository.save(outsider);

        // create group
        var g = new Group();
        g.setGroupName("MVP Group");
        g = groupRepository.save(g);

        // add only 'member' to the group
        var m = new Membership();
        m.setUser(member);
        m.setGroup(g);
        m.setRole(Membership.Role.MEMBER);
        membershipRepository.saveAll(List.of(m));

        // attempt: outsider tries to create an expense in the group → should be denied
        Long groupId = g.getGroupId();
        Long outsiderId = outsider.getUserId();

        assertThatThrownBy(() -> expenseService.createExpense(
                groupId,
                outsiderId,
                new BigDecimal("25.00"),
                Expense.CurrencyCode.CAD,
                "Snacks",
                Instant.parse("2025-01-01T00:00:00Z"))).isInstanceOf(java.nio.file.AccessDeniedException.class)
                .hasMessageContaining("member of this group");
    }
}
