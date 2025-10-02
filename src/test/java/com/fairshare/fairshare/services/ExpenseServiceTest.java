package com.fairshare.fairshare.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fairshare.fairshare.dto.BalanceDTO;
import com.fairshare.fairshare.dto.ExpenseDTO;
import com.fairshare.fairshare.dto.SettlementDTO;
import com.fairshare.fairshare.entity.Expense;
import com.fairshare.fairshare.entity.ExpenseShare;
import com.fairshare.fairshare.entity.Group;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.Settlement;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.ExpenseRepository;
import com.fairshare.fairshare.repository.ExpenseShareRepository;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.SettlementRepository;
import com.fairshare.fairshare.repository.UserRepository;
import com.fairshare.fairshare.service.ExpenseService;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    ExpenseRepository expenseRepository;
    @Mock
    ExpenseShareRepository expenseShareRepository;
    @Mock
    MembershipRepository membershipRepository;
    @Mock
    GroupRepository groupRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SettlementRepository settlementRepository;

    @InjectMocks
    ExpenseService expenseService;

    /* ==== helper functions ==== */
    // prevents redundantly building users,groups & memberships in the tests
    private static User user(long id, String name, String email) {
        User u = new User();
        u.setUserId(id);
        u.setUserName(name);
        u.setUserEmail(email);
        return u;
    }

    private static Group group(long id, String name) {
        Group g = new Group();
        g.setGroupId(id);
        g.setGroupName(name);
        return g;
    }

    private static Membership membership(User u, Group g, Membership.Role role) {
        Membership m = new Membership();
        m.setUser(u);
        m.setGroup(g);
        m.setRole(role);
        return m;
    }

    /* ==== tests ==== */
    /**
     * Test creating an expense and the expense is split equally amongst members (we
     * split equally currently)
     * 
     * @throws Exception if error occurs
     */
    @Test
    void createExpense_createsExpense_andSplitsEqually() throws Exception {
        Long groupId = 1L;
        Long payerId = 10L;
        var g = group(groupId, "Lunch");
        var alice = user(payerId, "Alice", "alice@example.com");
        var bob = user(20L, "Bob", "bob@example.com");

        // validate group and payer exist + payer is a member
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(userRepository.existsById(payerId)).thenReturn(true);
        when(membershipRepository.existsByUser_UserIdAndGroup_GroupId(payerId, groupId)).thenReturn(true);

        // define group membership (alice = admin, bob = member)
        // ensures two members to divide the cost
        when(membershipRepository.findByGroup_GroupId(groupId))
                .thenReturn(List.of(
                        membership(alice, g, Membership.Role.ADMIN),
                        membership(bob, g, Membership.Role.MEMBER)));

        // stub saving an expense. repository should assign an ID and return the saved
        // entity
        Expense saved = new Expense();
        saved.setExpenseId(100L);
        saved.setGroup(g);
        saved.setPayer(alice);
        saved.setAmount(new BigDecimal("30.00"));
        saved.setCurrency(Expense.CurrencyCode.CAD);
        saved.setDescription("Pizza");
        saved.setOccurredAt(Instant.parse("2025-01-01T00:00:00Z"));

        when(groupRepository.getReferenceById(groupId)).thenReturn(g);
        when(userRepository.getReferenceById(payerId)).thenReturn(alice);
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        // for each ExpenseShare saved, just return the same object
        when(expenseShareRepository.save(any(ExpenseShare.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        /* Act: call service method */
        ExpenseDTO dto = expenseService.createExpense(
                groupId,
                payerId,
                new BigDecimal("30.00"),
                Expense.CurrencyCode.CAD,
                "Pizza",
                Instant.parse("2025-01-01T00:00:00Z"));

        /* Assert: verify returned DTO correctness */
        assertEquals(100L, dto.id());
        assertEquals(groupId, dto.groupId());
        assertEquals(payerId, dto.payerId());
        assertEquals(new BigDecimal("30.00"), dto.amount());
        assertEquals(2, dto.shares().size());

        // Verify: split logic, each share is 15.00 & we persisted 2 share rows
        var perShare = new BigDecimal("30.00").divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        assertTrue(dto.shares().stream().allMatch(s -> s.shareAmount().compareTo(perShare) == 0));
        verify(expenseShareRepository, times(2)).save(any(ExpenseShare.class));
    }

    /**
     * Test to retreive user balance and ensure it returns correctly after math
     * 
     * @throws Exception if error occurs
     */
    @Test
    void getUserBalances_computesNetBalancesCorrectly() throws Exception {
        Long groupId = 1L;
        var g = group(groupId, "Trip");
        var alice = user(10L, "Alice", "alice@example.com");
        var bob = user(20L, "Bob", "bob@example.com");

        // group must exist, otherwise NotFoundException should be thrown
        when(groupRepository.existsById(groupId)).thenReturn(true);

        // define group membership (Alice + Bob)
        when(membershipRepository.findByGroup_GroupId(groupId))
                .thenReturn(List.of(
                        membership(alice, g, Membership.Role.ADMIN),
                        membership(bob, g, Membership.Role.MEMBER)));

        // stub one expense (Alice paid $20)
        var expense = new Expense();
        expense.setExpenseId(100L);
        expense.setGroup(g);
        expense.setPayer(alice);
        expense.setAmount(new BigDecimal("20.00"));
        expense.setCurrency(Expense.CurrencyCode.CAD);
        expense.setDescription("Snacks");
        expense.setOccurredAt(Instant.parse("2025-01-01T00:00:00Z"));

        when(expenseRepository.findByGroup_GroupId(groupId)).thenReturn(List.of(expense));

        // stub expense shares ($10 per person)
        var aliceShare = new ExpenseShare();
        aliceShare.setExpense(expense);
        aliceShare.setParticipant(alice);
        aliceShare.setShareAmount(new BigDecimal("10.00"));

        var bobShare = new ExpenseShare();
        bobShare.setExpense(expense);
        bobShare.setParticipant(bob);
        bobShare.setShareAmount(new BigDecimal("10.00"));

        when(expenseShareRepository.findByExpense_Group_GroupId(groupId))
                .thenReturn(List.of(aliceShare, bobShare));

        /* Act: call service */
        List<BalanceDTO> balances = expenseService.getUserBalances(groupId);

        /* Assert: balances must reflect net positions */
        // Alice: paid 20, owes 10 -> net +10
        // Bob: paid 0, owes 10 -> net -10
        assertEquals(2, balances.size(), "Should return one balance per member");

        var aliceBalance = balances.stream().filter(b -> b.userId().equals(10L)).findFirst().orElseThrow();
        var bobBalance = balances.stream().filter(b -> b.userId().equals(20L)).findFirst().orElseThrow();

        assertEquals(new BigDecimal("10.00"), aliceBalance.balance(), "Alice should be owed 10");
        assertEquals(new BigDecimal("-10.00"), bobBalance.balance(), "Bob should owe 10");
    }

    /**
     * Test creating a settlement between two parties
     * 
     * @throws Exception if error occurs
     */
    @Test
    void createSettlement_persistsSettlement_andReturnsDTO() throws Exception {
        Long groupId = 1L;
        // alice pays, bob receives
        Long payerId = 10L;
        Long payeeId = 20L;
        BigDecimal amount = new BigDecimal("25.00");

        var g = group(groupId, "Club");
        var alice = user(payerId, "Alice", "alice@example.com");
        var bob = user(payeeId, "Bob", "bob@example.com");

        // group must exist; else should throw NotFoundException
        when(groupRepository.existsById(groupId)).thenReturn(true);

        // both users must exist; else NotFoundException
        when(userRepository.existsById(payerId)).thenReturn(true);
        when(userRepository.existsById(payeeId)).thenReturn(true);

        // both users must be members of the group; else AccessDeniedException
        when(membershipRepository.existsByUser_UserIdAndGroup_GroupId(payerId, groupId)).thenReturn(true);
        when(membershipRepository.existsByUser_UserIdAndGroup_GroupId(payeeId, groupId)).thenReturn(true);

        // getReferenceById stubs for associations
        when(groupRepository.getReferenceById(groupId)).thenReturn(g);
        when(userRepository.getReferenceById(payerId)).thenReturn(alice);
        when(userRepository.getReferenceById(payeeId)).thenReturn(bob);

        // saving the settlement returns a managed entity with IDs/timestamps
        Settlement saved = new Settlement();
        saved.setSettlementId(500L);
        saved.setPayer(alice);
        saved.setPayee(bob);
        saved.setGroup(g);
        saved.setAmount(amount);
        saved.setCurrency(Settlement.CurrencyCode.CAD);
        saved.setSettledAt(Instant.parse("2025-03-03T00:00:00Z"));

        when(settlementRepository.save(any(Settlement.class))).thenReturn(saved);

        /* Act: call service method under test */
        SettlementDTO dto = expenseService.createSettlement(
                groupId,
                payerId,
                payeeId,
                amount,
                Settlement.CurrencyCode.CAD);

        /* Assert: returned DTO reflects persisted state */
        assertEquals(500L, dto.id(), "DTO should expose the generated settlement ID");
        assertEquals(payerId, dto.payerId(), "DTO must reflect the payer user ID");
        assertEquals(payeeId, dto.payeeId(), "DTO must reflect the payee user ID");
        assertEquals(groupId, dto.groupId(), "DTO must reflect the group ID");
        assertEquals(new BigDecimal("25.00"), dto.amount(), "DTO amount must match persisted amount");
        assertEquals(Settlement.CurrencyCode.CAD, dto.currency(), "DTO currency must match persisted currency");
        assertEquals(Instant.parse("2025-03-03T00:00:00Z"), dto.settledAt(),
                "DTO settledAt must match persisted timestamp");

        /* Verify: a single settlement row was persisted */
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }
}
