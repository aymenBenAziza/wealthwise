package com.wealthwise.services;

import com.wealthwise.dao.BudgetAlertDao;
import com.wealthwise.dao.BudgetDao;
import com.wealthwise.dao.CategoryDao;
import com.wealthwise.dao.TransactionDao;
import com.wealthwise.models.Budget;
import com.wealthwise.models.BudgetAlert;
import com.wealthwise.models.Category;
import com.wealthwise.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetService {

    private final BudgetDao      budgetDao      = new BudgetDao();
    private final BudgetAlertDao alertDao       = new BudgetAlertDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final CategoryDao    categoryDao    = new CategoryDao();

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");

    // ── ADD budget + auto-create alert at 80% ─────────────────────────────────
    public boolean addBudget(Budget b) {
        // business rule: one budget per category per month
        if (budgetDao.existsForCategoryAndMonth(
                b.getUserId(), b.getCategoryId(), b.getMonthYear())) {
            return false; // already exists
        }
        budgetDao.add(b);

        // fetch saved budget to get its generated id
        Budget saved = budgetDao
                .getByUserAndMonth(b.getUserId(), b.getMonthYear())
                .stream()
                .filter(x -> x.getCategoryId() == b.getCategoryId())
                .findFirst()
                .orElse(null);

        if (saved != null) {
            // auto-create budget_alert at 80% threshold
            alertDao.add(new BudgetAlert(saved.getId(), 80));
        }
        return true;
    }

    // ── UPDATE budget limit ───────────────────────────────────────────────────
    public void updateBudget(Budget b) {
        budgetDao.update(b);
    }

    // ── DELETE budget + its alert ─────────────────────────────────────────────
    public void deleteBudget(int id) {
        alertDao.delete(id);  // FK: delete alert first
        budgetDao.delete(id);
    }

    // ── GET current month budgets ─────────────────────────────────────────────
    public List<Budget> getCurrentMonthBudgets(int userId) {
        String monthYear = LocalDate.now().format(MONTH_FMT);
        return budgetDao.getByUserAndMonth(userId, monthYear);
    }

    // ── CALCULATE consumption % + trigger alert once ──────────────────────────
    public double getConsumptionPercent(Budget budget, int userId) {
        List<Transaction> transactions = transactionDao.getByUserId(userId);

        BigDecimal spent = transactions.stream()
                .filter(t -> t.getCategoryId() == budget.getCategoryId()
                        && "EXPENSE".equals(t.getType())
                        && t.getTransactionDate().format(MONTH_FMT).equals(budget.getMonthYear()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) == 0) return 0;

        double pct = spent
                .divide(budget.getLimitAmount(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        // trigger alert once when threshold reached
        if (pct >= 80) {
            BudgetAlert alert = alertDao.getByBudgetId(budget.getId());
            if (alert != null && !alert.isTriggered()) {
                alert.setTriggered(true);
                alertDao.update(alert);

                Category cat = categoryDao.getById(budget.getCategoryId());
                String catName = (cat != null) ? cat.getName()
                        : "Categorie " + budget.getCategoryId();

            }
        }

        return pct;
    }

    // ── GET spent amount for a budget ─────────────────────────────────────────
    public BigDecimal getSpentAmount(Budget budget, int userId) {
        return transactionDao.getByUserId(userId).stream()
                .filter(t -> t.getCategoryId() == budget.getCategoryId()
                        && "EXPENSE".equals(t.getType())
                        && t.getTransactionDate().format(MONTH_FMT).equals(budget.getMonthYear()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}