package com.wealthwise.dao;

import com.wealthwise.models.BudgetAlert;
import com.wealthwise.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetAlertDao implements IDao<BudgetAlert> {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ── C — INSERT ────────────────────────────────────────────────────────────
    @Override
    public void add(BudgetAlert a) {
        String sql = "INSERT INTO budget_alert (budget_id, threshold, is_triggered) " +
                "VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getBudgetId());
            ps.setInt(2, a.getThreshold());
            ps.setBoolean(3, false);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── U — TRIGGER ALERT ─────────────────────────────────────────────────────
    @Override
    public void update(BudgetAlert a) {
        String sql = "UPDATE budget_alert SET is_triggered=?, triggered_at=NOW() " +
                "WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, a.isTriggered());
            ps.setInt(2, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── D — DELETE BY BUDGET ID ───────────────────────────────────────────────
    @Override
    public void delete(int budgetId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM budget_alert WHERE budget_id=?")) {
            ps.setInt(1, budgetId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── R — GET BY ID ─────────────────────────────────────────────────────────
    @Override
    public BudgetAlert getById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM budget_alert WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — GET ALL ───────────────────────────────────────────────────────────
    @Override
    public List<BudgetAlert> getAll() {
        List<BudgetAlert> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM budget_alert")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — GET BY BUDGET ID ──────────────────────────────────────────────────
    public BudgetAlert getByBudgetId(int budgetId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM budget_alert WHERE budget_id=?")) {
            ps.setInt(1, budgetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — IS ALREADY TRIGGERED ──────────────────────────────────────────────
    public boolean isAlreadyTriggered(int budgetId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT is_triggered FROM budget_alert WHERE budget_id=?")) {
            ps.setInt(1, budgetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("is_triggered");
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── MAP ROW → BudgetAlert ─────────────────────────────────────────────────
    private BudgetAlert mapRow(ResultSet rs) throws SQLException {
        BudgetAlert a = new BudgetAlert();
        a.setId(rs.getInt("id"));
        a.setBudgetId(rs.getInt("budget_id"));
        a.setThreshold(rs.getInt("threshold"));
        a.setTriggeredAt(rs.getString("triggered_at"));
        a.setTriggered(rs.getBoolean("is_triggered"));
        return a;
    }
}