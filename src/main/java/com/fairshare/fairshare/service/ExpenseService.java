package com.fairshare.fairshare.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairshare.fairshare.dto.BalanceDTO;
import com.fairshare.fairshare.dto.ExpenseDTO;
import com.fairshare.fairshare.dto.SettlementDTO;
import com.fairshare.fairshare.dto.ShareDTO;
import com.fairshare.fairshare.entity.Expense;
import com.fairshare.fairshare.entity.ExpenseShare;
import com.fairshare.fairshare.entity.Membership;
import com.fairshare.fairshare.entity.Settlement;
import com.fairshare.fairshare.repository.ExpenseRepository;
import com.fairshare.fairshare.repository.ExpenseShareRepository;
import com.fairshare.fairshare.repository.GroupRepository;
import com.fairshare.fairshare.repository.MembershipRepository;
import com.fairshare.fairshare.repository.SettlementRepository;
import com.fairshare.fairshare.repository.UserRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final SettlementRepository settlementRepository;

    public ExpenseService(ExpenseRepository expenseRepository, GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            UserRepository userRepository, ExpenseShareRepository expenseShareRepository,
            SettlementRepository settlementRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.settlementRepository = settlementRepository;
    }

    /**
     * Create an expense and split shares equally amongst members
     * 
     * @param groupId   group that expense will reside
     * @param payerId   person who paid the expense
     * @param amount    amount of expense
     * @param currency  currency of expense
     * @param desc      text
     * @param occuredAt when
     * @return expense dto
     * @throws NotFoundException
     * @throws AccessDeniedException
     */
    @Transactional
    public ExpenseDTO createExpense(Long groupId, Long payerId, BigDecimal amount, Expense.CurrencyCode currency,
            String desc, Instant occuredAt) throws NotFoundException, AccessDeniedException {

        if (!(groupRepository.existsById(groupId)) || !(userRepository.existsById(payerId))) {
            throw new NotFoundException();
        }
        // verify user is member of group
        if (!membershipRepository.existsByUser_UserIdAndGroup_GroupId(payerId, groupId)) {
            throw new AccessDeniedException("Payer is not a member of this group");
        }

        // verify amount is above 0
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0");
        }

        // normalize amount to 2 decimal places 10.235 -> 10.24
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        // save expense
        Expense e = new Expense();
        e.setGroup(groupRepository.getReferenceById(groupId));
        e.setPayer(userRepository.getReferenceById(payerId));
        e.setAmount(normalizedAmount);
        // if currency is null default to cad
        e.setCurrency(currency != null ? currency : Expense.CurrencyCode.CAD);
        e.setDescription(desc);
        // if occuredat is null default to now
        e.setOccurredAt(occuredAt != null ? occuredAt : Instant.now());

        Expense saved = expenseRepository.save(e);

        var members = membershipRepository.findByGroup_GroupId(groupId);
        if (members.isEmpty()) {
            throw new IllegalStateException("Cannot split expense: no members in group");
        }

        // equal split across all members
        int memberCount = members.size();
        BigDecimal perMemberShare = normalizedAmount
                .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);

        List<ShareDTO> shares = new ArrayList<>(memberCount);
        for (var m : members) {
            ExpenseShare es = new ExpenseShare();
            es.setExpense(saved);
            es.setParticipant(m.getUser());
            es.setShareAmount(perMemberShare);
            es.setShareRatio(null);
            expenseShareRepository.save(es);

            shares.add(new ShareDTO(m.getUser().getUserId(), perMemberShare, null));
        }

        return new ExpenseDTO(
                saved.getExpenseId(),
                groupId,
                payerId,
                saved.getAmount(),
                saved.getCurrency(),
                saved.getDescription(),
                saved.getOccurredAt(),
                saved.getCreatedAt(),
                // set shares
                shares);
    }

    /**
     * List all expenses within a group
     * 
     * @param groupId where expenses are
     * @return expense dto
     * @throws NotFoundException
     */
    @Transactional(readOnly = true)
    public List<ExpenseDTO> listGroupExpenses(Long groupId) throws NotFoundException {

        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException();
        }

        // fetch all expenses for this group
        var expenses = expenseRepository.findByGroup_GroupId(groupId);

        return expenses.stream().map(expense -> {
            // fetch all shares for this expense
            var shares = expenseShareRepository.findByExpense_ExpenseId(expense.getExpenseId())
                    .stream()
                    .map(s -> new ShareDTO(
                            s.getParticipant().getUserId(),
                            s.getShareAmount(),
                            s.getShareRatio()))
                    .toList();

            return new ExpenseDTO(
                    expense.getExpenseId(),
                    expense.getGroup().getGroupId(),
                    expense.getPayer().getUserId(),
                    expense.getAmount(),
                    expense.getCurrency(),
                    expense.getDescription(),
                    expense.getOccurredAt(),
                    expense.getCreatedAt(),
                    shares);
        }).toList();
    }

    /**
     * retrieve balance of all users within a group
     * 
     * @param groupId where users with balances are found
     * @return balance list (userid, balance)
     * @throws NotFoundException
     */
    @Transactional(readOnly = true)
    public List<BalanceDTO> getUserBalances(Long groupId) throws NotFoundException {

        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException();
        }

        // get members
        List<Membership> members = membershipRepository.findByGroup_GroupId(groupId);

        Map<Long, BigDecimal> balances = new HashMap<>();

        // initialize hashmap with 0.00 balance for each member
        for (Membership member : members) {
            balances.put(member.getUser().getUserId(), BigDecimal.ZERO);
        }

        // add to balance what each payer paid
        List<Expense> expenses = expenseRepository.findByGroup_GroupId(groupId);
        for (Expense exp : expenses) {
            Long payerId = exp.getPayer().getUserId();
            BigDecimal current = balances.getOrDefault(payerId, BigDecimal.ZERO);
            balances.put(payerId, current.add(exp.getAmount()));
        }

        // subtract from balance what each participant owes
        List<ExpenseShare> shares = expenseShareRepository.findByExpense_Group_GroupId(groupId);
        for (ExpenseShare share : shares) {
            Long participantId = share.getParticipant().getUserId();
            BigDecimal current = balances.getOrDefault(participantId, BigDecimal.ZERO);
            balances.put(participantId, current.subtract(share.getShareAmount()));
        }

        // apply settlements (payer pays out, payee receives)
        List<Settlement> settlements = settlementRepository.findByGroup_GroupId(groupId);
        for (Settlement s : settlements) {
            Long payerId = s.getPayer().getUserId();
            Long payeeId = s.getPayee().getUserId();
            BigDecimal amt = s.getAmount();
            balances.put(payerId, balances.get(payerId).subtract(amt));
            balances.put(payeeId, balances.get(payeeId).add(amt));
        }

        // normalize balances
        BigDecimal total = balances.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) != 0) {
            Long firstUserId = balances.keySet().iterator().next();
            balances.put(firstUserId, balances.get(firstUserId).subtract(total));
        }

        // round balances to 2 decimals for clean output
        return balances.entrySet().stream()
                .map(e -> new BalanceDTO(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    /**
     * Create a settlement
     * 
     * @param groupId  where settlement occurs
     * @param payerId  payer of settlement
     * @param payeeId  receiver of settlement
     * @param amount   amount of settlement
     * @param currency currency it was made in
     * @return
     * @throws NotFoundException
     * @throws AccessDeniedException
     */
    @Transactional
    public SettlementDTO createSettlement(
            Long groupId,
            Long payerId,
            Long payeeId,
            BigDecimal amount,
            Settlement.CurrencyCode currency) throws NotFoundException, AccessDeniedException {

        // validate group & users & membership
        if (!groupRepository.existsById(groupId))
            throw new NotFoundException();

        if (!userRepository.existsById(payerId) || !userRepository.existsById(payeeId))
            throw new NotFoundException();

        if (!membershipRepository.existsByUser_UserIdAndGroup_GroupId(payerId, groupId) ||
                !membershipRepository.existsByUser_UserIdAndGroup_GroupId(payeeId, groupId)) {
            throw new AccessDeniedException("Both users must be members of the group");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot settle 0 or negative");
        }

        // create settlement
        Settlement s = new Settlement();
        s.setPayer(userRepository.getReferenceById(payerId));
        s.setPayee(userRepository.getReferenceById(payeeId));
        s.setGroup(groupRepository.getReferenceById(groupId));
        s.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        s.setCurrency(currency != null ? currency : Settlement.CurrencyCode.CAD);
        s.setSettledAt(Instant.now());

        Settlement saved = settlementRepository.save(s);

        // return dto
        return new SettlementDTO(
                saved.getSettlementId(),
                payerId,
                payeeId,
                groupId,
                saved.getAmount(),
                saved.getCurrency(),
                saved.getSettledAt());
    }

}
