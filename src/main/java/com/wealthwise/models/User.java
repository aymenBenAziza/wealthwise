package com.wealthwise.models;

import java.time.LocalDateTime;

public class User {

    private int           id;
    private String        name;
    private String        email;
    private String        password;   // stored as MD5 hash
    private String        role;       // "USER" or "ADMIN"
    private boolean       isActive;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────
    public User() {}

    public User(String name, String email, String password, String role) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.isActive = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int           getId()                        { return id; }
    public void          setId(int id)                  { this.id = id; }

    public String        getName()                      { return name; }
    public void          setName(String name)           { this.name = name; }

    public String        getEmail()                     { return email; }
    public void          setEmail(String email)         { this.email = email; }

    public String        getPassword()                  { return password; }
    public void          setPassword(String password)   { this.password = password; }

    public String        getRole()                      { return role; }
    public void          setRole(String role)           { this.role = role; }

    public boolean       isActive()                     { return isActive; }
    public void          setActive(boolean active)      { this.isActive = active; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    public boolean isAdmin() { return "ADMIN".equals(role); }

    @Override
    public String toString() { return name + " <" + email + ">"; }
}