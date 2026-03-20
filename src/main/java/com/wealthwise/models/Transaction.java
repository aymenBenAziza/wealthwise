package com.wealthwise.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {

    private int           id;
    private int           userId;
    private int           categoryId;
    private BigDecimal    amount;
    private String        type;             // "INCOME" or "EXPENSE"
    private LocalDate     transactionDate;
    private String        note;
    private String        receiptUrl;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────
    public Transaction() {}

    public Transaction(int userId, int categoryId, BigDecimal amount,
                       String type, LocalDate date, String note) {
        this.userId          = userId;
        this.categoryId      = categoryId;
        this.amount          = amount;
        this.type            = type;
        this.transactionDate = date;
        this.note            = note;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int           getId()                         { return id; }
    public void          setId(int id)                   { this.id = id; }

    public int           getUserId()                     { return userId; }
    public void          setUserId(int userId)           { this.userId = userId; }

    public int           getCategoryId()                 { return categoryId; }
    public void          setCategoryId(int categoryId)   { this.categoryId = categoryId; }

    public BigDecimal    getAmount()                     { return amount; }
    public void          setAmount(BigDecimal amount)    { this.amount = amount; }

    public String        getType()                       { return type; }
    public void          setType(String type)            { this.type = type; }

    public LocalDate     getTransactionDate()            { return transactionDate; }
    public void          setTransactionDate(LocalDate d) { this.transactionDate = d; }

    public String        getNote()                       { return note; }
    public void          setNote(String note)            { this.note = note; }

    public String        getReceiptUrl()                 { return receiptUrl; }
    public void          setReceiptUrl(String url)       { this.receiptUrl = url; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)   { this.createdAt = t; }

    @Override
    public String toString() {
        return type + " | " + amount + " TND | " + transactionDate;
    }
}