package com.wealthwise.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Budget {

    private int           id;
    private int           userId;
    private int           categoryId;
    private String        monthYear;      // format: "2026-03"
    private BigDecimal    limitAmount;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Budget() {}

    public Budget(int userId, int categoryId, String monthYear, BigDecimal limitAmount) {
        this.userId      = userId;
        this.categoryId  = categoryId;
        this.monthYear   = monthYear;
        this.limitAmount = limitAmount;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int           getId()                         { return id; }
    public void          setId(int id)                   { this.id = id; }

    public int           getUserId()                     { return userId; }
    public void          setUserId(int userId)           { this.userId = userId; }

    public int           getCategoryId()                 { return categoryId; }
    public void          setCategoryId(int c)            { this.categoryId = c; }

    public String        getMonthYear()                  { return monthYear; }
    public void          setMonthYear(String m)          { this.monthYear = m; }

    public BigDecimal    getLimitAmount()                { return limitAmount; }
    public void          setLimitAmount(BigDecimal l)    { this.limitAmount = l; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)   { this.createdAt = t; }

    @Override
    public String toString() {
        return "Budget{category=" + categoryId + ", month=" + monthYear +
                ", limit=" + limitAmount + " TND}";
    }
}