package com.wealthwise.controllers;

import com.wealthwise.models.User;
import com.wealthwise.services.UserService;
import com.wealthwise.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         emailError;
    @FXML private Label         passwordError;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();

    // ── Handle login button ───────────────────────────────────────────────────
    @FXML
    public void handleLogin() {
        clearErrors();

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // format validation
        boolean valid = true;
        if (email.isEmpty()) {
            emailError.setText("L'email est obligatoire.");
            valid = false;
        } else if (!isValidEmail(email)) {
            emailError.setText("Format d'email invalide.");
            valid = false;
        }
        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est obligatoire.");
            valid = false;
        }
        if (!valid) return;

        // business logic in service
        User user = userService.login(email, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            navigateTo("/fxml/Dashboard.fxml");
        } else {
            // intentionally vague — don't reveal which field is wrong
            errorLabel.setText("Email ou mot de passe incorrect.");
            errorLabel.setStyle(
                    "-fx-text-fill: #A32D2D; -fx-background-color: #FCEBEB;" +
                            "-fx-background-radius: 6; -fx-padding: 6 12;");
        }
    }

    // ── Navigate to register ──────────────────────────────────────────────────
    @FXML
    public void goToRegister() {
        navigateTo("/fxml/Register.fxml");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        emailError.setText("");
        passwordError.setText("");
        errorLabel.setText("");
        errorLabel.setStyle("");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}