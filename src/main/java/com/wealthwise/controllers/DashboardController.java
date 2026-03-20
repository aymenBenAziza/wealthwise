package com.wealthwise.controllers;

import com.wealthwise.models.User;
import com.wealthwise.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { navigateTo("/fxml/Login.fxml"); return; }

        welcomeLabel.setText("Bonjour, " + user.getName());
        userInfoLabel.setText("Email : " + user.getEmail() +
                " | Role : " + user.getRole());
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml");
    }

    @FXML public void goToProfile() { navigateTo("/fxml/UserProfile.fxml"); }
    @FXML public void goToTransactions() { navigateTo("/fxml/Transaction.fxml"); }
    @FXML public void goToBudgets() { navigateTo("/fxml/Budget.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}