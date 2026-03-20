package com.wealthwise.dao;

import com.wealthwise.models.Budget;
import com.wealthwise.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao implements IDao<Budget> {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ── C — INSERT ────────────────────────────────────────────────────────────
    @Override
    public void add(Budget b) {
        String sql = "INSERT INTO budget (user_id, category_id, month_year, limit_amount) " +
                "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getCategoryId());
            ps.setString(3, b.getMonthYear());
            ps.setBigDecimal(4, b.getLimitAmount());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── U — UPDATE ────────────────────────────────────────────────────────────
    @Override
    public void update(Budget b) {
        String sql = "UPDATE budget SET limit_amount=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, b.getLimitAmount());
            ps.setInt(2, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── D — DELETE ────────────────────────────────────────────────────────────
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM budget WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── R — GET BY ID ─────────────────────────────────────────────────────────
    @Override
    public Budget getById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM budget WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — GET ALL ───────────────────────────────────────────────────────────
    @Override
    public List<Budget> getAll() {
        List<Budget> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM budget")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — GET BY USER AND MONTH ─────────────────────────────────────────────
    public List<Budget> getByUserAndMonth(int userId, String monthYear) {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT * FROM budget WHERE user_id=? AND month_year=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, monthYear);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — CHECK IF BUDGET EXISTS FOR CATEGORY + MONTH ──────────────────────
    public boolean existsForCategoryAndMonth(int userId, int categoryId, String monthYear) {
        String sql = "SELECT COUNT(*) FROM budget " +
                "WHERE user_id=? AND category_id=? AND month_year=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setString(3, monthYear);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── MAP ROW → Budget ──────────────────────────────────────────────────────
    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setMonthYear(rs.getString("month_year"));
        b.setLimitAmount(rs.getBigDecimal("limit_amount"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    }
}