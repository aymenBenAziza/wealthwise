package com.wealthwise.services;

import com.wealthwise.dao.UserDao;
import com.wealthwise.dao.UserProfileDao;
import com.wealthwise.models.User;
import com.wealthwise.models.UserProfile;
import com.wealthwise.utils.PasswordUtils;

public class UserService {

    private final UserDao        userDao    = new UserDao();
    private final UserProfileDao profileDao = new UserProfileDao();

    // ── LOGIN — business logic ────────────────────────────────────────────────
    // rule 1: email must exist
    // rule 2: password hash must match
    // rule 3: account must be active
    public User login(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user == null) return null;                           // email not found
        if (!user.isActive()) return null;                       // account disabled
        if (!PasswordUtils.verify(password, user.getPassword())) return null; // wrong pwd
        return user;
    }

    // ── REGISTER — business logic ─────────────────────────────────────────────
    // rule 1: email must be unique
    // rule 2: password is hashed before saving
    // rule 3: default role is USER
    // rule 4: auto-create user_profile row
    public boolean register(String name, String email, String password) {
        if (userDao.findByEmail(email) != null) return false; // email taken

        String hashed = PasswordUtils.hash(password);
        User   user   = new User(name, email, hashed, "USER");
        userDao.add(user);

        // auto-create empty profile for new user
        User saved = userDao.findByEmail(email);
        if (saved != null) {
            profileDao.add(new UserProfile(saved.getId()));
        }
        return true;
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────────
    public void updateUser(User user) {
        userDao.update(user);
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────────────
    // rule: current password must be verified before changing
    public boolean changePassword(User user, String currentPwd, String newPwd) {
        if (!PasswordUtils.verify(currentPwd, user.getPassword())) return false;
        user.setPassword(PasswordUtils.hash(newPwd));
        userDao.update(user);
        return true;
    }
}