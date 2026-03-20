package com.wealthwise.models;

public class BudgetAlert {

    private int     id;
    private int     budgetId;
    private int     threshold;    // default 80 (%)
    private String  triggeredAt;  // nullable
    private boolean isTriggered;

    // ── Constructors ──────────────────────────────────────────────────────────
    public BudgetAlert() {}

    public BudgetAlert(int budgetId) {
        this.budgetId    = budgetId;
        this.threshold   = 80;
        this.isTriggered = false;
    }

    public BudgetAlert(int budgetId, int threshold) {
        this.budgetId    = budgetId;
        this.threshold   = threshold;
        this.isTriggered = false;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int     getId()                        { return id; }
    public void    setId(int id)                  { this.id = id; }

    public int     getBudgetId()                  { return budgetId; }
    public void    setBudgetId(int budgetId)       { this.budgetId = budgetId; }

    public int     getThreshold()                 { return threshold; }
    public void    setThreshold(int threshold)     { this.threshold = threshold; }

    public String  getTriggeredAt()               { return triggeredAt; }
    public void    setTriggeredAt(String t)        { this.triggeredAt = t; }

    public boolean isTriggered()                  { return isTriggered; }
    public void    setTriggered(boolean triggered) { this.isTriggered = triggered; }
}