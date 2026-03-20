package com.wealthwise.controllers;

import com.wealthwise.dao.UserProfileDao;
import com.wealthwise.models.User;
import com.wealthwise.models.UserProfile;
import com.wealthwise.services.UserService;
import com.wealthwise.utils.PasswordUtils;
import com.wealthwise.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class UserProfileController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label        avatarLabel;
    @FXML private Label        userNameDisplay;
    @FXML private Label        userEmailDisplay;
    @FXML private Label        roleLabel;
    @FXML private Label        memberSinceLabel;
    @FXML private Label        messageLabel;

    // Personal info
    @FXML private TextField    nameField;
    @FXML private TextField    emailField;
    @FXML private Label        nameError;
    @FXML private Label        emailError;

    // Password
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         currentPwdError;
    @FXML private Label         newPwdError;
    @FXML private Label         confirmPwdError;

    // Preferences
    @FXML private TextField    incomeField;
    @FXML private ComboBox<String> currencyCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Label        incomeError;

    // ── Services & DAOs ───────────────────────────────────────────────────────
    private final UserService      userService  = new UserService();
    private final UserProfileDao   profileDao   = new UserProfileDao();

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { navigateTo("/fxml/Login.fxml"); return; }

        // fill combos
        currencyCombo.setItems(FXCollections.observableArrayList("TND", "EUR", "USD", "GBP"));
        languageCombo.setItems(FXCollections.observableArrayList("FR", "AR", "EN"));

        // fill personal info
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());

        // avatar — first letter of name
        String initials = user.getName().isEmpty()
                ? "?" : String.valueOf(user.getName().charAt(0)).toUpperCase();
        avatarLabel.setText(initials);
        userNameDisplay.setText(user.getName());
        userEmailDisplay.setText(user.getEmail());
        roleLabel.setText(user.getRole());
        memberSinceLabel.setText("2026");

        // load existing profile if exists
        UserProfile profile = profileDao.getByUserId(user.getId());
        if (profile != null) {
            incomeField.setText(profile.getMonthlyIncome() != null
                    ? profile.getMonthlyIncome().toPlainString() : "");
            currencyCombo.setValue(profile.getPreferredCurrency() != null
                    ? profile.getPreferredCurrency() : "TND");
            languageCombo.setValue(profile.getLanguage() != null
                    ? profile.getLanguage() : "FR");
        } else {
            currencyCombo.setValue("TND");
            languageCombo.setValue("FR");
        }
    }

    // ── Save personal info ────────────────────────────────────────────────────
    @FXML
    public void handleSaveInfo() {
        clearErrors();
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        boolean valid = true;

        if (name.length() < 3) {
            nameError.setText("Le nom doit contenir au moins 3 caracteres.");
            valid = false;
        }
        if (email.isEmpty() || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            emailError.setText("Email invalide.");
            valid = false;
        }
        if (!valid) return;

        User user = SessionManager.getInstance().getCurrentUser();
        user.setName(name);
        user.setEmail(email);
        userService.updateUser(user);

        // update session
        SessionManager.getInstance().setCurrentUser(user);
        userNameDisplay.setText(name);
        userEmailDisplay.setText(email);
        avatarLabel.setText(String.valueOf(name.charAt(0)).toUpperCase());

        showSuccess("Informations mises a jour avec succes.");
    }

    // ── Change password ───────────────────────────────────────────────────────
    @FXML
    public void handleChangePassword() {
        clearErrors();
        String current = currentPasswordField.getText();
        String newPwd  = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();
        boolean valid  = true;

        User user = SessionManager.getInstance().getCurrentUser();

        if (current.isEmpty()) {
            currentPwdError.setText("Entrez votre mot de passe actuel.");
            valid = false;
        } else if (!PasswordUtils.verify(current, user.getPassword())) {
            currentPwdError.setText("Mot de passe actuel incorrect.");
            valid = false;
        }
        if (newPwd.length() < 6) {
            newPwdError.setText("Le nouveau mot de passe doit contenir au moins 6 caracteres.");
            valid = false;
        }
        if (!newPwd.equals(confirm)) {
            confirmPwdError.setText("Les mots de passe ne correspondent pas.");
            valid = false;
        }
        if (!valid) return;

        // hash and save new password
        user.setPassword(PasswordUtils.hash(newPwd));
        userService.updateUser(user);
        SessionManager.getInstance().setCurrentUser(user);

        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        showSuccess("Mot de passe modifie avec succes.");
    }

    // ── Save financial preferences ────────────────────────────────────────────
    @FXML
    public void handleSavePreferences() {
        clearErrors();
        String incomeText = incomeField.getText().trim();
        String currency   = currencyCombo.getValue();

        if (incomeText.isEmpty()) {
            incomeError.setText("Entrez votre revenu mensuel.");
            return;
        }

        BigDecimal income;
        try {
            income = new BigDecimal(incomeText);
            if (income.compareTo(BigDecimal.ZERO) < 0) {
                incomeError.setText("Le revenu doit etre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            incomeError.setText("Montant invalide (ex: 3000).");
            return;
        }

        User        user    = SessionManager.getInstance().getCurrentUser();
        UserProfile profile = profileDao.getByUserId(user.getId());

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
        }

        profile.setMonthlyIncome(income);
        profile.setPreferredCurrency(currency != null ? currency : "TND");

        // keep existing language
        String lang = languageCombo.getValue();
        profile.setLanguage(lang != null ? lang : "FR");

        profileDao.saveOrUpdate(profile);
        showSuccess("Preferences financieres sauvegardees. Revenu : " + income + " TND");
    }

    // ── Save language ─────────────────────────────────────────────────────────
    @FXML
    public void handleSaveLanguage() {
        String language = languageCombo.getValue();
        if (language == null) return;

        User        user    = SessionManager.getInstance().getCurrentUser();
        UserProfile profile = profileDao.getByUserId(user.getId());

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
            profile.setMonthlyIncome(BigDecimal.ZERO);
            profile.setPreferredCurrency("TND");
        }
        profile.setLanguage(language);
        profileDao.saveOrUpdate(profile);

        showSuccess("Langue mise a jour : " + language);
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML public void goToDashboard()    { navigateTo("/fxml/Dashboard.fxml"); }
    @FXML public void goToTransactions() { navigateTo("/fxml/Transaction.fxml"); }
    @FXML public void goToBudgets()      { navigateTo("/fxml/Budget.fxml"); }


    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        nameError.setText("");
        emailError.setText("");
        currentPwdError.setText("");
        newPwdError.setText("");
        confirmPwdError.setText("");
        incomeError.setText("");
        messageLabel.setText("");
        messageLabel.setStyle("");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle(
                "-fx-text-fill: #1D9E75; -fx-background-color: #E1F5EE;" +
                        "-fx-background-radius: 6; -fx-padding: 8 14;"
        );
    }
}