package com.wealthwise.services;

import com.wealthwise.dao.TransactionDao;
import com.wealthwise.models.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class TransactionService {

    private final TransactionDao dao = new TransactionDao();

    // ── CRUD ──────────────────────────────────────────────────────────────────
    public void addTransaction(Transaction t)    { dao.add(t); }
    public void updateTransaction(Transaction t) { dao.update(t); }
    public void deleteTransaction(int id)        { dao.delete(id); }

    // ── GET ALL FOR USER ──────────────────────────────────────────────────────
    public List<Transaction> getUserTransactions(int userId) {
        return dao.getByUserId(userId);
    }

    // ── FILTER BY TYPE ────────────────────────────────────────────────────────
    // type = "INCOME" or "EXPENSE" or "ALL"
    public List<Transaction> getByType(int userId, String type) {
        if ("ALL".equals(type)) return dao.getByUserId(userId);
        return dao.getByUserAndType(userId, type);
    }

    // ── SEARCH BY NOTE OR CATEGORY NAME ──────────────────────────────────────
    public List<Transaction> search(int userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dao.getByUserId(userId);
        }
        return dao.search(userId, keyword.trim());
    }

    // ── BUSINESS LOGIC — calculate total by type ─────────────────────────────
    // used by dashboard to show income / expense / balance
    public BigDecimal getTotalByType(int userId, String type) {
        return dao.getByUserId(userId).stream()
                .filter(t -> type.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── BUSINESS LOGIC — get last N transactions ──────────────────────────────
    public List<Transaction> getRecent(int userId, int limit) {
        return dao.getByUserId(userId).stream()
                .limit(limit)
                .toList();
    }
}