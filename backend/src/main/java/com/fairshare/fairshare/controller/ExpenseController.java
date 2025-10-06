package com.fairshare.fairshare.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fairshare.fairshare.dto.BalanceDTO;
import com.fairshare.fairshare.dto.CreateExpenseRequest;
import com.fairshare.fairshare.dto.CreateSettlementRequest;
import com.fairshare.fairshare.dto.ExpenseDTO;
import com.fairshare.fairshare.dto.SettlementDTO;
import com.fairshare.fairshare.service.ExpenseService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/groups/{groupId}")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ExpenseDTO createExpense(
            @PathVariable Long groupId,
            @RequestBody @Valid CreateExpenseRequest request) throws Exception {
        // TODO: input normalization (trim description, etc.)
        return expenseService.createExpense(
                groupId,
                request.payerId(),
                request.amount(),
                request.currency(),
                request.description(),
                request.occurredAt());
    }

    @GetMapping("/expenses")
    @Transactional(readOnly = true)
    public List<ExpenseDTO> listGroupExpenses(@PathVariable Long groupId) throws Exception {
        // TODO: pagination/filtering in future
        return expenseService.listGroupExpenses(groupId);
    }

    @GetMapping("/balances")
    @Transactional(readOnly = true)
    public List<BalanceDTO> getUserBalances(@PathVariable Long groupId) throws Exception {
        return expenseService.getUserBalances(groupId);
    }

    @PostMapping("/settlements")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public SettlementDTO createSettlement(
            @PathVariable Long groupId,
            @RequestBody @Valid CreateSettlementRequest request) throws Exception {
        return expenseService.createSettlement(
                groupId,
                request.payerId(),
                request.payeeId(),
                request.amount(),
                request.currency());
    }

}
