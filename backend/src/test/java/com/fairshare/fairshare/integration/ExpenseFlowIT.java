package com.fairshare.fairshare.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fairshare.fairshare.dto.BalanceDTO;
import com.fairshare.fairshare.dto.ExpenseDTO;
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
class ExpenseFlowIT {

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

    /**
     * integration test for creating expense
     * 
     * @throws Exception
     */
    @Test
    void createExpense_andComputeBalances_endToEnd() throws Exception {
        // seed users
        var alice = new User();
        alice.setUserName("Alice");
        alice.setUserEmail("alice@example.com");
        var bob = new User();
        bob.setUserName("Bob");
        bob.setUserEmail("bob@example.com");
        alice = userRepository.save(alice);
        bob = userRepository.save(bob);

        // create group
        var g = new Group();
        g.setGroupName("Trip");
        g = groupRepository.save(g);

        // add memberships (Alice admin, Bob member)
        var m1 = new Membership();
        m1.setUser(alice);
        m1.setGroup(g);
        m1.setRole(Membership.Role.ADMIN);
        var m2 = new Membership();
        m2.setUser(bob);
        m2.setGroup(g);
        m2.setRole(Membership.Role.MEMBER);
        membershipRepository.saveAll(List.of(m1, m2));

        // create an expense via service (Alice pays 40)
        ExpenseDTO expense = expenseService.createExpense(
                g.getGroupId(),
                alice.getUserId(),
                new BigDecimal("40.00"),
                Expense.CurrencyCode.CAD,
                "Dinner",
                Instant.now());

        assertThat(expense.id()).isNotNull();
        assertThat(expense.shares()).hasSize(2);

        // compute balances via service
        List<BalanceDTO> balances = expenseService.getUserBalances(g.getGroupId());
        Map<Long, BigDecimal> map = balances.stream()
                .collect(java.util.stream.Collectors.toMap(BalanceDTO::userId, BalanceDTO::balance));

        // Expected: +20 for Alice (owed), -20 for Bob (owes)
        assertThat(map.get(alice.getUserId())).isEqualByComparingTo("20.00");
        assertThat(map.get(bob.getUserId())).isEqualByComparingTo("-20.00");
    }
}