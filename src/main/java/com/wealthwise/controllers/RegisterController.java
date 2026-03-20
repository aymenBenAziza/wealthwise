package com.wealthwise.controllers;

import com.wealthwise.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label         nameError;
    @FXML private Label         emailError;
    @FXML private Label         passwordError;
    @FXML private Label         confirmError;
    @FXML private Label         messageLabel;

    private final UserService userService = new UserService();

    // ── Handle register button ────────────────────────────────────────────────
    @FXML
    public void handleRegister() {
        clearErrors();

        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        boolean valid = true;

        if (name.length() < 3) {
            nameError.setText("Le nom doit contenir au moins 3 caracteres.");
            valid = false;
        }
        if (email.isEmpty() || !isValidEmail(email)) {
            emailError.setText("Format d'email invalide.");
            valid = false;
        }
        if (password.length() < 6) {
            passwordError.setText("Le mot de passe doit contenir au moins 6 caracteres.");
            valid = false;
        }
        if (!password.equals(confirm)) {
            confirmError.setText("Les mots de passe ne correspondent pas.");
            valid = false;
        }
        if (!valid) return;

        boolean success = userService.register(name, email, password);

        if (success) {
            showSuccess("Compte cree avec succes ! Redirection...");
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(
                        () -> navigateTo("/fxml/Login.fxml"));
            }).start();
        } else {
            emailError.setText("Cet email est deja utilise.");
        }
    }

    @FXML
    public void goToLogin() { navigateTo("/fxml/Login.fxml"); }

    private void clearErrors() {
        nameError.setText("");
        emailError.setText("");
        passwordError.setText("");
        confirmError.setText("");
        messageLabel.setText("");
        messageLabel.setStyle("");
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle(
                "-fx-text-fill: #1D9E75; -fx-background-color: #E1F5EE;" +
                        "-fx-background-radius: 6; -fx-padding: 6 12;");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}