package com.wealthwise.models;

import java.math.BigDecimal;

public class UserProfile {

    private int        id;
    private int        userId;
    private String     avatarUrl;          // nullable
    private String     bio;                // nullable
    private BigDecimal monthlyIncome;
    private String     preferredCurrency;  // "TND", "EUR", "USD"
    private String     language;           // "FR", "AR", "EN"

    // ── Constructors ──────────────────────────────────────────────────────────
    public UserProfile() {}

    public UserProfile(int userId) {
        this.userId            = userId;
        this.monthlyIncome     = BigDecimal.ZERO;
        this.preferredCurrency = "TND";
        this.language          = "FR";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int        getId()                          { return id; }
    public void       setId(int id)                    { this.id = id; }

    public int        getUserId()                      { return userId; }
    public void       setUserId(int userId)            { this.userId = userId; }

    public String     getAvatarUrl()                   { return avatarUrl; }
    public void       setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }

    public String     getBio()                         { return bio; }
    public void       setBio(String bio)               { this.bio = bio; }

    public BigDecimal getMonthlyIncome()               { return monthlyIncome; }
    public void       setMonthlyIncome(BigDecimal m)   { this.monthlyIncome = m; }

    public String     getPreferredCurrency()           { return preferredCurrency; }
    public void       setPreferredCurrency(String c)   { this.preferredCurrency = c; }

    public String     getLanguage()                    { return language; }
    public void       setLanguage(String language)     { this.language = language; }

    @Override
    public String toString() {
        return "UserProfile{userId=" + userId +
                ", income=" + monthlyIncome +
                ", currency=" + preferredCurrency + "}";
    }
}