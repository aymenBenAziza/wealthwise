package com.wealthwise.utils;

import com.wealthwise.models.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    // ── Private constructor — Singleton ───────────────────────────────────────
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Session management ────────────────────────────────────────────────────
    public User getCurrentUser()               { return currentUser; }
    public void setCurrentUser(User user)      { this.currentUser = user; }

    public boolean isLoggedIn()                { return currentUser != null; }
    public boolean isAdmin()                   {
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout()                       { this.currentUser = null; }
}