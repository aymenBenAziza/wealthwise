package com.wealthwise.controllers;

import com.wealthwise.dao.CategoryDao;
import com.wealthwise.models.Budget;
import com.wealthwise.models.Category;
import com.wealthwise.services.BudgetService;
import com.wealthwise.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetController {

    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField          limitField;
    @FXML private Label              categoryError;
    @FXML private Label              limitError;
    @FXML private Label              messageLabel;
    @FXML private VBox               budgetContainer;
    @FXML private Label              monthLabel;

    private final BudgetService budgetService = new BudgetService();
    private final CategoryDao   categoryDao   = new CategoryDao();

    private static final DateTimeFormatter MONTH_FMT   =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    @FXML
    public void initialize() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        categoryCombo.setItems(FXCollections.observableArrayList(
                categoryDao.getByUser(userId)));
        monthLabel.setText("Budgets — " + LocalDate.now().format(DISPLAY_FMT));
        loadBudgets();
    }

    @FXML
    public void handleAdd() {
        clearErrors();
        Category  category  = categoryCombo.getValue();
        String    limitText = limitField.getText().trim();
        boolean   valid     = true;

        if (category == null) {
            categoryError.setText("Choisissez une categorie."); valid = false;
        }

        BigDecimal limit = BigDecimal.ZERO;
        if (limitText.isEmpty()) {
            limitError.setText("La limite est obligatoire."); valid = false;
        } else {
            try {
                limit = new BigDecimal(limitText);
                if (limit.compareTo(BigDecimal.ZERO) <= 0) {
                    limitError.setText("La limite doit etre positive."); valid = false;
                }
            } catch (NumberFormatException e) {
                limitError.setText("Montant invalide (ex: 300)."); valid = false;
            }
        }
        if (!valid) return;

        int    userId    = SessionManager.getInstance().getCurrentUser().getId();
        String monthYear = LocalDate.now().format(MONTH_FMT);
        Budget budget    = new Budget(userId, category.getId(), monthYear, limit);

        boolean added = budgetService.addBudget(budget);
        if (added) {
            clearForm();
            showSuccess("Budget ajoute pour " + category.getName() + ".");
            loadBudgets();
        } else {
            categoryError.setText("Un budget existe deja pour cette categorie ce mois.");
        }
    }

    private void loadBudgets() {
        budgetContainer.getChildren().clear();
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        List<Budget> budgets = budgetService.getCurrentMonthBudgets(userId);

        if (budgets.isEmpty()) {
            Label empty = new Label("Aucun budget ce mois. Ajoutez-en un ci-dessus.");
            empty.setStyle("-fx-font-size: 13; -fx-text-fill: #aaaaaa;");
            budgetContainer.getChildren().add(empty);
            return;
        }

        for (Budget b : budgets) {
            budgetContainer.getChildren().add(buildBudgetCard(b, userId));
        }
    }

    private VBox buildBudgetCard(Budget budget, int userId) {
        double pct = Math.min(
                budgetService.getConsumptionPercent(budget, userId), 100);
        BigDecimal spent = budgetService.getSpentAmount(budget, userId);

        String color = pct >= 85 ? "#E24B4A" : pct >= 60 ? "#EF9F27" : "#1D9E75";

        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #eeeeee; -fx-border-width: 0.5;" +
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-padding: 16 18;");

        // category name
        Category cat    = categoryDao.getById(budget.getCategoryId());
        String   catName = cat != null ? cat.getName()
                : "Categorie " + budget.getCategoryId();

        // header: name + %
        HBox headerRow = new HBox();
        Label catLabel = new Label(catName);
        catLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #222222;");
        HBox.setHgrow(catLabel, Priority.ALWAYS);
        Label pctLabel = new Label(String.format("%.0f%%", pct));
        pctLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        headerRow.getChildren().addAll(catLabel, pctLabel);

        // amounts
        Label amountsLabel = new Label(String.format(
                "%.2f TND / %.2f TND",
                spent.doubleValue(), budget.getLimitAmount().doubleValue()));
        amountsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #888888;");

        // alert badge
        if (pct >= 80) {
            Label alertBadge = new Label(
                    pct >= 100 ? "Budget depasse !" : "Attention : 80% atteint");
            alertBadge.setStyle(
                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + (pct >= 100 ? "#A32D2D" : "#633806") + ";" +
                            "-fx-background-color: " + (pct >= 100 ? "#FCEBEB" : "#FAEEDA") + ";" +
                            "-fx-background-radius: 4; -fx-padding: 3 10;");
            card.getChildren().addAll(headerRow, amountsLabel, alertBadge);
        } else {
            card.getChildren().addAll(headerRow, amountsLabel);
        }

        // progress bar
        HBox barBg = new HBox();
        barBg.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 6;");
        barBg.setPrefHeight(10);
        barBg.setMaxWidth(Double.MAX_VALUE);
        Rectangle fill = new Rectangle(0, 10);
        fill.setArcWidth(6);
        fill.setArcHeight(6);
        fill.setStyle("-fx-fill: " + color + ";");
        final double fp = pct;
        barBg.widthProperty().addListener((obs, o, n) ->
                fill.setWidth(n.doubleValue() * fp / 100));
        barBg.getChildren().add(fill);

        // action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier la limite");
        editBtn.setStyle(
                "-fx-background-color: #E6F1FB; -fx-text-fill: #0C447C;" +
                        "-fx-font-size: 11; -fx-background-radius: 4;" +
                        "-fx-cursor: hand; -fx-padding: 5 12;");
        editBtn.setOnAction(e -> showEditDialog(budget, catName));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle(
                "-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D;" +
                        "-fx-font-size: 11; -fx-background-radius: 4;" +
                        "-fx-cursor: hand; -fx-padding: 5 12;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer le budget \"" + catName + "\" ?");
            confirm.setContentText("Cette action est irreversible.");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    budgetService.deleteBudget(budget.getId());
                    showSuccess("Budget supprime.");
                    loadBudgets();
                }
            });
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(barBg, actions);
        return card;
    }

    private void showEditDialog(Budget budget, String catName) {
        TextInputDialog dialog = new TextInputDialog(
                budget.getLimitAmount().toPlainString());
        dialog.setTitle("Modifier la limite");
        dialog.setHeaderText("Modifier le budget : " + catName);
        dialog.setContentText("Nouvelle limite (TND) :");

        dialog.showAndWait().ifPresent(input -> {
            try {
                BigDecimal newLimit = new BigDecimal(input.trim());
                if (newLimit.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("La limite doit etre positive."); return;
                }
                budget.setLimitAmount(newLimit);
                budgetService.updateBudget(budget);
                showSuccess("Limite mise a jour : " + newLimit + " TND");
                loadBudgets();
            } catch (NumberFormatException e) {
                showError("Montant invalide.");
            }
        });
    }

    @FXML public void goToDashboard()     { navigateTo("/fxml/Dashboard.fxml"); }
    @FXML public void goToTransactions()  { navigateTo("/fxml/Transaction.fxml"); }
    @FXML public void goToSavings()       { navigateTo("/fxml/SavingsGoal.fxml"); }
    @FXML public void goToAI()            { navigateTo("/fxml/AI.fxml"); }
    @FXML public void goToNotifications() { navigateTo("/fxml/Notification.fxml"); }
    @FXML public void goToProfile()       { navigateTo("/fxml/UserProfile.fxml"); }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) limitField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearErrors() {
        categoryError.setText(""); limitError.setText("");
        messageLabel.setText(""); messageLabel.setStyle("");
    }

    private void clearForm() {
        categoryCombo.setValue(null); limitField.clear();
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #1D9E75; -fx-background-color: #E1F5EE;" +
                "-fx-background-radius: 6; -fx-padding: 6 12;");
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #A32D2D; -fx-background-color: #FCEBEB;" +
                "-fx-background-radius: 6; -fx-padding: 6 12;");
    }
}