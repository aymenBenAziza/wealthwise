package com.wealthwise.controllers;

import com.wealthwise.dao.CategoryDao;
import com.wealthwise.models.Category;
import com.wealthwise.models.Transaction;
import com.wealthwise.services.TransactionService;
import com.wealthwise.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TransactionController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private TextField                           amountField;
    @FXML private ComboBox<String>                    typeCombo;
    @FXML private ComboBox<Category>                  categoryCombo;
    @FXML private DatePicker                          datePicker;
    @FXML private TextField                           noteField;
    @FXML private Label                               amountError;
    @FXML private Label                               typeError;
    @FXML private Label                               categoryError;
    @FXML private Label                               dateError;
    @FXML private Label                               messageLabel;
    @FXML private ComboBox<String>                    filterCombo;
    @FXML private TextField                           searchField;
    @FXML private TableView<Transaction>              transactionTable;
    @FXML private TableColumn<Transaction, String>    noteColumn;
    @FXML private TableColumn<Transaction, String>    typeColumn;
    @FXML private TableColumn<Transaction, Integer>   categoryColumn;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, BigDecimal>amountColumn;
    @FXML private TableColumn<Transaction, Void>      actionColumn;

    // ── Services ──────────────────────────────────────────────────────────────
    private final TransactionService service     = new TransactionService();
    private final CategoryDao        categoryDao = new CategoryDao();
    private Transaction editingTransaction = null;

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        typeCombo.setItems(FXCollections.observableArrayList("INCOME", "EXPENSE"));
        filterCombo.setItems(FXCollections.observableArrayList("ALL", "INCOME", "EXPENSE"));
        filterCombo.setValue("ALL");
        reloadCategoryCombo(userId);
        datePicker.setValue(LocalDate.now());
        setupColumns();
        loadTransactions();
    }

    // ── Reload category combo ─────────────────────────────────────────────────
    private void reloadCategoryCombo(int userId) {
        Category selected = categoryCombo.getValue();
        List<Category> cats = categoryDao.getByUser(userId);
        categoryCombo.setItems(FXCollections.observableArrayList(cats));
        // restore selection if still exists
        if (selected != null) {
            cats.stream().filter(c -> c.getId() == selected.getId())
                    .findFirst().ifPresent(categoryCombo::setValue);
        }
    }

    // ── ADD CATEGORY — simple dialog ──────────────────────────────────────────
    @FXML
    public void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvelle categorie");
        dialog.setHeaderText("Ajouter une categorie personnalisee");
        dialog.setContentText("Nom :");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) return;
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            categoryDao.add(new Category(name.trim(), "", "#3498db", userId));
            reloadCategoryCombo(userId);
            // auto-select the new category
            categoryCombo.getItems().stream()
                    .filter(c -> c.getName().equals(name.trim()) && c.isCustom())
                    .findFirst().ifPresent(categoryCombo::setValue);
            showSuccess("Categorie '" + name.trim() + "' ajoutee.");
        });
    }

    // ── MANAGE CATEGORIES — full dialog with edit + delete ────────────────────
    @FXML
    public void handleManageCategories() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        // build the dialog content
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gerer les categories");
        dialog.setHeaderText("Vos categories personnalisees");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPrefWidth(420);
        content.setPadding(new Insets(12));

        refreshCategoryList(content, userId, dialog);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();

        // reload combo after dialog closes
        reloadCategoryCombo(userId);
    }

    // ── Build category list inside dialog ─────────────────────────────────────
    private void refreshCategoryList(VBox content, int userId, Dialog<Void> dialog) {
        content.getChildren().clear();

        List<Category> customCats = categoryDao.getCustomByUser(userId);

        if (customCats.isEmpty()) {
            Label empty = new Label("Aucune categorie personnalisee.");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #aaaaaa;");
            content.getChildren().add(empty);
        } else {
            for (Category cat : customCats) {
                HBox row = buildCategoryRow(cat, userId, content, dialog);
                content.getChildren().add(row);
            }
        }

        // Add new category row at bottom
        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addRow.setPadding(new Insets(8, 0, 0, 0));

        TextField newCatField = new TextField();
        newCatField.setPromptText("Nouvelle categorie...");
        newCatField.setStyle("-fx-font-size: 13; -fx-pref-height: 34;" +
                "-fx-background-radius: 6; -fx-border-radius: 6;" +
                "-fx-border-color: #dddddd; -fx-border-width: 0.5;");
        HBox.setHgrow(newCatField, Priority.ALWAYS);

        Button addBtn = new Button("Ajouter");
        addBtn.setStyle("-fx-background-color: #185FA5; -fx-text-fill: white;" +
                "-fx-font-size: 12; -fx-background-radius: 6;" +
                "-fx-cursor: hand; -fx-padding: 6 14;");
        addBtn.setOnAction(e -> {
            String name = newCatField.getText().trim();
            if (name.isEmpty()) return;
            categoryDao.add(new Category(name, "", "#3498db", userId));
            newCatField.clear();
            refreshCategoryList(content, userId, dialog);
        });

        addRow.getChildren().addAll(newCatField, addBtn);
        content.getChildren().add(new Separator());
        content.getChildren().add(addRow);
    }

    // ── Build one category row: name | edit button | delete button ────────────
    private HBox buildCategoryRow(Category cat, int userId,
                                  VBox content, Dialog<Void> dialog) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 6 0; -fx-border-color: #f0f0f0;" +
                "-fx-border-width: 0 0 0.5 0;");

        // colored dot
        Label dot = new Label();
        dot.setStyle("-fx-min-width: 10; -fx-min-height: 10;" +
                "-fx-max-width: 10; -fx-max-height: 10;" +
                "-fx-background-radius: 5;" +
                "-fx-background-color: " + cat.getColor() + ";");

        // category name
        Label nameLabel = new Label(cat.getName());
        nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #333333;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Edit button
        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #E6F1FB; -fx-text-fill: #0C447C;" +
                "-fx-font-size: 11; -fx-background-radius: 4;" +
                "-fx-cursor: hand; -fx-padding: 4 10;");
        editBtn.setOnAction(e -> {
            TextInputDialog editDialog = new TextInputDialog(cat.getName());
            editDialog.setTitle("Modifier la categorie");
            editDialog.setHeaderText("Renommer : " + cat.getName());
            editDialog.setContentText("Nouveau nom :");
            editDialog.showAndWait().ifPresent(newName -> {
                if (newName.trim().isEmpty()) return;
                cat.setName(newName.trim());
                categoryDao.update(cat);
                refreshCategoryList(content, userId, dialog);
                reloadCategoryCombo(userId);
                showSuccess("Categorie renommee en '" + newName.trim() + "'.");
            });
        });

        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D;" +
                "-fx-font-size: 11; -fx-background-radius: 4;" +
                "-fx-cursor: hand; -fx-padding: 4 10;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Supprimer la categorie \"" + cat.getName() + "\" ?");
            confirm.setContentText(
                    "Les transactions liees a cette categorie seront affectees.");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    categoryDao.delete(cat.getId());
                    refreshCategoryList(content, userId, dialog);
                    reloadCategoryCombo(userId);
                    showSuccess("Categorie supprimee.");
                }
            });
        });

        row.getChildren().addAll(dot, nameLabel, editBtn, deleteBtn);
        return row;
    }

    // ── Column setup ──────────────────────────────────────────────────────────
    private void setupColumns() {
        noteColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getNote() != null ? cell.getValue().getNote() : "—"));

        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType()));
        typeColumn.setCellFactory(col -> new TableCell<Transaction, String>() {
            private final Label badge = new Label();
            { badge.setStyle("-fx-font-size: 11; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 3 10;"); }
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 4 0;");
                if (empty || type == null) { setGraphic(null); return; }
                boolean isIncome = "INCOME".equals(type);
                badge.setText(isIncome ? "Revenu" : "Depense");
                badge.setStyle("-fx-font-size: 11; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-padding: 3 10;" +
                        (isIncome
                                ? "-fx-background-color: #E1F5EE; -fx-text-fill: #0F6E56;"
                                : "-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D;"));
                setGraphic(badge);
            }
        });

        categoryColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getCategoryId()).asObject());
        categoryColumn.setCellFactory(col -> new TableCell<Transaction, Integer>() {
            @Override
            protected void updateItem(Integer catId, boolean empty) {
                super.updateItem(catId, empty);
                if (empty || catId == null) { setText(null); return; }
                Category cat = categoryDao.getById(catId);
                setText(cat != null ? cat.getName() : "—");
                setStyle("-fx-font-size: 13;");
            }
        });

        dateColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getTransactionDate()));
        dateColumn.setCellFactory(col -> new TableCell<Transaction, LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); return; }
                setText(date.format(fmt));
                setStyle("-fx-font-size: 13;");
            }
        });

        amountColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getAmount()));
        amountColumn.setCellFactory(col -> new TableCell<Transaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) { setText(null); setStyle(""); return; }
                Transaction t = getTableView().getItems().get(getIndex());
                boolean isIncome = "INCOME".equals(t.getType());
                setText((isIncome ? "+" : "-") + amount + " TND");
                setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: "
                        + (isIncome ? "#1D9E75" : "#E24B4A") + ";");
            }
        });

        actionColumn.setCellFactory(col -> new TableCell<Transaction, Void>() {
            private final Button editBtn   = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);
            {
                box.setPadding(new Insets(2, 0, 2, 0));
                editBtn.setMinWidth(70);
                editBtn.setStyle("-fx-background-color: #E6F1FB; -fx-text-fill: #0C447C;" +
                        "-fx-font-size: 11; -fx-background-radius: 4;" +
                        "-fx-cursor: hand; -fx-padding: 4 8;");
                deleteBtn.setMinWidth(76);
                deleteBtn.setStyle("-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D;" +
                        "-fx-font-size: 11; -fx-background-radius: 4;" +
                        "-fx-cursor: hand; -fx-padding: 4 8;");
                editBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    loadIntoForm(t);
                });
                deleteBtn.setOnAction(e -> {
                    Transaction t = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setHeaderText("Supprimer cette transaction ?");
                    confirm.setContentText("Cette action est irreversible.");
                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            service.deleteTransaction(t.getId());
                            if (editingTransaction != null &&
                                    editingTransaction.getId() == t.getId()) cancelEdit();
                            showSuccess("Transaction supprimee.");
                            loadTransactions();
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Load table ────────────────────────────────────────────────────────────
    private void loadTransactions() {
        int    userId  = SessionManager.getInstance().getCurrentUser().getId();
        String filter  = filterCombo.getValue();
        String keyword = searchField.getText().trim();
        List<Transaction> list = keyword.isEmpty()
                ? service.getByType(userId, filter != null ? filter : "ALL")
                : service.search(userId, keyword);
        transactionTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML public void handleFilter() { loadTransactions(); }
    @FXML public void handleSearch() { loadTransactions(); }

    // ── Add / Update transaction ──────────────────────────────────────────────
    @FXML
    public void handleAdd() {
        clearErrors();
        String    amountText = amountField.getText().trim();
        String    type       = typeCombo.getValue();
        Category  category   = categoryCombo.getValue();
        LocalDate date       = datePicker.getValue();
        String    note       = noteField.getText().trim();
        boolean   valid      = true;

        if (amountText.isEmpty()) {
            amountError.setText("Le montant est obligatoire."); valid = false;
        } else {
            try {
                if (new BigDecimal(amountText).compareTo(BigDecimal.ZERO) <= 0) {
                    amountError.setText("Le montant doit etre positif."); valid = false;
                }
            } catch (NumberFormatException ex) {
                amountError.setText("Montant invalide (ex: 150.00)."); valid = false;
            }
        }
        if (type == null)     { typeError.setText("Choisissez un type."); valid = false; }
        if (category == null) { categoryError.setText("Choisissez une categorie."); valid = false; }
        if (date == null) {
            dateError.setText("La date est obligatoire."); valid = false;
        } else if (date.isAfter(LocalDate.now())) {
            dateError.setText("La date ne peut pas etre dans le futur."); valid = false;
        }
        if (!valid) return;

        int userId = SessionManager.getInstance().getCurrentUser().getId();
        if (editingTransaction != null) {
            editingTransaction.setAmount(new BigDecimal(amountText));
            editingTransaction.setType(type);
            editingTransaction.setCategoryId(category.getId());
            editingTransaction.setTransactionDate(date);
            editingTransaction.setNote(note);
            service.updateTransaction(editingTransaction);
            cancelEdit();
            showSuccess("Transaction mise a jour avec succes.");
        } else {
            service.addTransaction(new Transaction(
                    userId, category.getId(),
                    new BigDecimal(amountText), type, date, note));
            clearForm();
            showSuccess("Transaction ajoutee avec succes.");
        }
        loadTransactions();
    }

    private void loadIntoForm(Transaction t) {
        editingTransaction = t;
        amountField.setText(t.getAmount().toPlainString());
        typeCombo.setValue(t.getType());
        datePicker.setValue(t.getTransactionDate());
        noteField.setText(t.getNote() != null ? t.getNote() : "");
        categoryCombo.getItems().stream()
                .filter(c -> c.getId() == t.getCategoryId())
                .findFirst().ifPresent(categoryCombo::setValue);
        showInfo("Mode edition — modifiez et cliquez + Ajouter pour sauvegarder.");
    }

    private void cancelEdit() {
        editingTransaction = null;
        clearForm();
        messageLabel.setText("");
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML public void goToDashboard()     { navigateTo("/fxml/Dashboard.fxml"); }
    @FXML public void goToBudgets()       { navigateTo("/fxml/Budget.fxml"); }
    @FXML public void goToProfile()       { navigateTo("/fxml/UserProfile.fxml"); }
    @FXML public void handleLogout() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Stage stage = (Stage) amountField.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource(fxml)), 900, 600);
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        amountError.setText(""); typeError.setText("");
        categoryError.setText(""); dateError.setText("");
        messageLabel.setText(""); messageLabel.setStyle("");
    }

    private void clearForm() {
        amountField.clear(); noteField.clear();
        typeCombo.setValue(null); categoryCombo.setValue(null);
        datePicker.setValue(LocalDate.now());
        editingTransaction = null;
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #1D9E75; -fx-background-color: #E1F5EE;" +
                "-fx-background-radius: 6; -fx-padding: 6 12;");
    }

    private void showInfo(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: #0C447C; -fx-background-color: #E6F1FB;" +
                "-fx-background-radius: 6; -fx-padding: 6 12;");
    }
}